package com.sosim.server.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.Event;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateEventRequest {
    @JsonProperty("groupId")
    private Long groupId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
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

    public Event toEntity(Group group, User user) {
        return Event.builder()
                .date(date)
                .amount(amount)
                .ground(ground)
                .memo(memo)
                .situation(situation)
                .nickname(nickname)
                .group(group)
                .user(user)
                .build();
    }
}
