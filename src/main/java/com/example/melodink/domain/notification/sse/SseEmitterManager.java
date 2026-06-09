package com.example.melodink.domain.notification.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    // hashtable 클래스 단점 보완 및 multi thread 환경에서 사용 가능한 hash map
    private final Map<UUID, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userPublicId){

        SseEmitter sseEmitter = new SseEmitter(60l * 60l * 1000l);

        sseEmitters.put(userPublicId, sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitters.remove(userPublicId));

        sseEmitter.onTimeout(() -> sseEmitters.remove(userPublicId));

        sseEmitter.onError(error -> sseEmitters.remove(userPublicId));

        return sseEmitter;
    }

    public SseEmitter get(UUID userPublicId){
        return sseEmitters.get(userPublicId);
    }

}
