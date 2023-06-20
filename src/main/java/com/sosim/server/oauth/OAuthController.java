package com.sosim.server.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.common.util.CookieUtil;
import com.sosim.server.jwt.dto.response.JwtResponse;
import com.sosim.server.oauth.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

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
        ResponseCode successSignup = ResponseCode.SUCCESS_SIGNUP;

        return new ResponseEntity<>(Response.create(successSignup, loginResponse), successSignup.getHttpStatus());
    }

    @GetMapping
    public ResponseEntity<?> login(@PathVariable("socialType") String social, @RequestParam("code") String code,
                                   HttpServletResponse response) throws JsonProcessingException {
        LoginResponse loginResponse = oAuthService.login(social, code);
        CookieUtil.setCookieRefreshToken(response, loginResponse.getRefreshToken());
        ResponseCode successLogin = ResponseCode.SUCCESS_LOGIN;

        return new ResponseEntity<>(Response.create(successLogin, loginResponse), successLogin.getHttpStatus());
    }
}