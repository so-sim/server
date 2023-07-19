package com.sosim.server.event.dto.request;

import com.sosim.server.event.Situation;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class ModifySituationRequest {

    private List<Long> eventIdList;

    @NotNull(message = "존재하지 않는 납부 여부 목록입니다.")
    private Situation situation;
}
