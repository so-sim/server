package com.sosim.server.event.dto.response;

import com.sosim.server.event.Event;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GetEventIdListResponse {

    private List<GetEventResponse> eventList;

    public static GetEventIdListResponse toDto(List<Event> list) {
        List<GetEventResponse> eventResponseList = list.stream()
                .map(GetEventResponse::toDto)
                .collect(Collectors.toList());

        return GetEventIdListResponse.builder()
                .eventList(eventResponseList)
                .build();
    }
}
