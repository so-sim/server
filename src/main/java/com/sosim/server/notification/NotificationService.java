package com.sosim.server.notification;

import com.sosim.server.common.response.Response;
import com.sosim.server.notification.dto.response.NotificationCountResponse;
import com.sosim.server.notification.dto.response.NotificationResponse;
import com.sosim.server.notification.util.SseEmitterRepository;
import com.sosim.server.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;
    private final ParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    public SseEmitter subscribe(long userId) {
        SseEmitter sseEmitter = sseEmitterRepository.save(userId);
        long userNotificationCount = notificationRepository.countByUserIdBetweenMonth(userId, LocalDateTime.now().minusMonths(3));
        NotificationCountResponse notificationCount = NotificationCountResponse.toDto(userNotificationCount);
        Response<?> subscribeResponse = Response.create(SUCCESS_SUBSCRIBE, notificationCount);
        sendToClient(sseEmitter, userId, "subscribe", subscribeResponse);

        return sseEmitter;
    }

    public void sendNotification(long groupId, String groupTitle, Content content) {
        List<Long> receiverUserIdList = getReceiverUserIdList(groupId);
        Notification notification = saveAllNotification(receiverUserIdList, groupId, content);

        NotificationResponse notificationResponse = NotificationResponse.toDto(notification, groupTitle);
        Response<?> response = Response.create(SUCCESS_SEND_NOTIFICATION, notificationResponse);
        receiverUserIdList.forEach(id ->
                sendToClient(sseEmitterRepository.findByUserId(id), id, "notification", response)
        );
    }

    @Transactional
    private Notification saveAllNotification(List<Long> receiverUserIdList, long groupId, Content content) {
        List<Notification> notificationList = new ArrayList<>();
        receiverUserIdList.forEach(id ->
                notificationList.add(Notification.toEntity(id, groupId, content))
        );
        notificationRepository.saveAll(notificationList);
        return notificationList.get(0);
    }

    @Transactional(readOnly = true)
    private List<Long> getReceiverUserIdList(long groupId) {
        return participantRepository.getReceiverUserIdList(groupId);
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
