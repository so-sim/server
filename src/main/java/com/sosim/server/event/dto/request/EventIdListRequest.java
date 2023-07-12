package com.sosim.server.event.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class EventIdListRequest {

    private List<Long> eventIdList;
}
