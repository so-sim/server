package com.sosim.server.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyNotificationsResponse {

    private boolean hasNext;

    private List<NotificationResponse> notificationResponseList;

    public static MyNotificationsResponse toDto(List<NotificationResponse> myNotifications, boolean hasNext) {
        return MyNotificationsResponse.builder()
                .hasNext(hasNext)
                .notificationResponseList(myNotifications)
                .build();

    }
}
