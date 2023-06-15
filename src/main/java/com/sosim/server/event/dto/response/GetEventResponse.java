package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetEventResponse {
    @JsonProperty("eventId")
    private Long id;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("ground")
    private String ground;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("situation")
    private String situation;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    public static GetEventResponse toDto(Event event, boolean isAdmin) {
        return GetEventResponse.builder()
                .id(event.getId())
                .date(event.getDate())
                .amount(event.getAmount())
                .ground(event.getGround())
                .memo(event.getMemo())
                .situation(event.getSituation())
                .nickname(event.getNickname())
                .isAdmin(isAdmin)
                .build();
    }
}
