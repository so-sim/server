package com.sosim.server.common.advice.exception;

import com.sosim.server.common.response.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

@Getter
public class CustomException extends NestedRuntimeException {

    private ResponseCode responseCode;
    private Content content;

    public CustomException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public CustomException(ResponseCode responseCode, String field, String message) {
        this(responseCode);
        content = new Content(field, message);
    }

    @Getter
    @AllArgsConstructor
    private static class Content {
        private String field;
        private String message;
    }
}
