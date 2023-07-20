package com.sosim.server.notification;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.Event;
import com.sosim.server.event.EventRepository;
import com.sosim.server.event.dto.request.EventIdListRequest;
import com.sosim.server.group.Group;
import com.sosim.server.notification.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.NONE_ADMIN;
import static com.sosim.server.common.response.ResponseCode.ONLY_CAN_HAVE_NONE_PAYMENT;
import static com.sosim.server.notification.Content.NON_PAYMENT;

@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final EventRepository eventRepository;

    private final NotificationRepository notificationRepository;

    private final NotificationUtil notificationUtil;

    @Transactional
    public void sendPaymentNotification(long userId, EventIdListRequest eventIdListRequest) {
        List<Event> events = eventRepository.findAllById(eventIdListRequest.getEventIdList());
        checkIsAdmin(userId, events);
        checkAllStatusIsNonePayment(events);

        List<Notification> notifications = makeNotifications(events);
        //TODO: save로직을 여기서 수행하는게 맞을 지?
        notificationRepository.saveAll(notifications);

        notificationUtil.sendNotifications(notifications);
    }

    private List<Notification> makeNotifications(List<Event> events) {
        Map<Long, Integer> map = makeUserTotalAmountMap(events);
        Group group = events.get(0).getGroup();
        return map.entrySet().stream()
                .map(entry -> makeNotification(group, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Notification makeNotification(Group group, long userId, int totalAmount) {
        return Notification.toEntity(userId, group,
                Content.create(NON_PAYMENT, String.valueOf(totalAmount)));
    }

    private Map<Long, Integer> makeUserTotalAmountMap(List<Event> events) {
        Map<Long, Integer> map = new HashMap<>();
        for (Event event : events) {
            map.compute(event.getUser().getId(), (k, v) -> (v == null ? 0 : v) + event.getAmount());
        }
        return map;
    }

    private void checkAllStatusIsNonePayment(List<Event> events) {
        if (events.stream().anyMatch(Event::isNotNonePaymentSituation)) {
            throw new CustomException(ONLY_CAN_HAVE_NONE_PAYMENT);
        }
    }

    private void checkIsAdmin(long userId, List<Event> events) {
        if (events.stream().anyMatch(event -> !event.getGroup().isAdminUser(userId))) {
            throw new CustomException(NONE_ADMIN);
        }
    }
}
