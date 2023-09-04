package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.domain.entity.Group;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyGroupDto {
    private long groupId;

    private String title;

    private String coverColor;

    private String type;

    private String adminNickname;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;


    @Builder
    public MyGroupDto(long id, String title, String coverColor, String type, String adminNickname, boolean isAdmin, int size, Boolean isInto) {
        this.groupId = id;
        this.title = title;
        this.coverColor = coverColor;
        this.type = type;
        this.adminNickname = adminNickname;
        this.isAdmin = isAdmin;
    }

    public static MyGroupDto toDto(Group group, boolean isAdmin) {
        return MyGroupDto.builder()
                .id(group.getId())
                .title(group.getTitle())
                .coverColor(group.getCoverColor())
                .type(group.getGroupType())
                .adminNickname(group.getAdminParticipant().getNickname())
                .isAdmin(isAdmin)
                .size(group.getNumberOfParticipants())
                .build();
    }
}
