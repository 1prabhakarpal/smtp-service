package com.example.api.controller;

import com.example.api.dto.EmailRequest;
import com.example.common.entity.Email;
import com.example.common.entity.OutboundQueue;
import com.example.common.entity.User;
import com.example.common.repository.EmailRepository;
import com.example.common.repository.OutboundQueueRepository;
import com.example.common.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Email Management", description = "Operations for managing emails - sending, receiving, organizing and deleting")
@SecurityRequirement(name = "BearerAuth")
public class EmailController {

    private final EmailRepository emailRepository;
    private final OutboundQueueRepository outboundQueueRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @Operation(summary = "List emails", description = "Retrieves a paginated list of emails for the authenticated user. Optionally filter by folder.")
    @ApiResponse(responseCode = "200", description = "Paginated list of emails", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<Email>> getEmails(
            @Parameter(description = "Filter emails by folder ID (optional)") @RequestParam(value = "folderId", required = false) Long folderId,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
            @Parameter(hidden = true) Authentication authentication) {
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

    @Operation(summary = "Get email by ID", description = "Retrieves a single email by its ID. User must own the email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Email.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Email belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmail(
            @Parameter(description = "Email ID", required = true) @PathVariable("id") Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!email.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(email);
    }

    @Operation(summary = "Send email", description = "Queues an email for sending. The email will be processed by the outbound queue service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email queued successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Failed to queue email")
    })
    @PostMapping
    public ResponseEntity<?> sendEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email details", required = true, content = @Content(schema = @Schema(implementation = EmailRequest.class))) @RequestBody EmailRequest emailRequest,
            @Parameter(hidden = true) Authentication authentication) {
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

    @Operation(summary = "Update email", description = "Updates email properties such as marking as read/unread or starring")
    @ApiResponse(responseCode = "200", description = "Email updated successfully")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmail(
            @Parameter(description = "Email ID") @PathVariable("id") Long id,
            @RequestBody Email updates,
            @Parameter(hidden = true) Authentication authentication) {
        // Implement update logic (e.g. mark as read)
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete email", description = "Permanently deletes an email")
    @ApiResponse(responseCode = "200", description = "Email deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmail(
            @Parameter(description = "Email ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) Authentication authentication) {
        emailRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Move email to folder", description = "Moves an email to a different folder")
    @ApiResponse(responseCode = "200", description = "Email moved successfully")
    @PostMapping("/{id}/move")
    public ResponseEntity<?> moveEmail(
            @Parameter(description = "Email ID") @PathVariable("id") Long id,
            @Parameter(description = "Target folder ID") @RequestParam("folderId") Long folderId,
            @Parameter(hidden = true) Authentication authentication) {
        // Implement move logic
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add attachment to email", description = "Uploads and attaches a file to an email")
    @ApiResponse(responseCode = "200", description = "Attachment added successfully")
    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> addAttachment(
            @Parameter(description = "Email ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) Authentication authentication) {
        // Implement attachment logic
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get email attachment", description = "Downloads a specific attachment from an email")
    @ApiResponse(responseCode = "200", description = "Attachment retrieved successfully")
    @GetMapping("/{id}/attachments/{aid}")
    public ResponseEntity<?> getAttachment(
            @Parameter(description = "Email ID") @PathVariable("id") Long id,
            @Parameter(description = "Attachment ID") @PathVariable("aid") Long aid,
            @Parameter(hidden = true) Authentication authentication) {
        // Implement get attachment logic
        return ResponseEntity.ok().build();
    }
}
