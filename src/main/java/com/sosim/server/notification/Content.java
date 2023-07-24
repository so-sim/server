package com.sosim.server.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Content {
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String data;

    public static Content create(ContentType contentType) {
        return create(contentType, null);
    }

    public static Content create(ContentType contentType, String data) {
        //TODO 납부 요청, 승인대기 Type은 data가 null이면 안됨!
        return Content.builder()
                .contentType(contentType)
                .data(data)
                .build();
    }

    public String getMessage(String groupTitle) {
        return String.format(contentType.getMessageFormat(), groupTitle, data);
    }

}
