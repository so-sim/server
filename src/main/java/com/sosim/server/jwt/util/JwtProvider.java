package com.sosim.server.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtProvider {

    @Value("${jwt.access.key}")
    private String accessKey;

    @Value("${jwt.refresh.key}")
    private String refreshKey;

    public void verifyAccessToken(String accessToken){
        verify(accessKey, accessToken);
    }

    public void verifyRefreshToken(String refreshToken){
        verify(refreshKey, refreshToken);
    }

    public boolean checkRenewRefreshToken(String refreshToken, Long time){
        Instant expiredTime = getClaims(refreshKey, refreshToken).getExpiration().toInstant();

        return Instant.now().until(expiredTime, ChronoUnit.DAYS) < time;
    }

    private void verify(String key, String token) {
        Jwts.parser()
                .setSigningKey(key.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims getClaims(String key, String token){
        return Jwts.parser()
                .setSigningKey(key.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
