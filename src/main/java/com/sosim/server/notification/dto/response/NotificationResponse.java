package com.sosim.server.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sosim.server.notification.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NotificationResponse {

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate date;

    private String category;

    private String groupTitle;

    private String message;

    public static NotificationResponse toDto(Notification notification) {
        return NotificationResponse.builder()
                .date(notification.getSendDateTime().toLocalDate())
                .category(notification.getContent().getCategory())
                .groupTitle(notification.getGroupTitle())
                .message(notification.getContent().getMessage())
                .build();
    }
}
