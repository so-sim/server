package com.sosim.server.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseType;
import com.sosim.server.common.util.HeaderUtil;
import com.sosim.server.jwt.dto.response.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login/oauth2/code/{social}")
    public ResponseEntity<?> login(@PathVariable("social") String social, @RequestParam("code") String code,
                                   HttpServletResponse response) throws JsonProcessingException {
        JwtResponse jwtResponse = oAuthService.login(social, code);
        HeaderUtil.setHeaderRefreshToken(response, jwtResponse.getRefreshToken());
        ResponseType successLogin = ResponseType.SUCCESS_LOGIN;

        return new ResponseEntity<>(Response.create(successLogin, jwtResponse), successLogin.getHttpStatus());
    }
}