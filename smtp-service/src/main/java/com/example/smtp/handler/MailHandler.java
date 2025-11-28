package com.example.smtp.handler;

import com.example.common.repository.EmailRepository;
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

    public MailHandler(MessageContext context, EmailRepository emailRepository) {
        this.context = context;
        this.emailRepository = emailRepository;
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8));
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
        return null;
    }

    @Override
    public void done() {
    }
}
