package com.sosim.server.notification;

import com.sosim.server.common.response.Response;
import com.sosim.server.notification.dto.response.NotificationCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter subscribe(long userId) {
        SseEmitter sseEmitter = sseEmitterRepository.save(userId);
        // TODO : 유저별 알림 개수
        NotificationCountResponse notificationCount = NotificationCountResponse.toDto(0);
        Response<?> subscribeResponse = Response.create(SUCCESS_SUBSCRIBE, notificationCount);
        sendToClient(sseEmitter, userId, "subscribe", subscribeResponse);

        return sseEmitter;
    }

    private void sendToClient(SseEmitter sseEmitter, long userId, String notificationName, Response<?> notificationData) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(notificationName)
                    .data(notificationData));
        } catch (IOException e) {
            sseEmitterRepository.deleteById(userId);
        }
    }
}
