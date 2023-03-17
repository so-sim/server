package com.sosim.server.oauth;

import com.sosim.server.oauth.dto.request.KakaoUserRequest;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;

import java.util.Map;

public class OAuthUserFactory {
    public static OAuthUserRequest getOAuth2UserInfo(Social social, Map<String, Object> attributes) {
        switch (social) {
//            case GOOGLE: return new GoogleUserInfoRequest(attributes);
//            case NAVER: return new NaverOAuth2UserInfo(attributes);
            case KAKAO: return new KakaoUserRequest(attributes);
            default: throw new IllegalArgumentException("등록되지 않은 소셜 로그인입니다.");
        }
    }
}
