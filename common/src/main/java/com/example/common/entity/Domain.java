package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "domains")
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "dkim_private_key", columnDefinition = "TEXT")
    private String dkimPrivateKey;

    @Column(name = "dkim_selector")
    private String dkimSelector;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
