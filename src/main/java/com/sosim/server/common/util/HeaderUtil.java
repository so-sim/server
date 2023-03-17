package com.sosim.server.common.util;

import org.springframework.http.ResponseCookie;

import javax.servlet.http.HttpServletResponse;

public class HeaderUtil {

    private static final String COOKIE_HEADER = "Set-Cookie";

    public static void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_HEADER , refreshToken)
                .maxAge(60 * 60 * 24)
                .secure(true)
                .sameSite("Lax")
                .httpOnly(true)
                .secure(true)
                .build();

        response.setHeader(COOKIE_HEADER, cookie.toString());
    }
}
