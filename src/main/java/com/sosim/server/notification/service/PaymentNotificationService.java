package com.sosim.server.notification.service;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.event.domain.repository.EventRepository;
import com.sosim.server.event.dto.request.EventIdListRequest;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.notification.domain.entity.Content;
import com.sosim.server.notification.domain.entity.ContentType;
import com.sosim.server.notification.domain.entity.Notification;
import com.sosim.server.notification.domain.repository.NotificationRepository;
import com.sosim.server.notification.dto.NotificationDataDto;
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
        notificationRepository.saveAll(notifications);

        notificationUtil.sendNotifications(notifications);
    }

    private List<Notification> makeNotifications(List<Event> events) {
        Map<Long, NotificationDataDto> map = makeUserNotificationDataMap(events);
        Group group = events.get(0).getGroup();

        return map.entrySet().stream()
                .map(entry -> makeNotification(group, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Map<Long, NotificationDataDto> makeUserNotificationDataMap(List<Event> events) {
        Map<Long, NotificationDataDto> map = new HashMap<>();
        for (Event event : events) {

            if (event.isLock()) {
                continue;
            }

            Long userId = event.getUser().getId();
            NotificationDataDto dataDto = map.compute(userId, (k, v) -> v == null ? new NotificationDataDto() : v);
            dataDto.addAmount(event.getAmount());
            dataDto.addEventId(event.getId());
        }
        return map;
    }

    private Notification makeNotification(Group group, long userId, NotificationDataDto notificationDataDto) {
        int totalAmount = notificationDataDto.getTotalAmount();
        List<Long> eventIdList = notificationDataDto.getEventIdList();

        return Notification.toEntity(userId, group,
                Content.create(ContentType.REQUEST_PAYMENT, String.valueOf(totalAmount)), eventIdList);
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
