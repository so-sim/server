package com.sosim.server.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.jwt.JwtService;
import com.sosim.server.oauth.dto.request.OAuthTokenRequest;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import com.sosim.server.oauth.dto.response.LoginResponse;
import com.sosim.server.user.User;
import com.sosim.server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    private final InMemoryClientRegistrationRepository inMemoryRepository;
    private final UserService userService;
    private final JwtService jwtService;

    public LoginResponse signUp(String social, String code) throws JsonProcessingException {
        return createLoginResponse(userService.save(getOAuthUserInfo(social, code)));
    }

    public LoginResponse login(String social, String code) throws JsonProcessingException {
        return createLoginResponse(userService.update(getOAuthUserInfo(social, code)));
    }

    private LoginResponse createLoginResponse(User user) {
        return LoginResponse.toDto(jwtService.createToken(user.getId()), user);
    }

    private OAuthUserRequest getOAuthUserInfo(String social, String code) throws JsonProcessingException {
        ClientRegistration clientRegistration = inMemoryRepository.findByRegistrationId(social);
        OAuthTokenRequest oAuth2Token = getOAuthToken(clientRegistration, code);
        Map<String, Object> oAuthAttributes = getOAuthAttributes(clientRegistration, oAuth2Token);

        return OAuthUserFactory.getOAuthUser(social, oAuthAttributes);
    }

    private OAuthTokenRequest getOAuthToken(ClientRegistration type, String authorizationCode) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return OBJECT_MAPPER.readValue(getResponseBody(
                type.getProviderDetails().getTokenUri(), HttpMethod.POST,
                new HttpEntity<>(setQueryParams(authorizationCode, type), headers)), OAuthTokenRequest.class);
    }

    private MultiValueMap<String, String> setQueryParams(String authorizationCode, ClientRegistration type) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("grant_type", "authorization_code");
        formData.add("redirection_uri", type.getRedirectUri());
        formData.add("client_secret", type.getClientSecret());
        formData.add("client_id", type.getClientId());
        return formData;
    }

    private Map<String, Object> getOAuthAttributes(ClientRegistration type, OAuthTokenRequest token) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + token.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return OBJECT_MAPPER.readValue(getResponseBody(
                type.getProviderDetails().getUserInfoEndpoint().getUri(), HttpMethod.GET,
                new HttpEntity<>(null, headers)), Map.class);
    }

    private String getResponseBody(String uri, HttpMethod method, HttpEntity<?> request) {
        try {
            return new RestTemplate().exchange(uri, method, request, String.class).getBody();
        } catch (HttpClientErrorException ignore) {
            throw new CustomException(ResponseCode.INCORRECT_OAUTH_CODE);
        }
    }
}
