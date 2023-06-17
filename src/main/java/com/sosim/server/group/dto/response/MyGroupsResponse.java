package com.sosim.server.group.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class MyGroupsResponse {

    private boolean hasNext;

    List<MyGroupDto> groupList;

    public static MyGroupsResponse toResponseDto(boolean hasNext, List<MyGroupDto> groupList) {
        return MyGroupsResponse.builder()
                .hasNext(hasNext)
                .groupList(groupList)
                .build();
    }
}
