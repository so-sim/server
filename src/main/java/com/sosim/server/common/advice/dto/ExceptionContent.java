package com.sosim.server.common.advice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ExceptionContent {
    private String field;
    private String message;
}
