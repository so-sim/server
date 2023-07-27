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

    private InnerData innerData;

    public static NotificationResponse toDto(Notification notification) {
        InnerData innerData = InnerData.builder()
                .groupId(notification.getGroupId())
                .dataType(notification.getType())
                //TODO Notification에 eventId List 추가해서 적용
                .build();

        return NotificationResponse.builder()
                .date(notification.getSendDateTime().toLocalDate())
                .groupTitle(notification.getGroupTitle())
                .category(notification.getCategory())
                .summary(notification.getSummary())
                .message(notification.getMessage())
                .innerData(innerData)
                .build();
    }
}
