package com.sosim.server.common.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Response<T> {
    /**
     * 요청에 대한 응답 메시지
     */
    private Status status;

    /**
     * 요청에 대한 응답 데이터
     */
    private T content;

    @Getter
    @NoArgsConstructor
    private static class Status {
        private String code;
        private String message;
    }

    public static <T> Response<?> create(T content) {
        return Response.builder()
                .status(new Status())
                .content(content)
                .build();
    }
}