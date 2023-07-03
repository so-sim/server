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
    @AllArgsConstructor
    private static class Status {
        private int code;
        private String message;
    }

    public static <T> Response<?> create(ResponseCode responseCode, T content) {
        return Response.builder()
                .status(new Status(responseCode.getCode(), responseCode.getMessage()))
                .content(content)
                .build();
    }
}