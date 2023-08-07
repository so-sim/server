package com.sosim.server.oauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.util.CookieUtil;
import com.sosim.server.oauth.service.OAuthService;
import com.sosim.server.oauth.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static com.sosim.server.common.response.ResponseCode.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/{socialType}")
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping
    public ResponseEntity<?> signup(@PathVariable("socialType") String social, @RequestParam("code") String code,
                                   HttpServletResponse response) throws JsonProcessingException {
        LoginResponse loginResponse = oAuthService.signUp(social, code);
        CookieUtil.setCookieRefreshToken(response, loginResponse.getRefreshToken());

        return new ResponseEntity<>(Response.create(SUCCESS_SIGNUP, loginResponse), SUCCESS_SIGNUP.getHttpStatus());
    }

    @GetMapping
    public ResponseEntity<?> login(@PathVariable("socialType") String social, @RequestParam("code") String code,
                                   HttpServletResponse response) throws JsonProcessingException {
        LoginResponse loginResponse = oAuthService.login(social, code);
        CookieUtil.setCookieRefreshToken(response, loginResponse.getRefreshToken());

        return new ResponseEntity<>(Response.create(SUCCESS_LOGIN, loginResponse), SUCCESS_LOGIN.getHttpStatus());
    }
}