package com.sosim.server.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sosim.server.event.Event;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreateEventRequest {

    private long groupId;

    private String nickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private LocalDate date;

    private int amount;

    private String ground;

    private String memo;

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
