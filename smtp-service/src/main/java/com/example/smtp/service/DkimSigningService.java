package com.example.smtp.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@Slf4j
public class DkimSigningService {

    @Value("${dkim.signing.domain}")
    private String signingDomain;

    @Value("${dkim.signing.selector}")
    private String selector;

    @Value("${dkim.private.key.path}")
    private String privateKeyPath;

    public MimeMessage signMessage(byte[] emailData) {
        try {
            // Convert byte array to Email object
            Email email = EmailConverter
                    .mimeMessageToEmail(new MimeMessage(null, new java.io.ByteArrayInputStream(emailData)));

            // Build a new Email with DKIM signing
            File privateKeyFile = new File(privateKeyPath);
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

            Email signedEmail = EmailBuilder.copying(email)
                    .signWithDomainKey(privateKeyBytes, signingDomain, selector, null)
                    .buildEmail();

            // Convert back to MimeMessage
            return EmailConverter.emailToMimeMessage(signedEmail);

        } catch (Exception e) {
            log.error("Failed to sign message with DKIM", e);
            throw new RuntimeException("DKIM signing failed", e);
        }
    }
}
