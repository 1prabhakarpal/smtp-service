package com.example.smtp;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class SimpleMessageHandler implements MessageHandler {

    private final MessageContext context;
    private final com.example.common.repository.EmailRepository emailRepository;
    private String from;
    private String recipient;

    public SimpleMessageHandler(MessageContext context, com.example.common.repository.EmailRepository emailRepository) {
        this.context = context;
        this.emailRepository = emailRepository;
    }

    @Override
    public void from(String from) throws RejectException {
        this.from = from;
        System.out.println("FROM: " + from);
    }

    @Override
    public void recipient(String recipient) throws RejectException {
        this.recipient = recipient;
        System.out.println("RECIPIENT: " + recipient);
    }

    @Override
    public String data(InputStream data) throws RejectException, IOException {
        String emailBody = new BufferedReader(
                new InputStreamReader(data, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        System.out.println("DATA:");
        System.out.println(emailBody);

        com.example.common.entity.Email email = com.example.common.entity.Email.builder()
                .sender(from)
                .recipient(recipient)
                .body(emailBody)
                .subject("No Subject") // Simple parsing for now
                .build();

        emailRepository.save(email);
        System.out.println("Saved email to DB: " + email);

        return null;
    }

    @Override
    public void done() {
        System.out.println("Finished processing email from " + from + " to " + recipient);
    }
}
