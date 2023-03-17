package com.sosim.server.oauth;

import com.sosim.server.oauth.dto.request.KakaoUserRequest;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;

import java.util.Map;

public class OAuthUserFactory {
    public static OAuthUserRequest getOAuthUser(String social, Map<String, Object> attributes) {
        switch (social) {
//            case "google": return new GoogleUserInfoRequest(attributes);
//            case "naver": return new NaverOAuth2UserInfo(attributes);
            case "kakao": return new KakaoUserRequest(attributes);
            default: throw new IllegalArgumentException("등록되지 않은 소셜 로그인입니다.");
        }
    }
}
