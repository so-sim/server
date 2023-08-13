package com.sosim.server.event.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.util.List;

@Getter
@Setter
public class GetEventIdListRequest {
    @Min(1)
    private long groupId;

    private List<Long> eventIdList;
}
