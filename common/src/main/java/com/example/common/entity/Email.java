package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipients;

    @Column(length = 998)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "received_at")
    private LocalDateTime receivedAt = LocalDateTime.now();
}
