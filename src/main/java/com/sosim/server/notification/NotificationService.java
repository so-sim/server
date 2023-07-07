package com.sosim.server.notification;

import com.sosim.server.common.response.Response;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.dto.response.MyNotificationsResponse;
import com.sosim.server.notification.dto.response.NotificationCountResponse;
import com.sosim.server.notification.dto.response.NotificationResponse;
import com.sosim.server.notification.util.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public SseEmitter subscribe(long userId) {
        SseEmitter sseEmitter = sseEmitterRepository.save(userId);
        long userNotificationCount = notificationRepository.countByUserIdBetweenMonth(userId, LocalDateTime.now().minusMonths(3));
        NotificationCountResponse notificationCount = NotificationCountResponse.toDto(userNotificationCount);
        Response<?> subscribeResponse = Response.create(SUCCESS_SUBSCRIBE, notificationCount);
        sendToClient(sseEmitter, userId, "subscribe", subscribeResponse);

        return sseEmitter;
    }

    public void sendNotification(List<Long> receiverUserIdList, long groupId, String groupTitle, Content content) {
        Notification notification = saveAllNotification(receiverUserIdList, groupId, content);

        NotificationResponse notificationResponse = NotificationResponse.toDto(notification, groupTitle);
        Response<?> response = Response.create(SUCCESS_SEND_NOTIFICATION, notificationResponse);
        receiverUserIdList.forEach(id ->
                sendToClient(sseEmitterRepository.findByUserId(id), id, "notification", response)
        );
    }

    public void sendNotification(long receiverUserId, long groupId, String groupTitle, Content content) {
        Notification notification = saveNotification(receiverUserId, groupId, content);

        NotificationResponse notificationResponse = NotificationResponse.toDto(notification, groupTitle);
        Response<?> response = Response.create(SUCCESS_SEND_NOTIFICATION, notificationResponse);
        sendToClient(sseEmitterRepository.findByUserId(receiverUserId), receiverUserId, "notification", response);
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

    @Transactional
    private Notification saveNotification(long receiverUserId, long groupId, Content content) {
        Notification notification = Notification.toEntity(receiverUserId, groupId, content);
        return notificationRepository.save(notification);
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

    @Transactional
    public void viewAllNotification(long userId) {
        notificationRepository.updateViewByUserId(userId);
    }

    @Transactional(readOnly = true)
    public MyNotificationsResponse getMyNotifications(long userId, Pageable pageable) {
        Slice<Notification> myNotifications = notificationRepository.findByUserIdAndCreateDateGreaterThan(userId, LocalDateTime.now().minusMonths(3), pageable);
        List<NotificationResponse> notificationDtoList = toNotificationResponseList(myNotifications);

        return MyNotificationsResponse.toDto(notificationDtoList, myNotifications.hasNext());
    }

    private List<NotificationResponse> toNotificationResponseList(Slice<Notification> myNotifications) {
        List<Long> groupIdList = myNotifications.stream()
                .map(Notification::getGroupId).collect(Collectors.toList());
        Map<Long, String> groupTitleMap = createGroupTitleMap(groupIdList);
        return myNotifications.stream()
                .map(n -> NotificationResponse.toDto(n, groupTitleMap.get(n.getGroupId())))
                .collect(Collectors.toList());
    }

    private Map<Long, String> createGroupTitleMap(List<Long> groupIdList) {
        Map<Long, String> groupTitleMap = new HashMap<>();
        List<Group> groupList = groupRepository.findAllById(groupIdList);
        groupList.forEach(g -> groupTitleMap.put(g.getId(), g.getTitle()));
        return groupTitleMap;
    }
}
