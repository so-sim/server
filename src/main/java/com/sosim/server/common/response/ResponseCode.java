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
    SEARCH_PARTICIPANTS(successCode(), HttpStatus.OK, "참가자 검색이 성공적으로 수행되었습니다."),
    MODIFY_GROUP(successCode(), HttpStatus.OK, "모임이 성공적으로 수정되었습니다."),
    DELETE_GROUP(successCode(), HttpStatus.OK, "모임이 성공적으로 삭제되었습니다."),
    INTO_GROUP(successCode(), HttpStatus.CREATED, "모임에 성공적으로 참가되었습니다."),
    MODIFY_GROUP_ADMIN(successCode(), HttpStatus.OK, "관리자가 성공적으로 변경되었습니다."),
    WITHDRAW_GROUP(successCode(), HttpStatus.OK, "성공적으로 모임에서 탈퇴되었습니다."),
    MODIFY_NICKNAME(successCode(), HttpStatus.OK, "성공적으로 닉네임이 수정되었습니다."),
    GET_MY_GROUPS(successCode(), HttpStatus.OK, "성공적으로 참가한 모임들이 조회되었습니다"),
    GET_NICKNAME(successCode(), HttpStatus.OK , "성공적으로 닉네임이 조회되었습니다."),

    GET_GROUP_NOTIFICATION_SETTING(successCode(), HttpStatus.OK, "성공적으로 알림 설정을 조회했습니다."),
    SET_GROUP_NOTIFICATION_SETTING(successCode(), HttpStatus.OK, "성공적으로 알림 설정을 저장했습니다."),

    CAN_WITHDRAW(successCode(), HttpStatus.OK, "회원 탈퇴가 가능한 상태입니다."),
    SUCCESS_WITHDRAW(successCode(), HttpStatus.OK, "회원 탈퇴가 성공적으로 이루어졌습니다."),

    CREATE_EVENT(successCode(), HttpStatus.CREATED, "상세 내역이 성공적으로 생성되었습니다."),
    GET_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 조회되었습니다."),
    MODIFY_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 수정되었습니다."),
    DELETE_EVENT(successCode(), HttpStatus.OK, "상세 내역이 성공적으로 삭제되었습니다."),
    MODIFY_EVENT_SITUATION(successCode(), HttpStatus.OK, "상세 내역 납부 여부가 성공적으로 변경되었습니다."),
    GET_EVENT_CALENDAR(successCode(), HttpStatus.OK, "상세 내역 캘린더가 성공적으로 조회되었습니다."),
    GET_EVENTS(successCode(), HttpStatus.OK, "상세 내역 리스트가 성공적으로 조회되었습니다."),
    EVENTS_NOTIFICATION(successCode(), HttpStatus.OK, "상세 내역 알림 발송이 성공적으로 완료되었습니다."),
    SEND_NONE_PAYMENT_NOTIFICATIONS(successCode(), HttpStatus.OK, "미납 알림 발송이 성공적으로 완료되었습니다."),

    SUCCESS_SUBSCRIBE(successCode(), HttpStatus.OK, "알림 구독이 성공적으로 완료되었습니다."),
    SUCCESS_SEND_NOTIFICATION(successCode(), HttpStatus.OK, "알림이 성공적으로 전송되었습니다."),
    VIEW_ALL_NOTIFICATIONS(successCode(), HttpStatus.OK, "모든 알림이 성공적으로 읽음 처리되었습니다."),
    VIEW_NOTIFICATION(successCode(), HttpStatus.OK, "해당 알림이 성공적으로 읽음 처리되었습니다."),
    GET_MY_NOTIFICATIONS(successCode(), HttpStatus.OK, "성공적으로 알림 목록이 조회되었습니다."),
    GET_MY_NOTIFICATION_COUNT(successCode(), HttpStatus.OK, "성공적으로 알림 갯수가 조회되었습니다."),

    BINDING_ERROR(2000, HttpStatus.BAD_REQUEST, "입력값 중 검증에 실패한 값이 있습니다."),
    BAD_REQUEST(2001, HttpStatus.BAD_REQUEST, "올바르지 않은 요청입니다."),

    NOT_SUPPORTED_OAUTH(1400, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 서비스입니다."),
    INCORRECT_OAUTH_CODE(1401, HttpStatus.BAD_REQUEST, "올바르지 않은 Authorization 코드입니다."),

    NOT_EXIST_TOKEN_COOKIE(1200, HttpStatus.FORBIDDEN, "리프레시 토큰이 존재하지 않습니다."),
    MODULATION_REFRESH(1201, HttpStatus.UNAUTHORIZED, "변조된 리프레시 토큰입니다."),
    NOT_FOUNT_REFRESH(1202, HttpStatus.UNAUTHORIZED, "존재하지 않는 리프레시 토큰 데이터입니다."),
    MODULATION_ACCESS(1203, HttpStatus.UNAUTHORIZED, "변조된 엑세스 토큰입니다."),
    EXPIRATION_ACCESS(1204, HttpStatus.UNAUTHORIZED, "만료된 엑세스 토큰입니다."),
    NOT_EXIST_TOKEN(1205, HttpStatus.FORBIDDEN, "엑세스 토큰이 존재하지 않습니다."),

    NOT_FOUND_USER(1100, HttpStatus.NOT_FOUND, "존재하지 않는 회원 정보입니다."),
    USER_ALREADY_EXIST(1101, HttpStatus.BAD_REQUEST, "이미 존재하는 회원 정보입니다."),
    CANNOT_WITHDRAWAL_BY_GROUP_ADMIN(1102, HttpStatus.BAD_REQUEST, "모임의 총무는 소심한 총무 서비스 회원 탈퇴를 할 수 없습니다. 총무 역할을 위임해 주세요."),

    NOT_FOUND_GROUP(1001, HttpStatus.NOT_FOUND, "해당 모임을 찾을 수 없습니다."),
    NONE_ADMIN(1002, HttpStatus.FORBIDDEN, "모임 관리자 권한이 필요합니다."),
    NOT_FOUND_PARTICIPANT(1003, HttpStatus.NOT_FOUND, "존재하지 않는 참가자 정보입니다."),
    ALREADY_USE_NICKNAME(1004, HttpStatus.BAD_REQUEST, "모임 내 다른 팀원이 사용 중인 이름입니다."),
    NO_MORE_GROUP(1005, HttpStatus.BAD_REQUEST, "더 이상 조회할 모임이 없습니다."),
    ALREADY_INTO_GROUP(1006, HttpStatus.BAD_REQUEST, "이미 참여중인 모임입니다."),
    NONE_ZERO_PARTICIPANT(1007, HttpStatus.BAD_REQUEST, "모임에 다른 참가자가 존재합니다."),
    NOT_FOUND_ADMIN(1008, HttpStatus.NOT_FOUND, "모임의 총무를 찾을 수 없습니다."),
    USED_NICKNAME(1009, HttpStatus.BAD_REQUEST, "탈퇴 이력이 존재한 닉네임으로 변경 불가능 합니다."),

    NOT_FOUND_EVENT(1300, HttpStatus.BAD_REQUEST, "해당 상세 내역을 찾을 수 없습니다."),
    FAIL_TO_CHECK(1301, HttpStatus.BAD_REQUEST, "완납인 상세 내역은 확인중으로 변경 불가능합니다."),
    NOT_CHECK_SITUATION(1302, HttpStatus.BAD_REQUEST, "확인 요청 상태로만 변경 가능합니다."),
    NOT_FULL_OR_NON_SITUATION(1303, HttpStatus.BAD_REQUEST, "미납 또는 완납 상태로만 변경 가능합니다."),
    ONLY_CAN_HAVE_NONE_PAYMENT(1304, HttpStatus.BAD_REQUEST, "납부 요청은 미납 상태의 내역만 가능합니다."),
    NOT_FULL_TO_CHECK(1305, HttpStatus.BAD_REQUEST, "미납인 내역만 확인 중으로 변경 가능합니다."),
    NOT_SAME_SITUATION(1306, HttpStatus.BAD_REQUEST, "모든 상세 내역이 동일한 납부 여부 상태가 아닙니다."),

    MUST_NEED_NOTIFICATION_MESSAGE_DATA(1306, HttpStatus.BAD_REQUEST, "알림 메시지 데이터가 반드시 필요합니다."),
    NOT_FOUND_NOTIFICATION(1307, HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    IS_NOT_NOTIFICATION_RECEIVER(1308, HttpStatus.FORBIDDEN, "알림 수신자가 아닙니다.")
    ;
    private int code;
    private HttpStatus httpStatus;
    private String message;

    private static int successCode() {
        return 900;
    }
}
