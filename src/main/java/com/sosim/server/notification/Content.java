package com.sosim.server.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Content {

    PAYMENT_DATE("납부일 안내", ""),
    NON_PAYMENT("미납 안내", ""),
    SITUATION_PAYMENT("납부여부 변경", ""),
    CHANGE_ADMIN("총무 변경", ""),
    ;

    private String category;
    private String message;

    public static Content create(Content content, String data) {
        if (content.equals(PAYMENT_DATE)) {
            content.message = "오늘은 벌금 납부일입니다.\n미납 내역 확인 후, 벌금을 납부해주세요!";
        }

        if (content.equals(NON_PAYMENT)) {
            content.message = String.format("벌금 납부를 잊으셨나요?\n미납 내역 확인 후, %s원을 납부해주세요!", data);
        }

        if (content.equals(CHANGE_ADMIN)) {
            content.message = String.format("총무가 %s님으로 변경되었습니다.", data);
        }
        return content;
    }

    public static Content create(Content content, String data1, String data2) {
        content.message = String.format("%s님이 벌금내역을 %s으로 변경하였습니다.", data1, data2);
        return content;
    }
}
