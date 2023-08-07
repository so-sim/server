package com.sosim.server.oauth.dto.request;

import com.sosim.server.oauth.domain.domain.Social;

import java.util.Map;

public class KakaoUserRequest extends OAuthUserRequest {

    private Map<String, Object> kakaoAccountAttributes;

    public KakaoUserRequest(Map<String, Object> attributes) {
        super(attributes);
        kakaoAccountAttributes = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public Long getOAuthId() {
        return (Long) super.attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccountAttributes.get("email");
    }

    @Override
    public Social getOAuthSocial() {
        return Social.KAKAO;
    }
}
