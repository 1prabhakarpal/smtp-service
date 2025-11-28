package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outbound_queue")
public class OutboundQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String recipient;

    @Column(name = "email_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] emailData;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt = LocalDateTime.now();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
