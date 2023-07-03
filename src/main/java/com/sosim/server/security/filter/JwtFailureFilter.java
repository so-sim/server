package com.sosim.server.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFailureFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            responseJwtError(response, e);
        }
    }

    public void responseJwtError(HttpServletResponse response, CustomException e) throws IOException {
        response.setStatus(e.getResponseCode().getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Response<?> responseValue = Response.create(e.getResponseCode(), null);

        new ObjectMapper().writeValue(response.getOutputStream(), responseValue);
    }
}
