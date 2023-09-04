package com.sosim.server.event.dto.response;

import com.sosim.server.event.domain.entity.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventIdResponse {

    private long eventId;

    public static EventIdResponse toDto(Event event) {
        return EventIdResponse.builder()
                .eventId(event.getId())
                .build();
    }
}
