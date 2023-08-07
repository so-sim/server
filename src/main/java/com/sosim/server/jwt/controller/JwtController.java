package com.sosim.server.jwt.controller;

import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.common.util.CookieUtil;
import com.sosim.server.jwt.service.JwtService;
import com.sosim.server.jwt.dto.response.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class JwtController {

    private final JwtService jwtService;

    @GetMapping("/refresh")
    public ResponseEntity<?> reIssueToken(HttpServletRequest request, HttpServletResponse response) {
        JwtResponse refresh = jwtService.refresh(CookieUtil.getRefreshToken(request));
        CookieUtil.setCookieRefreshToken(response, refresh.getRefreshToken());
        ResponseCode successRefresh = ResponseCode.SUCCESS_REFRESH_TOKEN;

        return new ResponseEntity<>(Response.create(successRefresh, refresh), successRefresh.getHttpStatus());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtService.delete(CookieUtil.getRefreshToken(request));
        CookieUtil.deleteRefreshToken(response);
        ResponseCode successLogout = ResponseCode.SUCCESS_LOGOUT;

        return new ResponseEntity<>(Response.create(successLogout, null), successLogout.getHttpStatus());
    }
}
