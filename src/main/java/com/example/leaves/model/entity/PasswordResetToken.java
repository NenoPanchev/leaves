package com.example.leaves.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens", schema = "public")
@Getter
@Setter
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token")
    private String token;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, UserEntity user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusSeconds(EXPIRATION);
    }

    public PasswordResetToken() {

    }

    @PreRemove
    public void dismissUser() {
        this.user = null;
    }
}