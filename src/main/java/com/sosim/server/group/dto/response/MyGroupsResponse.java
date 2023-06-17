package com.sosim.server.group.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class MyGroupsResponse {
    private Long index;

    private boolean next;

    List<GetGroupResponse> groupList;

    public static MyGroupsResponse create(Long index, boolean next, List<GetGroupResponse> groupList) {
        return MyGroupsResponse.builder()
                .index(index)
                .next(next)
                .groupList(groupList)
                .build();
    }
}
