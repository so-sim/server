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
    GET_MY_GROUPS(successCode(), HttpStatus.OK, "성공적으로 참가한 모임들이 조회되었습니다"),
    GET_NICKNAME(successCode(), HttpStatus.OK , "성공적으로 닉네임이 조회되었습니다."),

    CAN_WITHDRAW(successCode(), HttpStatus.OK, "회원 탈퇴가 가능한 상태입니다."),
    SUCCESS_WITHDRAW(successCode(), HttpStatus.OK, "회원 탈퇴가 성공적으로 이루어졌습니다."),

    CREATE_EVENT(successCode(), HttpStatus.CREATED, "상세 내역이 성공적으로 생성되었습니다."),
    GET_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 조회되었습니다."),
    MODIFY_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 수정되었습니다."),
    DELETE_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 삭제되었습니다."),
    MODIFY_EVENT_SITUATION(successCode(), HttpStatus.OK, "상세 내역 납부 여부가 성공적으로 변경되었습니다."),
    GET_EVENT_CALENDAR(successCode(), HttpStatus.OK, "상세 내역 캘린더가 성공적으로 조회되었습니다."),
    GET_EVENTS(successCode(), HttpStatus.OK, "상세 내역 리스트가 성공적으로 조회되었습니다."),

    BINDING_ERROR(2000, HttpStatus.BAD_REQUEST, "입력값 중 검증에 실패한 값이 있습니다"),

    NOT_SUPPORTED_OAUTH(1400, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 서비스입니다."),
    INCORRECT_OAUTH_CODE(1401, HttpStatus.BAD_REQUEST, "올바르지 않은 Authorization 코드입니다."),

    NOT_EXIST_TOKEN_COOKIE(1200, HttpStatus.BAD_REQUEST, "리프레시 토큰이 존재하지 않습니다."),
    MODULATION_JWT(1201, HttpStatus.UNAUTHORIZED, "변조된 JWT 토큰 입니다."),
    EXPIRATION_JWT(1202, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰 입니다."),

    NOT_FOUND_USER(1100, HttpStatus.NOT_FOUND, "존재하지 않는 회원 정보입니다."),
    USER_ALREADY_EXIST(1101, HttpStatus.BAD_REQUEST, "이미 존재하는 회원 정보입니다."),
    CANNOT_WITHDRAWAL_BY_GROUP_ADMIN(1102, HttpStatus.BAD_REQUEST, "모임의 총무는 소심한 총무 서비스 회원 탈퇴를 할 수 없습니다. 총무 역할을 위임해 주세요."),

    NOT_FOUND_GROUP(1001, HttpStatus.NOT_FOUND, "해당 모임을 찾을 수 없습니다."),
    NONE_ADMIN(1002, HttpStatus.FORBIDDEN, "모임 관리자 권한이 필요합니다."),
    NOT_FOUND_PARTICIPANT(1003, HttpStatus.NOT_FOUND, "존재하지 않는 참가자 정보입니다."),
    ALREADY_USE_NICKNAME(1004, HttpStatus.BAD_REQUEST, "모임에서 이미 사용중인 닉네임입니다."),
    NO_MORE_GROUP(1005, HttpStatus.BAD_REQUEST, "더 이상 조회할 모임이 없습니다."),
    ALREADY_INTO_GROUP(1006, HttpStatus.BAD_REQUEST, "이미 참여중인 모임입니다."),
    NONE_ZERO_PARTICIPANT(1007, HttpStatus.BAD_REQUEST, "모임에 다른 참가자가 존재합니다."),
    NOT_FOUND_ADMIN(1008, HttpStatus.NOT_FOUND, "모임의 총무를 찾을 수 없습니다."),

    NOT_FOUND_EVENT(1300, HttpStatus.BAD_REQUEST, "해당 상세 내역을 찾을 수 없습니다."),

    ;

    private int code;
    private HttpStatus httpStatus;
    private String message;

    private static int successCode() {
        return 900;
    }
}
