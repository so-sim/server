package com.sosim.server.event.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateEventRequest {
    @JsonProperty("groupId")
    private Long groupId;

    @JsonProperty("nickname")
    private String nickname;

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
}
