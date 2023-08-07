package com.sosim.server.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sosim.server.jwt.dto.response.JwtResponse;
import com.sosim.server.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    private long userId;

    private String email;

    public static LoginResponse toDto(JwtResponse jwtResponse, User user) {
        return LoginResponse.builder()
                .accessToken(jwtResponse.getAccessToken())
                .refreshToken(jwtResponse.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }
}
