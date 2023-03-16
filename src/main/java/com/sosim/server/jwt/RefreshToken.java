package com.sosim.server.jwt;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash
@Builder(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    private String id;

    private String refreshToken;
}
