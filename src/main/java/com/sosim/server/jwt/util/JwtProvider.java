package com.sosim.server.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtProvider {

    @Value("${jwt.access.key}")
    private String accessKey;

    @Value("${jwt.refresh.key}")
    private String refreshKey;

    public boolean checkRenewRefreshToken(String refreshToken, Long time){
        Instant expiredTime = getClaims(refreshKey, refreshToken).getExpiration().toInstant();

        return Instant.now().until(expiredTime, ChronoUnit.DAYS) < time;
    }

    public Long getUserId(String accessToken) {
        return Long.valueOf(getClaims(accessToken, accessToken).getSubject());
    }

    private Claims getClaims(String key, String token){
        return Jwts.parser()
                .setSigningKey(key.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
