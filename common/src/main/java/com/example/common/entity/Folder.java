package com.example.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "folders", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name", "parent_id"})
})
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;
}
