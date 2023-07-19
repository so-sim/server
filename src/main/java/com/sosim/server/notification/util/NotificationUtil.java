package com.sosim.server.notification.util;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.Notification;
import com.sosim.server.notification.NotificationRepository;
import com.sosim.server.notification.dto.response.NotificationResponse;
import com.sosim.server.participant.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_GROUP;
import static com.sosim.server.common.response.ResponseCode.SUCCESS_SEND_NOTIFICATION;
import static com.sosim.server.notification.Content.PAYMENT_DATE;
import static com.sosim.server.notification.Content.create;

@RequiredArgsConstructor
@Component
public class NotificationUtil {
    public final static String NOTIFICATION_NAME = "notification";

    private final NotificationRepository notificationRepository;

    private final GroupRepository groupRepository;

    private final SseEmitterRepository sseEmitterRepository;

    @Transactional
    @Scheduled(cron = "0 */30 * * * *") //30분 마다
    public void sendRegularNotification() {
        List<Notification> reservedNotifications = notificationRepository.findReservedNotifications();
        reservedNotifications.forEach(this::sendReservedNotification);

        if (!reservedNotifications.isEmpty()) {
            Set<Long> groupIdSet = makeGroupIdSet(reservedNotifications);
            groupIdSet.forEach(this::reserveNextRegularNotifications);
        }
    }

    @Async
    @Transactional
    public void reserveNextRegularNotifications(long groupId) {
        Group group = getGroup(groupId);
        reserveNextRegularNotifications(group);
    }

    @Async
    @Transactional
    public void reserveNextRegularNotifications(Group group) {
        List<Notification> notifications = new ArrayList<>();
        group.getParticipantList()
                .forEach(p -> notifications.add(makeReservedNotification(group, p)));
        notificationRepository.saveAll(notifications);
    }

    @Async
    @Transactional
    public void changeReservedRegularNotifications(Group group) {
        notificationRepository.deleteReservedNotifications(group.getId());
        reserveNextRegularNotifications(group);
    }

    private Set<Long> makeGroupIdSet(List<Notification> reservedNotifications) {
        return reservedNotifications.stream()
                .map(Notification::getGroupId)
                .collect(Collectors.toSet());
    }

    private Notification makeReservedNotification(Group group, Participant p) {
        return Notification.builder()
                .userId(p.getUser().getId())
                .groupId(group.getId())
                .groupTitle(group.getTitle())
                .content(create(PAYMENT_DATE, null))
                .reserved(true)
                .sendDateTime(group.getNextNotifyDateTime())
                .build();
    }

    private void sendReservedNotification(Notification notification) {
        Response<?> response = makeNotificationResponse(notification);
        sendToClient(notification, response);
        notification.sendComplete();
    }

    private Response<?> makeNotificationResponse(Notification notification) {
        NotificationResponse notificationResponse = NotificationResponse.toDto(notification);
        return Response.create(SUCCESS_SEND_NOTIFICATION, notificationResponse);
    }

    private void sendToClient(Notification notification, Response<?> response) {
        SseEmitter sseEmitter = sseEmitterRepository.findByUserId(notification.getUserId());
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(NOTIFICATION_NAME)
                    .data(response));
        } catch (NullPointerException | IOException e) {
            sseEmitterRepository.deleteById(notification.getUserId());
        }
    }

    private Group getGroup(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }
}