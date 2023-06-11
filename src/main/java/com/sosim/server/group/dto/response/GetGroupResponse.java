package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.Group;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetGroupResponse {
    @JsonProperty("groupId")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("coverColor")
    private String coverColor;

    @JsonProperty("type")
    private String groupType;

    @JsonProperty("adminNickname")
    private String adminNickname;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    @JsonProperty("size")
    private int size;

    @JsonProperty("isInto")
    private Boolean isInto;

    public static GetGroupResponse create(Group group, boolean isAdmin, int size, boolean isInto) {
        return GetGroupResponse.builder()
                .id(group.getId())
                .title(group.getTitle())
                .coverColor(group.getCoverColor())
                .groupType(group.getGroupType())
                .adminNickname(group.getAdminNickname())
                .isAdmin(isAdmin)
                .size(size)
                .isInto(isInto)
                .build();
    }
}
