package com.sosim.server.oauth.dto.request;

import com.sosim.server.oauth.domain.domain.Social;

import java.util.Map;

public abstract class OAuthUserRequest {
    protected Map<String, Object> attributes;

    protected OAuthUserRequest(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract Long getOAuthId();

    public abstract String getEmail();

    public abstract Social getOAuthSocial();
}
