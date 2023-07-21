package com.sosim.server.notification;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.event.dto.request.EventIdListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sosim.server.common.response.ResponseCode.SEND_NONE_PAYMENT_NOTIFICATIONS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentNotificationController {

    private final PaymentNotificationService paymentNotificationService;

    @PostMapping("/notifications")
    public ResponseEntity<?> sendPaymentNotifications(@AuthUserId long userId, @Validated @RequestBody EventIdListRequest eventIdListRequest) {
        paymentNotificationService.sendPaymentNotification(userId, eventIdListRequest);

        return new ResponseEntity<>(Response.create(SEND_NONE_PAYMENT_NOTIFICATIONS, null), SEND_NONE_PAYMENT_NOTIFICATIONS.getHttpStatus());
    }
}
