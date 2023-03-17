package com.sosim.server.oauth.dto.request;

import com.sosim.server.oauth.Social;

import java.util.Map;

public abstract class OAuthUserRequest {
    protected Map<String, Object> attributes;

    protected OAuthUserRequest(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getOAuth2Id();

    public abstract String getEmail();

    public abstract Social getOAuthSocial();
}
