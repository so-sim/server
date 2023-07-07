package com.sosim.server.notification.util;

import com.sosim.server.notification.Content;
import com.sosim.server.notification.NotificationService;
import com.sosim.server.notification.dto.request.ManualNotificationRequest;
import com.sosim.server.notification.dto.request.ModifyAdminNotificationRequest;
import com.sosim.server.notification.dto.request.ModifySituationNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

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
        if (object instanceof ModifySituationNotificationRequest) {
            ModifySituationNotificationRequest notification = (ModifySituationNotificationRequest) object;
            sendModifySituationNotification(notification);
        }
        if (object instanceof ManualNotificationRequest) {
            ManualNotificationRequest notification = (ManualNotificationRequest) object;
            sendManualNotification(notification);
        }
    }

    private void sendModifyAdminNotification(ModifyAdminNotificationRequest notification) {
        notificationService.sendNotification(notification.getReceiverUserIdList(), notification.getGroupId(),
                notification.getGroupTitle(), Content.create(CHANGE_ADMIN, notification.getAdminNickname()));
    }

    private void sendModifySituationNotification(ModifySituationNotificationRequest notification) {
        notificationService.sendNotification(notification.getReceiverUserIdList(), notification.getGroupId(),
                notification.getGroupTitle(), Content.create(SITUATION_PAYMENT, notification.getNickname(), notification.getSituation()));
    }

    private void sendManualNotification(ManualNotificationRequest notification) {
        List<Long> receiverUserIdList = new ArrayList<>(notification.getUserAmountMap().keySet());
        receiverUserIdList.forEach(id -> {
                notificationService.sendNotification(id, notification.getGroupId(), notification.getGroupTitle(),
                        Content.create(NON_PAYMENT, String.valueOf(notification.getUserAmountMap().get(id))));
            }
        );
    }
}
