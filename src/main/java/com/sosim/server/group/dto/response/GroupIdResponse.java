package com.sosim.server.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupIdResponse {
    private long groupId;

    public static GroupIdResponse toDto(long groupId) {
        return GroupIdResponse.builder()
                .groupId(groupId)
                .build();
    }
}
