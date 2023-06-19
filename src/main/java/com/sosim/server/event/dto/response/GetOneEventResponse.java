package com.sosim.server.event.dto.response;

import com.sosim.server.event.Event;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GetOneEventResponse extends GetEventResponse {

    private Boolean isAdmin;

    public static GetOneEventResponse toDto(Event event, boolean isAdmin) {
        return GetOneEventResponse.builder()
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
