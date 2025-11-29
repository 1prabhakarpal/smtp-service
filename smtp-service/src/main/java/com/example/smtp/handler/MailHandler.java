package com.example.smtp.handler;

import com.example.common.entity.OutboundQueue;
import com.example.common.repository.EmailRepository;
import com.example.common.repository.MailQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MailHandler implements MessageHandler {

    private final MessageContext context;
    private final EmailRepository emailRepository;
    private final MailQueueRepository mailQueueRepository;

    public MailHandler(MessageContext context, EmailRepository emailRepository,
            MailQueueRepository mailQueueRepository) {
        this.context = context;
        this.emailRepository = emailRepository;
        this.mailQueueRepository = mailQueueRepository;
    }

    @Override
    public void from(String from) throws RejectException {
    }

    @Override
    public void recipient(String recipient) throws RejectException {
    }

    @Override
    public String data(InputStream data) throws RejectException, IOException {
        log.info("DATA received, parsing message manually...");

        byte[] rawData = data.readAllBytes();
        java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(rawData);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        StringBuilder bodyBuilder = new StringBuilder();
        boolean inBody = false;
        String line;

        while ((line = reader.readLine()) != null) {
            if (!inBody) {
                if (line.isEmpty()) {
                    inBody = true;
                } else {
                    int colonIndex = line.indexOf(':');
                    if (colonIndex > 0) {
                        String key = line.substring(0, colonIndex).trim().toLowerCase();
                        String value = line.substring(colonIndex + 1).trim();
                        headers.put(key, value);
                    }
                }
            } else {
                bodyBuilder.append(line).append("\n");
            }
        }

        String subject = headers.getOrDefault("subject", "No Subject");
        String from = headers.getOrDefault("from", "Unknown");
        String to = headers.getOrDefault("to", "Unknown");
        String body = bodyBuilder.toString().trim();

        log.info("Parsed Email (Manual) - Subject: {}, From: {}, To: {}", subject, from, to);

        com.example.common.entity.Email email = com.example.common.entity.Email.builder()
                .sender(from)
                .recipient(to)
                .subject(subject)
                .body(body)
                .build();

        emailRepository.save(email);
        log.info("Email saved to database.");

        // Save to Outbound Queue
        OutboundQueue queueItem = new OutboundQueue();
        queueItem.setSender(from);
        queueItem.setRecipient(to);
        queueItem.setEmailData(rawData);
        queueItem.setStatus("PENDING");
        queueItem.setRetryCount(0);
        queueItem.setNextRetryAt(java.time.LocalDateTime.now());
        queueItem.setCreatedAt(java.time.LocalDateTime.now());

        mailQueueRepository.save(queueItem);
        log.info("Email added to Outbound Queue for delivery.");

        return null;
    }

    @Override
    public void done() {
    }
}
