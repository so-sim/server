package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EventIdResponse {
    @JsonProperty("eventId")
    private Long eventId;

    public static EventIdResponse toDto(Event event) {
        return EventIdResponse.builder()
                .eventId(event.getId())
                .build();
    }
}
