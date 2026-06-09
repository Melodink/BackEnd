package com.example.melodink.domain.notification.controller;

import com.example.melodink.domain.notification.dto.response.NotificationResponse;
import com.example.melodink.domain.notification.service.NotificationService;
import com.example.melodink.domain.notification.sse.SseEmitterManager;
import com.example.melodink.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @GetMapping
    public SseEmitter getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        UUID userPublicId = userDetails.getUser().getPublicId();

        SseEmitter emitter = sseEmitterManager.connect(userPublicId);

        try{
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

}
