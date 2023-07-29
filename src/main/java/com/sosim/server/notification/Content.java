package com.sosim.server.notification;

import lombok.*;

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

    public String[] getData() {
        return new String[] {data1, data2};
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
