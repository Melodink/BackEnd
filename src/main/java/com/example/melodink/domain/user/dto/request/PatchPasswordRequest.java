package com.example.melodink.domain.user.dto.request;

public record PatchPasswordRequest(
        String currentPassword,
        String newPassword
) {
}
