package com.sosim.server.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCountResponse {

    private int count;

    public static NotificationCountResponse toDto(int count) {
        return NotificationCountResponse.builder()
                .count(count)
                .build();
    }
}
