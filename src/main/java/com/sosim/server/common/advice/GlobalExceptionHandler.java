package com.sosim.server.common.advice;

import com.sosim.server.common.advice.dto.ExceptionContent;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.sosim.server.common.response.ResponseCode.BINDING_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = CustomException.class)
    protected ResponseEntity<?> handleCustomException(CustomException exception) {
        return new ResponseEntity<>(Response.create(exception.getResponseCode(), exception.getContent()),
                exception.getResponseCode().getHttpStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<?> bindingException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String field = bindingResult.getFieldError().getField();
        String message = bindingResult.getFieldError().getDefaultMessage();

        ExceptionContent content = new ExceptionContent(field, message);
        return new ResponseEntity<>(Response.create(BINDING_ERROR, content), HttpStatus.BAD_REQUEST);
    }
}
