package com.artvault.artvaultbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "artvault_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    // User role: USER or ADMIN
    @Column(nullable = false, length = 20)
    private String role = "USER";

    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    // Get user ID
    public Long getId() {
        return id;
    }

    // Get username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Get email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Get password hash
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Get user role
    public String getRole() {
        return role;
    }

    // Set user role
    public void setRole(String role) {
        this.role = role;
    }

    // Get account creation date
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}