package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attachments")
@Data
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    private String filename;
    private String contentType;

    @Lob
    private byte[] data;
}
