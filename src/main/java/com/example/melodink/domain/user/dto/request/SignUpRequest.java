package com.example.melodink.domain.user.dto.request;

import com.example.melodink.domain.user.entity.Role;
import com.example.melodink.domain.user.entity.User;

import java.time.LocalDateTime;

public record SignUpRequest (
        String name,

        String email,

        String verifiedEmail,

        String password,

        String nickname
){

    public User toEntity(String name, String email, String verifiedEmail, String password, String nickname, String pictureUrl, Role role) {
        return User.builder()
                .name(name)
                .email(email)
                .verifiedEmail(verifiedEmail)
                .password(password)
                .nickname(nickname)
                .pictureUrl(pictureUrl)
                .emailVerifiedAt(LocalDateTime.now())
                .role(role)
                .build();
    }
}
