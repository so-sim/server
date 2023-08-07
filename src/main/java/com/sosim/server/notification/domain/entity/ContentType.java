package com.sosim.server.notification.domain.entity;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.domain.entity.Situation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.sosim.server.common.response.ResponseCode.*;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    PAYMENT_DATE("PAYMENT_DATE", "납부일 안내", "오늘은 벌금 납부일입니다!", 0),
    REQUEST_PAYMENT("REQUEST_PAYMENT", "납부 요청", "벌금 납부를 잊으셨나요?", 1),
    CHANGE_FULL_SITUATION("CHANGE_FULL_SITUATION", "납부여부 변경", "벌금을 모두 납부했습니다!", 1),
    CHANGE_NONE_SITUATION("CHANGE_NONE_SITUATION", "납부여부 변경", "내역이 \"납부 전\"으로 다시 변경되었습니다.", 1),
    CHANGE_CHECK_SITUATION("CHANGE_CHECK_SITUATION", "납부여부 변경", "승인대기 중인 내역이 있습니다.", 2),
    CHANGE_ADMIN("CHANGE_ADMIN", "총무 변경", "총무가 변경되었습니다.", 0),
    ;

    private final String type;
    private final String category;
    private final String summary;
    private final int requiredDataNumber;

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

    public void checkDataCount(String[] data) {
        if (requiredDataNumber == 0) {
            return;
        }
        if (data == null || data.length < requiredDataNumber) {
            throw new CustomException(MUST_NEED_NOTIFICATION_MESSAGE_DATA);
        }
    }
}
