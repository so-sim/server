package com.sosim.server.jwt.domain.util;

import com.sosim.server.common.advice.exception.CustomException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static com.sosim.server.common.response.ResponseCode.*;

@Component
public class JwtProvider {

    @Value("${jwt.access.key}")
    private String accessKey;

    @Value("${jwt.refresh.key}")
    private String refreshKey;

    public boolean checkRenewRefreshToken(String refreshToken) {
        try {
            getClaims(refreshKey, refreshToken, true);
        } catch (CustomException e) {
            if (e.getResponseCode().equals(EXPIRATION_ACCESS)) return true;
            else throw e;
        }
        return false;
    }

    public Long getUserId(String accessToken) {
        return Long.valueOf(getClaims(accessKey, accessToken, false).getSubject());
    }

    private Claims getClaims(String key, String token, boolean isRefresh) {
        try {
            return Jwts.parser()
                    .setSigningKey(key.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException | MalformedJwtException | MissingClaimException ex) {
            if (isRefresh) {
                throw new CustomException(MODULATION_REFRESH);
            }
            throw new CustomException(MODULATION_ACCESS);
        } catch (ExpiredJwtException ex) {
            throw new CustomException(EXPIRATION_ACCESS);
        }
    }
}
