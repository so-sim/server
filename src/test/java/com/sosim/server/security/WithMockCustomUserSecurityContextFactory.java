package com.sosim.server.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    private static final long USER_ID = 1L;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthUser authUser = AuthUser.crate(USER_ID);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}