package com.sosim.server.jwt;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class RefreshToken {

    private Long id;

    private String refreshToken;
}
