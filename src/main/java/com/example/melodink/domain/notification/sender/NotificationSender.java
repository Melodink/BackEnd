package com.example.melodink.domain.notification.sender;

import com.example.melodink.domain.notification.dto.response.NotificationResponse;

import java.util.UUID;

public interface NotificationSender {

    void send( UUID userPublicId, NotificationResponse response);
}
