package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.Group;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CreateGroupResponse {
    @JsonProperty("groupId")
    private Long groupId;

    public static CreateGroupResponse create(Group group) {
        return CreateGroupResponse.builder()
                .groupId(group.getId())
                .build();
    }
}
