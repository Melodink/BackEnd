package com.example.melodink.domain.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseEmitterManager {

    private static final long SSE_TIMEOUT = 60L * 60L * 1000L;

    /**
     * userPublicId
     *      └── emitter1
     *      └── emitter2
     *      └── emitter3
     */
    private final ConcurrentHashMap<
            UUID,
            CopyOnWriteArrayList<SseEmitter>
            > sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userPublicId) {

        SseEmitter emitter =
                new SseEmitter(SSE_TIMEOUT);

        sseEmitters
                .computeIfAbsent(
                        userPublicId,
                        key -> new CopyOnWriteArrayList<>()
                )
                .add(emitter);

        emitter.onCompletion(
                () -> removeEmitter(userPublicId, emitter)
        );

        emitter.onTimeout(
                () -> removeEmitter(userPublicId, emitter)
        );

        emitter.onError(
                error -> removeEmitter(userPublicId, emitter)
        );

        sendConnectEvent(emitter);

        return emitter;
    }

    public void send(UUID userPublicId, Object data) {

        List<SseEmitter> emitters =
                sseEmitters.get(userPublicId);

        if (emitters == null) {
            return;
        }

        emitters.forEach(emitter -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("notification")
                                .data(data)
                );
            } catch (IOException e) {
                removeEmitter(userPublicId, emitter);
            }
        });
    }

    private void removeEmitter(UUID userPublicId, SseEmitter emitter) {

        List<SseEmitter> emitters =
                sseEmitters.get(userPublicId);

        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);

        if (emitters.isEmpty()) {
            sseEmitters.remove(userPublicId);
        }

        log.debug(
                "[SSE] emitter removed. user={}",
                userPublicId
        );
    }

    private void sendConnectEvent(
            SseEmitter emitter
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connect")
                            .data("connected")
            );
        } catch (IOException ignored) {
        }
    }

    /**
     * 30초마다 heartbeat
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {

        sseEmitters.forEach((userId, emitters) -> {

            emitters.forEach(emitter -> {

                try {
                    emitter.send(
                            SseEmitter.event()
                                    .name("ping")
                                    .data("alive")
                    );
                } catch (Exception e) {

                    removeEmitter(
                            userId,
                            emitter
                    );
                }
            });
        });
    }

    public int connectedUserCount() {
        return sseEmitters.size();
    }

    public int connectedEmitterCount() {

        return sseEmitters.values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }
}
