package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @Column(name = "is_admin")
    private boolean isAdmin = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
