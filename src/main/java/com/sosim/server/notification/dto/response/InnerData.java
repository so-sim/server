package com.sosim.server.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class InnerData {
    private long groupId;

    private String dataType;

    private List<Long> eventIdList;

}
