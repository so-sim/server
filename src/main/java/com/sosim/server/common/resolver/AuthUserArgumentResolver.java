package com.sosim.server.common.resolver;

import com.sosim.server.security.AuthUser;
import org.springframework.core.MethodParameter;
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
        try {
            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return authUser.getId();
        } catch (ClassCastException e) {
            return 0L;
        }
    }
}
