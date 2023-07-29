package com.sosim.server.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String[] data;

    public static Content create(ContentType contentType, String... data) {
        contentType.checkDataCount(data);
        return Content.builder()
                .contentType(contentType)
                .data(data == null ? new String[0] : data)
                .build();
    }

}
