package com.sosim.server.notification.dto.request;

import com.sosim.server.group.domain.entity.Group;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModifyAdminNotificationRequest {

    private long groupId;

    private String groupTitle;

    private String adminNickname;

    private List<Long> receiverUserIdList;

    public static ModifyAdminNotificationRequest toDto(Group group, String adminNickname, List<Long> receiverUserIdList) {
        return ModifyAdminNotificationRequest.builder()
                .groupId(group.getId())
                .groupTitle(group.getTitle())
                .adminNickname(adminNickname)
                .receiverUserIdList(receiverUserIdList)
                .build();
    }
}
