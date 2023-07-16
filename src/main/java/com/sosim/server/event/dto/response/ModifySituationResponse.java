package com.sosim.server.event.dto.response;

import com.sosim.server.event.Situation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModifySituationResponse {

    private String situation;

    private List<Long> eventIdList;

    public static ModifySituationResponse toDto(Situation situation, List<Long> eventIdList) {
        return ModifySituationResponse.builder()
                .situation(situation.getComment())
                .eventIdList(eventIdList)
                .build();
    }
}
