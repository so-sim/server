package com.sosim.server.notification;

import com.sosim.server.event.Situation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    PAYMENT_DATE("", "납부일 안내", "오늘은 벌금 납부일입니다!",
            "\"%s\"벌금 내역 확인 후, 벌금을 내주세요!"),
    NONE_PAYMENT("", "납부 요청", "벌금 납부를 잊으셨나요?",
            "\"%s\"벌금 내역 확인 후, %d원을 내주세요!"),
    CHANGE_FULL_SITUATION("", "납부여부 변경", "벌금을 모두 납부했습니다!",
            "\"%s\"벌금 내역이 납부완료로 변경되었습니다."),
    CHANGE_NONE_SITUATION("", "납부여부 변경", "아직 안 낸 벌금이 있습니다.",
            "\"%s\"벌금 내역이 납부 전으로 변경되었습니다."),
    CHANGE_CHECK_SITUATION("", "납부여부 변경", "승인대기 중인 내역이 있습니다.",
            "\"%s\" %s의 내역이 승인대기 중입니다."),
    CHANGE_ADMIN("", "납부여부 변경", "총무가 변경되었습니다.",
            "\"%s\"의 새로운 총무를 확인해보세요!"),
    ;

    private final String type;
    private final String category;
    private final String summary;
    private final String messageFormat;

    public static ContentType getSituationType(Situation situation) {
        switch (situation) {
            case FULL:
                return CHANGE_FULL_SITUATION;
            case NONE:
                return CHANGE_NONE_SITUATION;
            case CHECK:
                return CHANGE_CHECK_SITUATION;
        }
        return null;
    }
}
