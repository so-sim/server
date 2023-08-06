package com.sosim.server.notification.controller;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.notification.service.NotificationService;
import com.sosim.server.notification.dto.response.MyNotificationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.sosim.server.common.response.ResponseCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthUserId long userId) {
        return notificationService.subscribe(userId);
    }

    @PatchMapping("/notification")
    public ResponseEntity<?> viewAllNotifications(@AuthUserId long userId) {
        notificationService.viewAllNotifications(userId);

        return new ResponseEntity<>(Response.create(VIEW_ALL_NOTIFICATIONS, null), VIEW_ALL_NOTIFICATIONS.getHttpStatus());
    }

    @PatchMapping("/notification/{notificationId}")
    public ResponseEntity<?> viewNotification(@AuthUserId long userId, @PathVariable long notificationId) {
        notificationService.viewNotification(userId, notificationId);

        return new ResponseEntity<>(Response.create(VIEW_NOTIFICATION, null), VIEW_NOTIFICATION.getHttpStatus());
    }

    @GetMapping("/notification")
    public ResponseEntity<?> getMyNotifications(@AuthUserId long userId,
                                                @PageableDefault(sort="id", direction = Sort.Direction.DESC) Pageable pageable) {
        MyNotificationsResponse myNotifications = notificationService.getMyNotifications(userId, pageable);

        return new ResponseEntity<>(Response.create(GET_MY_NOTIFICATIONS, myNotifications), GET_MY_NOTIFICATIONS.getHttpStatus());
    }

}
