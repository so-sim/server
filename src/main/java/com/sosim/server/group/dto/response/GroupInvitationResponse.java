package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.domain.entity.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInvitationResponse {
    private long groupId;

    private String title;

    private String coverColor;

    @JsonProperty("isInto")
    private boolean into;

    @JsonProperty("isWithdraw")
    private boolean withdraw;

    private String nickname;

    public static GroupInvitationResponse toDto(Group group, boolean into, boolean withdraw, String nickname) {
        return GroupInvitationResponse.builder()
                .groupId(group.getId())
                .title(group.getTitle())
                .coverColor(group.getCoverColor())
                .into(into)
                .withdraw(withdraw)
                .nickname(nickname)
                .build();
    }
}
