package com.sosim.server.event.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModifySituationResponse {

    private String situation;

    private List<Long> eventIdList;

    public static ModifySituationResponse toDto(String situation, List<Long> eventIdList) {
        return ModifySituationResponse.builder()
                .situation(situation)
                .eventIdList(eventIdList)
                .build();
    }
}
