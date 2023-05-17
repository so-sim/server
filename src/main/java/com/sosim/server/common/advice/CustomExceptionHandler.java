package com.sosim.server.common.advice;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(value = CustomException.class)
    protected ResponseEntity<?> handleCustomException(CustomException exception) {
        return new ResponseEntity<>(Response.create(exception.getResponseCode(), exception.getContent()),
                exception.getResponseCode().getHttpStatus());
    }
}
