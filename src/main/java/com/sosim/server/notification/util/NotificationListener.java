package com.sosim.server.notification.util;

import com.sosim.server.notification.Content;
import com.sosim.server.notification.NotificationService;
import com.sosim.server.notification.dto.request.ModifyAdminNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.sosim.server.notification.Content.*;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void eventHandler(Object object) {
        if (object instanceof ModifyAdminNotificationRequest) {
            ModifyAdminNotificationRequest notification = (ModifyAdminNotificationRequest) object;
            sendModifyAdminNotification(notification);
        }
    }

    private void sendModifyAdminNotification(ModifyAdminNotificationRequest notification) {
        notificationService.sendNotification(notification.getGroupId(), notification.getGroupTitle(),
                Content.create(CHANGE_ADMIN, notification.getAdminNickname()));
    }
}
