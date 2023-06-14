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
    GET_GROUP(successCode(), HttpStatus.OK, "모임이 성공적으로 조회되었습니다."),
    GET_PARTICIPANTS(successCode(), HttpStatus.OK, "모임 참가자가 성공적으로 조회되었습니다."),
    MODIFY_GROUP(successCode(), HttpStatus.OK, "모임이 성공적으로 수정되었습니다."),
    DELETE_GROUP(successCode(), HttpStatus.OK, "모임이 성공적으로 삭제되었습니다."),
    INTO_GROUP(successCode(), HttpStatus.CREATED, "모임에 성공적으로 참가되었습니다."),
    MODIFY_GROUP_ADMIN(successCode(), HttpStatus.OK, "관리자가 성공적으로 변경되었습니다."),
    WITHDRAW_GROUP(successCode(), HttpStatus.OK, "성공적으로 모임에서 탈퇴되었습니다."),
    MODIFY_NICKNAME(successCode(), HttpStatus.OK, "성공적으로 닉네임이 수정되었습니다."),
    GET_GROUPS(successCode(), HttpStatus.OK, "성공적으로 참가한 모임들이 조회되었습니다"),
    GET_NICKNAME(successCode(), HttpStatus.OK , "성공적으로 닉네임이 조회되었습니다."),

    BINDING_ERROR(2000, HttpStatus.BAD_REQUEST, "입력값 중 검증에 실패한 값이 있습니다"),

    NOT_EXIST_TOKEN_COOKIE(1200, HttpStatus.BAD_REQUEST, "리프레시 토큰이 존재하지 않습니다."),

    NOT_FOUND_USER(1100, HttpStatus.BAD_REQUEST, "존재하지 않는 회원 정보입니다."),
    USER_ALREADY_EXIST(1101, HttpStatus.BAD_REQUEST, "이미 존재하는 회원 정보입니다."),
    CANNOT_WITHDRAWAL_BY_GROUP_ADMIN(1102, HttpStatus.BAD_REQUEST, "모임의 총무는 소심한 총무 서비스 회원 탈퇴를 할 수 없습니다. 총무 역할을 위임해 주세요."),

    NOT_FOUND_GROUP(1001, HttpStatus.BAD_REQUEST, "해당 모임을 찾을 수 없습니다."),
    NONE_ADMIN(1002, HttpStatus.BAD_REQUEST, "모임 관리자 권한이 필요합니다."),
    NONE_PARTICIPANT(1003, HttpStatus.NOT_FOUND, "존재하지 않는 참가자 정보입니다."),
    ALREADY_USE_NICKNAME(1004, HttpStatus.BAD_REQUEST, "모임에서 이미 사용중인 닉네임입니다."),
    NO_MORE_GROUP(1005, HttpStatus.BAD_REQUEST, "더 이상 조회할 모임이 없습니다."),
    ALREADY_INTO_GROUP(1006, HttpStatus.BAD_REQUEST, "이미 참여중인 모임입니다."),
    NONE_ZERO_PARTICIPANT(1007, HttpStatus.BAD_REQUEST, "모임에 다른 참가자가 존재합니다."),

    ;

    private int code;
    private HttpStatus httpStatus;
    private String message;

    private static int successCode() {
        return 900;
    }
}
