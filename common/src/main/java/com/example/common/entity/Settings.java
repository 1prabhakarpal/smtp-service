package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "settings")
@Data
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String theme;
    private boolean notificationsEnabled;
    private String signature;
}
