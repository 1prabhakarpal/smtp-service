package com.example.smtp.service;

import com.example.smtp.entity.OutboundEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DnsService dnsService;
    private final DkimService dkimService;

    @Value("${smtp.relay.host:}")
    private String relayHost;

    @Value("${smtp.relay.port:25}")
    private int relayPort;

    @Value("${smtp.helo.domain:localhost}")
    private String heloDomain;

    public void deliver(OutboundEmail email) throws Exception {
        log.info("Attempting delivery for email ID: {} to {}", email.getId(), email.getRecipient());

        String targetHost = relayHost;
        int targetPort = relayPort;

        if (targetHost == null || targetHost.isEmpty()) {
            String domain = getDomainFromRecipient(email.getRecipient());
            List<String> mxRecords = dnsService.getMxRecords(domain);
            if (mxRecords.isEmpty()) {
                throw new MessagingException("No MX records found for domain: " + domain);
            }
            targetHost = mxRecords.get(0); // Use highest priority
            targetPort = 25;
            log.debug("Resolved MX record: {} for domain: {}", targetHost, domain);
        } else {
            log.debug("Using relay host: {}:{}", targetHost, targetPort);
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", targetHost);
        props.put("mail.smtp.port", String.valueOf(targetPort));
        props.put("mail.smtp.localhost", heloDomain);
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(email.getSender()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipient()));
        message.setSubject(email.getSubject());
        message.setText(email.getBody()); // Assuming plain text for now

        // Sign with DKIM
        dkimService.sign(message);

        try {
            Transport.send(message);
            log.info("Email ID: {} delivered successfully to {}", email.getId(), targetHost);
        } catch (MessagingException e) {
            log.error("Failed to deliver email ID: {} to {}", email.getId(), targetHost, e);
            throw e;
        }
    }

    private String getDomainFromRecipient(String recipient) {
        int atIndex = recipient.lastIndexOf('@');
        if (atIndex > 0 && atIndex < recipient.length() - 1) {
            return recipient.substring(atIndex + 1);
        }
        throw new IllegalArgumentException("Invalid recipient address: " + recipient);
    }
}
