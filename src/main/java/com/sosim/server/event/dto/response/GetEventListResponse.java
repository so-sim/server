package com.sosim.server.event.dto.response;

import com.sosim.server.event.Event;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GetEventListResponse {

    private long totalCount;

    private List<GetEventResponse> eventList;

    public static GetEventListResponse toDto(List<Event> list, long totalCount) {
        return GetEventListResponse.builder()
                .totalCount(totalCount)
                .eventList(list.stream().map(GetEventResponse::toDto).collect(Collectors.toList()))
                .build();
    }
}
