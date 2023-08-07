package com.sosim.server.oauth.domain.util;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.oauth.dto.request.KakaoUserRequest;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;

import java.util.Map;

public class OAuthUserFactory {
    public static OAuthUserRequest getOAuthUser(String social, Map<String, Object> attributes) {
        switch (social) {
//            case "google": return new GoogleUserInfoRequest(attributes);
//            case "naver": return new NaverOAuth2UserInfo(attributes);
            case "kakao": return new KakaoUserRequest(attributes);
            default: throw new CustomException(ResponseCode.NOT_SUPPORTED_OAUTH);
        }
    }
}
