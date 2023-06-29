package com.sosim.server.security.filter;

import com.sosim.server.jwt.util.JwtProvider;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if(authorizationHeader == null){
            chain.doFilter(request, response);
            return;
        }
        setAuthPrincipal(getToken(authorizationHeader));

        chain.doFilter(request, response);
    }

    private String getToken(String authorizationHeader){
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private void setAuthPrincipal(String token) {
        AuthUser authUser = AuthUser.crate(jwtProvider.getUserId(token));
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
