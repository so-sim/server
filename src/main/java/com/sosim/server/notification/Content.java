package com.sosim.server.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Getter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    public static String PAYMENT_DATE = "납부일 안내";
    public static String NON_PAYMENT = "미납 안내";
    public static String SITUATION_PAYMENT = "납부여부 변경";
    public static String CHANGE_ADMIN = "총무 변경";

    private String category;

    private String message;

    public static Content create(String type, String data) {
        return Content.builder()
                .category(type)
                .message(createMessage(type,data))
                .build();
    }

    public static Content create(String type, String nickname, String situation) {
        return Content.builder()
                .category(type)
                .message(String.format("%s님이 벌금내역을 %s으로 변경하였습니다.", nickname, situation))
                .build();
    }

    private static String createMessage(String type, String data) {
        String message = "";
        if (type.equals(PAYMENT_DATE)) {
            message = "오늘은 벌금 납부일입니다.\n미납 내역 확인 후, 벌금을 납부해주세요!";
        }

        if (type.equals(NON_PAYMENT)) {
            message = String.format("벌금 납부를 잊으셨나요?\n미납 내역 확인 후, %s원을 납부해주세요!", data);
        }

        if (type.equals(CHANGE_ADMIN)) {
            message = String.format("총무가 %s님으로 변경되었습니다.", data);
        }
        return message;
    }
}
