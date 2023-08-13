package com.sosim.server.notification.util;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.event.domain.entity.Situation;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.notification.domain.entity.Content;
import com.sosim.server.notification.domain.entity.Notification;
import com.sosim.server.notification.domain.repository.NotificationRepository;
import com.sosim.server.notification.dto.response.NotificationCountResponse;
import com.sosim.server.notification.dto.response.NotificationResponse;
import com.sosim.server.participant.domain.entity.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.*;
import static com.sosim.server.event.domain.entity.Situation.CHECK;
import static com.sosim.server.notification.domain.entity.ContentType.*;

@RequiredArgsConstructor
@Component
public class NotificationUtil {
    public final static String NOTIFICATION_NAME = "notification";

    private final NotificationRepository notificationRepository;

    private final GroupRepository groupRepository;

    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter subscribe(long userId) {
        SseEmitter sseEmitter = sseEmitterRepository.save(userId);
        long userNotificationCount = notificationRepository.countByUserIdBetweenMonth(userId, LocalDateTime.now().minusMonths(3));
        NotificationCountResponse notificationCount = NotificationCountResponse.toDto(userNotificationCount);
        Response<?> subscribeResponse = Response.create(SUCCESS_SUBSCRIBE, notificationCount);
        sendToClient(sseEmitter, userId, "subscribe", subscribeResponse);

        return sseEmitter;
    }

    @Transactional
    @Scheduled(cron = "* */30 * * * *") //30분 마다
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
    public void reserveNextRegularNotifications(Group group) {
        List<Notification> notifications = new ArrayList<>();
        group.getParticipantList()
                .forEach(p -> notifications.add(makeReservedNotification(group, p)));
        notificationRepository.saveAll(notifications);
    }

    @Async
    @Transactional
    public void reserveNextRegularNotifications(long groupId) {
        Group group = getGroup(groupId);
        reserveNextRegularNotifications(group);
    }

    @Async
    @Transactional
    public void changeReservedRegularNotifications(Group group) {
        notificationRepository.deleteReservedNotifications(group.getId());
        reserveNextRegularNotifications(group);
    }

    @Async
    @Transactional
    public void sendNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            sendNotification(notification);
        }
    }

    @Async
    @Transactional
    public void sendNotification(Notification notification) {
        Response<?> response = makeNotificationResponse(notification);
        sendToClient(notification, response);
    }

    @Async
    @Transactional
    public void sendModifyAdminNotification(Group group) {
        //TODO: 총무 변경 시 알림을 기존 총무, 새 총무한테도 보내야 하는지?
        List<Notification> notifications = group.getParticipantList().stream()
                .map(participant -> makeChangeAdminNotification(group, participant))
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
        sendNotifications(notifications);
    }

    @Async
    @Transactional
    public void sendCheckSituationNotification(Group group, List<Event> events) {
        long adminId = group.getAdminParticipant().getUser().getId();
        String senderNickname = events.get(0).getNickname();
        Content content = Content.create(CHANGE_CHECK_SITUATION, senderNickname, CHECK.getComment());
        List<Long> eventIdList = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Notification notification = Notification.toEntity(adminId, group, content, eventIdList);
        notificationRepository.save(notification);
        sendNotification(notification);
    }

    @Async
    @Transactional
    public void sendModifySituationNotifications(List<Event> events, Situation preSituation, Situation newSituation) {
        List<Notification> notifications = events.stream()
                .map(event -> makeModifySituationNotification(event, preSituation, newSituation))
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
        sendNotifications(notifications);
    }

    @Async
    @Transactional(propagation = Propagation.MANDATORY)
    public void modifyNickname(long groupId, String preNickname, String newNickname) {
        notificationRepository.updateAllNicknameByGroupIdAndNickname(groupId, preNickname, newNickname);
    }
  
    @Async
    @Transactional(propagation = Propagation.MANDATORY)
    public void modifyGroupTitle(long groupId, String newTitle) {
        notificationRepository.updateAllGroupTitleByGroupId(groupId, newTitle);
    }

    private Notification makeChangeAdminNotification(Group group, Participant participant) {
        return Notification.toEntity(participant.getUser().getId(), group, Content.create(CHANGE_ADMIN, group.getAdminParticipant().getNickname()));
    }

    private Notification makeModifySituationNotification(Event event, Situation preSituation, Situation newSituation) {
        return Notification.toEntity(event.getUser().getId(),
                        event.getGroup(),
                        Content.create(getSituationType(newSituation), preSituation.getComment(), newSituation.getComment()));
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
                .content(Content.create(PAYMENT_DATE))
                .reserved(true)
                .sendDateTime(group.getNextNotifyDateTime())
                .build();
    }

    private void sendReservedNotification(Notification notification) {
        sendNotification(notification);
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

    private void sendToClient(SseEmitter sseEmitter, long userId, String notificationName, Response<?> notificationData) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(notificationName)
                    .data(notificationData));
        } catch (IOException e) {
            sseEmitterRepository.deleteById(userId);
        }
    }

    private Group getGroup(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

}
