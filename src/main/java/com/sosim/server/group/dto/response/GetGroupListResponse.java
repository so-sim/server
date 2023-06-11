package com.sosim.server.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetGroupListResponse {
    @JsonProperty("index")
    private Long index;

    @JsonProperty("next")
    private boolean next;

    @JsonProperty("groupList")
    List<GetGroupResponse> groupList;

    public static GetGroupListResponse create(Long index, boolean next, List<GetGroupResponse> groupList) {
        return GetGroupListResponse.builder()
                .index(index)
                .next(next)
                .groupList(groupList)
                .build();
    }
}
