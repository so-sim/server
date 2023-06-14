package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupIdResponse {
    @JsonProperty("groupId")
    private Long groupId;

    public static GroupIdResponse create(Group group) {
        return GroupIdResponse.builder()
                .groupId(group.getId())
                .build();
    }
}
