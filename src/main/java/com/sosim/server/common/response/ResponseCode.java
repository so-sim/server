package com.sosim.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS_LOGIN(successCode(), HttpStatus.OK, "로그인이 성공적으로 완료되었습니다.");

    private int code;
    private HttpStatus httpStatus;
    private String message;

    private static int successCode() {
        return 900;
    }
}
