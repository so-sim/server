package com.sosim.server.common.resolver;

import com.sosim.server.security.AuthUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean existAuthUserIdAnnotation = parameter.getParameterAnnotation(AuthUserId.class) != null;
        boolean isUserIdClass = long.class.equals(parameter.getParameterType()) || Long.class.equals(parameter.getParameterType());

        return existAuthUserIdAnnotation && isUserIdClass;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        AuthUserId annotation = parameter.getParameterAnnotation(AuthUserId.class);
        assert annotation != null;
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (annotation.required() || authentication != null) {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            return authUser.getId();
        }
        return 0L;
    }
}
