package com.sosim.server.notification.dto.request;

import com.sosim.server.group.domain.entity.Group;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModifySituationNotificationRequest {

    private long groupId;

    private String groupTitle;

    private String situation;

    private String nickname;

    private List<Long> receiverUserIdList;

    public static ModifySituationNotificationRequest toDto(Group group, String situation, String nickname, List<Long> receiverUserIdList) {
        return ModifySituationNotificationRequest.builder()
                .groupId(group.getId())
                .groupTitle(group.getTitle())
                .situation(situation)
                .nickname(nickname)
                .receiverUserIdList(receiverUserIdList)
                .build();
    }
}
