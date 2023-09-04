package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sosim.server.event.domain.entity.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GetEventResponse {

    private long eventId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private int amount;

    private String ground;

    private String memo;

    private String situation;

    private String nickname;

    public static GetEventResponse toDto(Event event) {
        return GetEventResponse.builder()
                .eventId(event.getId())
                .date(event.getDate())
                .amount(event.getAmount())
                .ground(event.getGround().getComment())
                .memo(event.getMemo())
                .situation(event.getSituation().getComment())
                .nickname(event.getNickname())
                .build();
    }
}
