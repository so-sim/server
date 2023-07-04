package com.sosim.server.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(long userId) {
        SseEmitter sseEmitter = new SseEmitter(60 * 30 * 1000L);
        emitters.put(userId, sseEmitter);
        sseEmitter.onCompletion(() -> deleteById(userId));
        sseEmitter.onTimeout(() -> deleteById(userId));
        return sseEmitter;
    }

    public void deleteById(long userId) {
        emitters.remove(userId);
    }
}
