package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email_id")
    private Email email;

    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(columnDefinition = "BYTEA")
    private byte[] data;

    @Column(name = "size_bytes")
    private Long sizeBytes;
}
