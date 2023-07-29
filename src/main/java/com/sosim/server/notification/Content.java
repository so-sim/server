package com.sosim.server.notification;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import static com.sosim.server.notification.ContentType.CHANGE_ADMIN;
import static com.sosim.server.notification.ContentType.PAYMENT_DATE;

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
        checkDataByContentType(contentType, data);
        return Content.builder()
                .contentType(contentType)
                .data(data)
                .build();
    }

    private static void checkDataByContentType(ContentType type, String data) {
        if (!(PAYMENT_DATE.equals(type) || CHANGE_ADMIN.equals(type)) && data == null) {
            throw new CustomException(ResponseCode.NOT_NULL_NOTIFICATION_DATA);
        }
    }

}
