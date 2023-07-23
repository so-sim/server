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

    private String groupTitle;

    private String category;

    private String summary;

    private String message;

    private String type;

    //TODO 내부 데이터 추가


    public static NotificationResponse toDto(Notification notification) {
        return NotificationResponse.builder()
                .date(notification.getSendDateTime().toLocalDate())
                .groupTitle(notification.getGroupTitle())
                .category(notification.getCategory())
                .summary(notification.getSummary())
                .message(notification.getMessage())
                .type(notification.getType())
                .build();
    }
}
