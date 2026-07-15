package com.example.melodink.domain.notification.event;

public record JobApplyEvent(
        Long directorId,
        Long userId,
        String userNickname,
        Long postId
) {
}
