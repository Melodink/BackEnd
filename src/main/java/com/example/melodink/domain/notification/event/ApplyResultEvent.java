package com.example.melodink.domain.notification.event;

public record ApplyResultEvent(
        Long artistId,
        Long directorId,
        Boolean accepted,
        Long postId
) {
}
