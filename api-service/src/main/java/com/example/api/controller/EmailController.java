package com.example.api.controller;

import com.example.api.dto.EmailRequest;
import com.example.common.entity.Email;
import com.example.common.entity.OutboundQueue;
import com.example.common.entity.User;
import com.example.common.repository.EmailRepository;
import com.example.common.repository.OutboundQueueRepository;
import com.example.common.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailRepository emailRepository;
    private final OutboundQueueRepository outboundQueueRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @GetMapping
    public ResponseEntity<Page<Email>> getEmails(
            @RequestParam(value = "folderId", required = false) Long folderId,
            Pageable pageable,
            Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Page<Email> emails;
        if (folderId != null) {
            emails = emailRepository.findByUser_IdAndFolder_Id(user.getId(), folderId, pageable);
        } else {
            emails = emailRepository.findByUser_Id(user.getId(), pageable);
        }
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmail(@PathVariable("id") Long id, Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!email.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(email);
    }

    @PostMapping
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest emailRequest, Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        try {
            // Create MimeMessage to get bytes
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(user.getUsername() + "@devprabhakar.in");
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), true); // Assume HTML for now

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mimeMessage.writeTo(os);
            byte[] emailData = os.toByteArray();

            // Save to OutboundQueue
            OutboundQueue queueItem = new OutboundQueue();
            queueItem.setSender(user.getUsername());
            queueItem.setRecipient(emailRequest.getTo());
            queueItem.setEmailData(emailData);
            queueItem.setStatus("PENDING");

            outboundQueueRepository.save(queueItem);

            return ResponseEntity.ok("Email queued successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to queue email: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmail(@PathVariable("id") Long id, @RequestBody Email updates,
            Authentication authentication) {
        // Implement update logic (e.g. mark as read)
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmail(@PathVariable("id") Long id, Authentication authentication) {
        emailRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<?> moveEmail(@PathVariable("id") Long id, @RequestParam("folderId") Long folderId,
            Authentication authentication) {
        // Implement move logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> addAttachment(@PathVariable("id") Long id, Authentication authentication) {
        // Implement attachment logic
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/attachments/{aid}")
    public ResponseEntity<?> getAttachment(@PathVariable("id") Long id, @PathVariable("aid") Long aid,
            Authentication authentication) {
        // Implement get attachment logic
        return ResponseEntity.ok().build();
    }
}
