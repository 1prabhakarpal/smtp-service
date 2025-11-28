package com.example.api.controller;

import com.example.common.entity.Email;
import com.example.common.repository.EmailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @GetMapping
    public ResponseEntity<List<Email>> getEmails(@RequestParam("recipient") String recipient) {
        // In a real app, get recipient from authenticated user context
        return ResponseEntity.ok(emailRepository.findByRecipient(recipient));
    }
}
