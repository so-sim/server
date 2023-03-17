package com.sosim.server.jwt;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash
@Builder(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String refreshToken;

    public static RefreshToken create(String refreshToken) {
        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .build();
    }
}
