package com.sosim.server.notification.dto.request;

import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModifyAdminNotificationRequest {

    private long groupId;

    private String groupTitle;

    private String adminNickname;

    public static ModifyAdminNotificationRequest toDto(Group group, String adminNickname) {
        return ModifyAdminNotificationRequest.builder()
                .groupId(group.getId())
                .groupTitle(group.getTitle())
                .adminNickname(adminNickname)
                .build();
    }
}
