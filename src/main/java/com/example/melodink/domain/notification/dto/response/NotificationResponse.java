package com.example.melodink.domain.notification.dto.response;

import com.example.melodink.domain.notification.entity.Notification;
import com.example.melodink.domain.notification.entity.NotificationType;
import com.example.melodink.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        Long id,
        User user,
        NotificationType type,
        String message,
        UUID targetPublicId,
        LocalDateTime createdAt
) {
    public NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUser(),
            notification.getType(),
            notification.getMessage(),
            notification.getTargetPublicId(),
            notification.getCreatedAt()
        );
    }
}
