package com.sosim.server.group.dto.response;

import com.sosim.server.group.domain.entity.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInvitationResponse {
    private long groupId;

    private String title;

    private String coverColor;

    private boolean isInto;

    private boolean isWithdraw;

    private String nickname;

    public static GroupInvitationResponse toDto(Group group, boolean isInto, boolean isWithdraw, String nickname) {
        return GroupInvitationResponse.builder()
                .groupId(group.getId())
                .title(group.getTitle())
                .coverColor(group.getCoverColor())
                .isInto(isInto)
                .isWithdraw(isWithdraw)
                .nickname(nickname)
                .build();
    }
}
