package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.Event;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
public class GetEventResponse {
    @JsonProperty("eventId")
    private Long id;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate date;

    private int amount;

    private String ground;

    private String memo;

    private String situation;

    private String nickname;

    public static GetEventResponse toDto(Event event) {
        return GetEventResponse.builder()
                .id(event.getId())
                .date(event.getDate())
                .amount(event.getAmount())
                .ground(event.getGround())
                .memo(event.getMemo())
                .situation(event.getSituation())
                .nickname(event.getNickname())
                .build();
    }
}
