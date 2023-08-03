package com.sosim.server.notification.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NotificationDataDto {
    private List<Long> eventIdList;

    private int totalAmount;

    public NotificationDataDto() {
        eventIdList = new ArrayList<>();
    }

    public void addAmount(int amount) {
        totalAmount += amount;
    }

    public void addEventId(long eventId) {
        eventIdList.add(eventId);
    }
}
