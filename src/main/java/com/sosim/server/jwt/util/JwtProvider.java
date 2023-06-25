package com.sosim.server.jwt.util;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import io.jsonwebtoken.*;
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
        return Long.valueOf(getClaims(accessKey, accessToken).getSubject());
    }

    private Claims getClaims(String key, String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException | MalformedJwtException | MissingClaimException ex) {
            throw new CustomException(ResponseCode.MODULATION_JWT);
        } catch (ExpiredJwtException ex) {
            throw new CustomException(ResponseCode.EXPIRATION_JWT);
        }
    }
}
