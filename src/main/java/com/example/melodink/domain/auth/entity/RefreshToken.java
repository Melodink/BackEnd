package com.example.melodink.domain.auth.entity;

import com.example.melodink.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID publicId;

    /** SHA-256 토큰 해시 (원문 저장 금지) */
    @Column(length = 64, unique = true, nullable = false)
    private String tokenHash;

    private String expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}

