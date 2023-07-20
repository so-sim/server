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

import static com.sosim.server.common.response.ResponseCode.CREATE_EVENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class PaymentNotificationController {

    private final PaymentNotificationService paymentNotificationService;

    @PostMapping("/payment-notification")
    public ResponseEntity<?> sendPaymentNotifications(@AuthUserId long userId, @Validated @RequestBody EventIdListRequest eventIdListRequest) {
        paymentNotificationService.sendPaymentNotification(userId, eventIdListRequest);

        //TODO: Response 변경
        return new ResponseEntity<>(Response.create(CREATE_EVENT, null), CREATE_EVENT.getHttpStatus());
    }
}
