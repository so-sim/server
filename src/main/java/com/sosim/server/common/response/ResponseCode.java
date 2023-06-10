package com.sosim.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS_LOGIN(successCode(), HttpStatus.OK, "로그인이 성공적으로 완료되었습니다."),
    SUCCESS_SIGNUP(successCode(), HttpStatus.OK, "회원가입이 성공적으로 완료되었습니다."),
    SUCCESS_REFRESH_TOKEN(successCode(), HttpStatus.OK, "토큰이 성공적으로 재발급 되었습니다."),
    SUCCESS_LOGOUT(successCode(), HttpStatus.OK, "로그아웃이 성공적으로 완료되었습니다."),

    CREATE_GROUP(successCode(), HttpStatus.CREATED, "모임이 성공적으로 생성되었습니다."),

    BINDING_ERROR(2000, HttpStatus.BAD_REQUEST, "입력값 중 검증에 실패한 값이 있습니다"),

    NOT_EXIST_TOKEN_COOKIE(1200, HttpStatus.BAD_REQUEST, "리프레시 토큰이 존재하지 않습니다."),

    NOT_FOUND_USER(1100, HttpStatus.BAD_REQUEST, "존재하지 않는 회원 정보입니다."),
    USER_ALREADY_EXIST(1101, HttpStatus.BAD_REQUEST, "이미 존재하는 회원 정보입니다.");


    private int code;
    private HttpStatus httpStatus;
    private String message;

    private static int successCode() {
        return 1000;
    }
}
