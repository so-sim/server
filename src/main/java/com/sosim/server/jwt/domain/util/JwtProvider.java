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
            getClaims(refreshKey, refreshToken);
        } catch (CustomException e) {
            if (e.getResponseCode().equals(EXPIRATION_JWT)) return true;
            else throw e;
        }
        return false;
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
            throw new CustomException(MODULATION_JWT);
        } catch (ExpiredJwtException ex) {
            throw new CustomException(EXPIRATION_JWT);
        }
    }
}
