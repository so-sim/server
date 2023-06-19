package com.sosim.server.event.dto.response;

import com.sosim.server.event.Event;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GetEventOneResponse extends GetEventResponse {

    private Boolean isAdmin;

    public static GetEventOneResponse toDto(Event event, boolean isAdmin) {
        return GetEventOneResponse.builder()
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
