package com.sosim.server.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String REFRESH_HEADER = "RefreshToken";

    private static Long refreshExpiration;

    @Value("${jwt.refresh.expiration}")
    public void setRefreshExpiration(Long refreshExpiration) {
        CookieUtil.refreshExpiration = refreshExpiration;
    }

    public static void setCookieRefreshToken(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_HEADER, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(refreshExpiration / 1000)
                .path("/")
                .build();

        response.addHeader(SET_COOKIE, cookie.toString());
    }
}
