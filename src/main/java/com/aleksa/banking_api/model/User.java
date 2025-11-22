package com.aleksa.banking_api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(unique = false, nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column( nullable = false,name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    public User(LocalDateTime updatedAt, Long id, String email, String password, LocalDateTime createdAt, UserStatus status) {
        this.updatedAt = updatedAt;
        this.id = id;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.status = status;
    }

    public User() {

    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UserStatus getStatus() {
        return status;
    }
}
