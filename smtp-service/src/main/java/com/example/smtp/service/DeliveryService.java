package com.example.smtp.service;

import com.example.common.entity.OutboundQueue;
import com.example.common.repository.MailQueueRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final MailQueueRepository mailQueueRepository;
    private final MxLookupService mxLookupService;
    private final DkimSigningService dkimSigningService;

    @Value("${relay.host:}")
    private String relayHost;

    @Value("${relay.port:587}")
    private int relayPort;

    @Value("${relay.username:}")
    private String relayUsername;

    @Value("${relay.password:}")
    private String relayPassword;

    @Value("${smtp.client.connection.timeout:5000}")
    private String connectionTimeout;

    @Value("${smtp.client.read.timeout:10000}")
    private String readTimeout;

    @Value("${smtp.client.localhost:localhost}")
    private String localhost;

    @Value("${queue.retry.max.attempts:5}")
    private int maxRetries;

    @Value("${queue.retry.backoff.factor:2}")
    private int backoffFactor;

    public void processItem(OutboundQueue item) {
        try {
            log.info("Processing outbound item ID: {}", item.getId());

            // 1. Sign with DKIM (and parse)
            MimeMessage message;
            try {
                message = dkimSigningService.signMessage(item.getEmailData());
            } catch (Exception e) {
                log.error("Failed to sign message with DKIM, sending unsigned", e);
                // Fallback to unsigned if signing fails? Or fail?
                // Let's fallback for now but log error
                Session session = Session.getInstance(new Properties());
                message = new MimeMessage(session, new java.io.ByteArrayInputStream(item.getEmailData()));
            }

            // 2. Determine Target Host
            String recipient = item.getRecipient();
            String domain = recipient.substring(recipient.indexOf('@') + 1);
            String targetHost = relayHost;
            int targetPort = relayPort;

            if (targetHost == null || targetHost.isEmpty()) {
                List<String> mxRecords = mxLookupService.getMxRecords(domain);
                if (mxRecords.isEmpty()) {
                    throw new MessagingException("No MX records found for domain: " + domain);
                }
                targetHost = mxRecords.get(0);
                targetPort = 25; // Standard SMTP port for direct delivery
            }

            // 3. Send Email
            log.info("Sending email to {} via {}:{}", recipient, targetHost, targetPort);
            Properties props = new Properties();
            props.put("mail.smtp.host", targetHost);
            props.put("mail.smtp.port", String.valueOf(targetPort));
            props.put("mail.smtp.connectiontimeout", connectionTimeout);
            props.put("mail.smtp.timeout", readTimeout);
            props.put("mail.smtp.localhost", localhost); // HELO/EHLO host

            if (relayUsername != null && !relayUsername.isEmpty()) {
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session transportSession = Session.getInstance(props);
            try (Transport transport = transportSession.getTransport("smtp")) {
                if (relayUsername != null && !relayUsername.isEmpty() && targetHost.equals(relayHost)) {
                    transport.connect(relayHost, relayUsername, relayPassword);
                } else {
                    transport.connect();
                }
                transport.sendMessage(message, message.getAllRecipients());
            }

            // 4. Update Status
            item.setStatus("SENT");
            item.setErrorMessage(null);
            mailQueueRepository.save(item);
            log.info("Email sent successfully. Item ID: {}", item.getId());

        } catch (Exception e) {
            log.error("Failed to send email. Item ID: {}", item.getId(), e);
            handleFailure(item, e.getMessage());
        }
    }

    private void handleFailure(OutboundQueue item, String errorMessage) {
        item.setRetryCount(item.getRetryCount() + 1);
        item.setErrorMessage(errorMessage);
        if (item.getRetryCount() >= maxRetries) {
            item.setStatus("FAILED");
        } else {
            item.setStatus("RETRY");
            // Exponential backoff: backoffFactor^retryCount * 1 minute
            long delayMinutes = (long) Math.pow(backoffFactor, item.getRetryCount());
            item.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
        mailQueueRepository.save(item);
    }
}
