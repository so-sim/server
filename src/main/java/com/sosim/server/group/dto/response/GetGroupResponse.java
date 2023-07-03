package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GetGroupResponse {
    private long groupId;

    private String title;

    private String coverColor;

    @JsonProperty("type")
    private String groupType;

    private String adminNickname;

    @JsonProperty("isAdmin")
    private boolean admin;

    private int size;

    @JsonProperty("isInto")
    private boolean into;

    @Builder
    public GetGroupResponse(long id, String title, String coverColor, String groupType, String adminNickname, boolean admin, int size, boolean into) {
        this.groupId = id;
        this.title = title;
        this.coverColor = coverColor;
        this.groupType = groupType;
        this.adminNickname = adminNickname;
        this.admin = admin;
        this.size = size;
        this.into = into;
    }

    public static GetGroupResponse toDto(Group group, boolean admin, int size, boolean into) {
        return GetGroupResponse.builder()
                .id(group.getId())
                .title(group.getTitle())
                .coverColor(group.getCoverColor())
                .groupType(group.getGroupType())
                .adminNickname(group.getAdminParticipant().getNickname())
                .admin(admin)
                .size(size)
                .into(into)
                .build();
    }
}
