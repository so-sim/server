package com.sosim.server.event.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ModifySituationRequest {

    private List<Long> eventIdList;

    private String situation;
}
