package com.sosim.server.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class WithdrawRequest {
    @JsonProperty("withdrawReason")
    private String withdrawReason;
}
