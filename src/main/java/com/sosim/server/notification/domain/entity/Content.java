package com.sosim.server.notification.domain.entity;

import com.sosim.server.event.Situation;
import com.sosim.server.notification.dto.response.MessageDataDto;
import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import static com.sosim.server.notification.domain.entity.ContentType.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Content {
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Getter(AccessLevel.NONE)
    private String data1;

    @Getter(AccessLevel.NONE)
    private String data2;

    public static Content create(ContentType contentType, String... data) {
        contentType.checkDataCount(data);
        Content content = Content.builder()
                .contentType(contentType)
                .build();
        content.setData(data);
        return content;
    }

    public MessageDataDto getData() {
        MessageDataDto messageDataDto = new MessageDataDto();
        if (REQUEST_PAYMENT.equals(contentType)) {
            messageDataDto.setAmount(Integer.valueOf(data1));
        } else if (CHANGE_CHECK_SITUATION.equals(contentType)) {
            messageDataDto.setNickname(data1);
            messageDataDto.setAfterSituation(Situation.getSituation(data2));
        } else if (CHANGE_FULL_SITUATION.equals(contentType) || CHANGE_NONE_SITUATION.equals(contentType)) {
            messageDataDto.setBeforeSituation(Situation.getSituation(data1));
            messageDataDto.setAfterSituation(Situation.getSituation(data2));
        }
        return messageDataDto;
    }

    private void setData(String[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        data1 = data[0];
        if (data.length > 1) {
            data2 = data[1];
        }
    }

}
