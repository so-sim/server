package com.sosim.server.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCountResponse {

    private long count;

    public static NotificationCountResponse toDto(long count) {
        return NotificationCountResponse.builder()
                .count(count)
                .build();
    }
}
