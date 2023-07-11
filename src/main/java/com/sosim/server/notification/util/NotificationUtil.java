package com.sosim.server.notification.util;

import com.sosim.server.common.response.Response;
import com.sosim.server.notification.Notification;
import com.sosim.server.notification.NotificationRepository;
import com.sosim.server.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static com.sosim.server.common.response.ResponseCode.SUCCESS_SEND_NOTIFICATION;

@RequiredArgsConstructor
@Component
public class NotificationUtil {
    public final static String NOTIFICATION_NAME = "notification";

    private final NotificationRepository notificationRepository;

    private final SseEmitterRepository sseEmitterRepository;

    @Transactional
    @Scheduled(cron = "0 */30 * * * *") //30분 마다
    public void sendRegularNotification() {
        List<Notification> reservedNotifications = notificationRepository.findReservedNotifications();

        reservedNotifications.forEach(this::sendReservedNotification);
        //TODO 다음 알림 생성하기
    }

    private void sendReservedNotification(Notification notification) {
        Response<?> response = makeNotificationResponse(notification);
        sendToClient(notification, response);
    }

    private Response<?> makeNotificationResponse(Notification notification) {
        NotificationResponse notificationResponse = NotificationResponse.toDto(notification);
        Response<?> response = Response.create(SUCCESS_SEND_NOTIFICATION, notificationResponse);
        return response;
    }

    private void sendToClient(Notification notification, Response<?> response) {
        SseEmitter sseEmitter = sseEmitterRepository.findByUserId(notification.getUserId());
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(NOTIFICATION_NAME)
                    .data(response));
            notification.sentComplete();
        } catch (IOException e) {
            sseEmitterRepository.deleteById(notification.getUserId());
        }
    }

}
