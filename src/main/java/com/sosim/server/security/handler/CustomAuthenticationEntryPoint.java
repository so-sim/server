package com.sosim.server.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.response.Response;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.sosim.server.common.response.ResponseCode.*;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(NOT_EXIST_TOKEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Response<?> responseValue = Response.create(NOT_EXIST_TOKEN, null);

        new ObjectMapper().writeValue(response.getOutputStream(), responseValue);
    }
}
