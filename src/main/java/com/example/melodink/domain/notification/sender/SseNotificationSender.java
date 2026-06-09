package com.example.melodink.domain.notification.sender;

import com.example.melodink.domain.notification.dto.response.NotificationResponse;
import com.example.melodink.domain.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {

    private final SseEmitterManager sseEmitterManager;

    public void send(UUID userPublicId, NotificationResponse response) {
        SseEmitter sseEmitter = sseEmitterManager.get(userPublicId);

        if(sseEmitter == null){
            return;
        }

        try{
            sseEmitter.send(SseEmitter.event()
                    .name("notification")
                    .data(response));
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
    }
}
