package com.sosim.server.common.util;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String REFRESH_HEADER = "RefreshToken";

    private static Long refreshExpiration;
    private static String domain;

    @Value("${jwt.refresh.expiration}")
    public void setRefreshExpiration(Long refreshExpiration) {
        CookieUtil.refreshExpiration = refreshExpiration;
    }

    @Value("${jwt.cookie.domain}")
    public void setDomain(String domain) {
        CookieUtil.domain = domain;
    }

    public static void setCookieRefreshToken(HttpServletResponse response, String refreshToken) {
        setCookie(refreshToken, refreshExpiration / 1000, response);
    }

    public static void deleteRefreshToken(HttpServletResponse response) {
        setCookie("", 0, response);
    }

    public static String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (REFRESH_HEADER.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static void setCookie(String value, long maxAge, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_HEADER, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(maxAge)
                .domain(domain)
                .path("/")
                .build();

        response.addHeader(SET_COOKIE, cookie.toString());
    }
}
