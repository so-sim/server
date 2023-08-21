package com.sosim.server.notification.util;

import com.sosim.server.common.auditing.Status;
import com.sosim.server.common.response.Response;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.event.domain.entity.Situation;
import com.sosim.server.event.domain.repository.EventRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.SUCCESS_SEND_NOTIFICATION;
import static com.sosim.server.common.response.ResponseCode.SUCCESS_SUBSCRIBE;
import static com.sosim.server.event.domain.entity.Situation.CHECK;
import static com.sosim.server.notification.domain.entity.ContentType.*;

@RequiredArgsConstructor
@Component
public class NotificationUtil {
    public final static String NOTIFICATION_NAME = "notification";

    private final NotificationRepository notificationRepository;

    private final GroupRepository groupRepository;

    private final EventRepository eventRepository;

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
    @Scheduled(cron = "0 */30 * * * *") //30분 마다
    public void sendRegularNotification() {
        List<Group> groups = findNowReservedGroups();
        List<Event> events = eventRepository.findNoneEventsInGroups(groups);

        List<Notification> notifications = makeReservedNotifications(events);
        sendNotifications(notifications);
        notificationRepository.saveAll(notifications);
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
        events.sort(Comparator.comparing(e -> e.getUser().getId()));
        Group group = events.get(0).getGroup();
        long userId = events.get(0).getUser().getId();
        List<Notification> notifications = new ArrayList<>();
        List<Long> eventIdList = new ArrayList<>();

        for (Event event : events) {
            if (!event.isMine(userId)) {
                notifications.add(makeModifySituationNotification(userId, group, preSituation, newSituation, eventIdList));
                userId = event.getUser().getId();
                eventIdList = new ArrayList<>();
            }
            eventIdList.add(event.getId());
        }
        notifications.add(makeModifySituationNotification(userId, group, preSituation, newSituation, eventIdList));

        notificationRepository.saveAll(notifications);
        sendNotifications(notifications);
    }

    @Async
    @Transactional
    public void modifyNickname(long groupId, String preNickname, String newNickname) {
        notificationRepository.updateAllNicknameByGroupIdAndNickname(groupId, preNickname, newNickname);
    }
  
    @Async
    @Transactional
    public void modifyGroupTitle(long groupId, String newTitle) {
        notificationRepository.updateAllGroupTitleByGroupId(groupId, newTitle);
    }

    @Async
    @Transactional
    public void lockNotification(String nickname, long groupId) {
        notificationRepository.updateAllStatusByNicknameAndGroupId(Status.LOCK, nickname, groupId);
    }

    private List<Group> findNowReservedGroups() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Group> groups = groupRepository.findToNextSendDateTime(now);

        for (Group group : groups) {
            group.setNextSendNotificationTime();
        }

        return groups;
    }

    private Notification makeChangeAdminNotification(Group group, Participant participant) {
        return Notification.toEntity(participant.getUser().getId(), group, Content.create(CHANGE_ADMIN, group.getAdminParticipant().getNickname()));
    }

    private Notification makeModifySituationNotification(long id, Group group, Situation preSituation, Situation newSituation, List<Long> eventIdList) {
        return Notification.toEntity(id, group,
                Content.create(getSituationType(newSituation), preSituation.getComment(), newSituation.getComment()),
                eventIdList);
    }

    private List<Notification> makeReservedNotifications(List<Event> events) {
        List<Notification> notificationList = new ArrayList<>();
        List<Long> eventIdList = new ArrayList<>();
        long currentUserId = events.get(0).getUser().getId();
        Group currentGroup = events.get(0).getGroup();

        for (Event event : events) {
            if (!(event.isMine(currentUserId) && event.included(currentGroup))) {
                notificationList.add(Notification
                        .toEntity(currentUserId, currentGroup, Content.create(PAYMENT_DATE), eventIdList));

                currentUserId = event.getUser().getId();
                currentGroup = event.getGroup();
                eventIdList = new ArrayList<>();
            }
            if (currentGroup.hasParticipant(currentUserId)) {
                eventIdList.add(event.getId());
            }
        }
        return notificationList;
        /*
        * event리스트 순회
        * userId, groupId로 정렬되어있음
        * 둘중 하나가 달라지면 바로 알림 생성
        * eventIDList 초기화
        * group, userid 초기화
        * group에 현재 currentuserId포함 안되면 리스트에 넣지 말기
        *
        * */
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

}
