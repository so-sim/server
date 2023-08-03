package com.sosim.server.notification;

import com.sosim.server.notification.dto.response.MyNotificationsResponse;
import com.sosim.server.notification.dto.response.NotificationResponse;
import com.sosim.server.notification.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final NotificationUtil notificationUtil;

    @Transactional(readOnly = true)
    public SseEmitter subscribe(long userId) {
        return notificationUtil.subscribe(userId);
    }

    @Transactional
    public void viewAllNotification(long userId) {
        notificationRepository.updateViewByUserId(userId);
    }

    @Transactional(readOnly = true)
    public MyNotificationsResponse getMyNotifications(long userId, Pageable pageable) {
        Slice<Notification> myNotifications = notificationRepository.findMyNotifications(userId, LocalDateTime.now().minusMonths(3), pageable);
        List<NotificationResponse> notificationDtoList = toNotificationResponseList(myNotifications);

        return MyNotificationsResponse.toDto(notificationDtoList, myNotifications.hasNext());
    }

    private List<NotificationResponse> toNotificationResponseList(Slice<Notification> myNotifications) {
        return myNotifications.stream()
                .map(NotificationResponse::toDto)
                .collect(Collectors.toList());
    }
}
