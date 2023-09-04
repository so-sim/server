package com.sosim.server.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sosim.server.notification.domain.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class NotificationResponse {

    private long notificationId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String type;

    private long groupId;

    private String groupTitle;

    private String category;

    private String summary;

    private MessageDataDto messageData;

    private List<Long> eventIdList;

    private boolean view;

    public static NotificationResponse toDto(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .date(notification.getCreateDate().toLocalDate())
                .type(notification.getType())
                .groupId(notification.getGroupId())
                .groupTitle(notification.getGroupTitle())
                .category(notification.getCategory())
                .summary(notification.getSummary())
                .messageData(notification.getMessageData())
                .eventIdList(notification.getEventIdList())
                .view(notification.isView())
                .build();
    }

}
