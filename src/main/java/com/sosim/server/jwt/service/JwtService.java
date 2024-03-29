package com.sosim.server.jwt.service;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.jwt.domain.entity.RefreshToken;
import com.sosim.server.jwt.domain.repository.JwtRepository;
import com.sosim.server.jwt.dto.response.JwtResponse;
import com.sosim.server.jwt.domain.util.JwtFactory;
import com.sosim.server.jwt.domain.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtRepository jwtRepository;
    private final JwtFactory jwtFactory;
    private final JwtProvider jwtProvider;

    public JwtResponse createToken(Long userId) {
        String refreshToken;
        Optional<RefreshToken> optional = jwtRepository.findById(userId);

        if (optional.isEmpty()) {
            refreshToken = jwtFactory.createRefreshToken();
            jwtRepository.save(RefreshToken.create(userId, refreshToken));
        } else {
            refreshToken = optional.get().getRefreshToken();
        }

        return JwtResponse.create(jwtFactory.createAccessToken(userId), refreshToken);
    }

    public JwtResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ResponseCode.NOT_EXIST_TOKEN_COOKIE);
        }

        RefreshToken refreshTokenEntity = getRefreshTokenEntity(refreshToken);

        if (jwtProvider.checkRenewRefreshToken(refreshToken)) {
            return createToken(refreshTokenEntity.getUserId());
        }

        return JwtResponse.create(jwtFactory.createAccessToken(refreshTokenEntity.getUserId()), refreshToken);
    }

    public void deleteToken(long userId) {
        jwtRepository.deleteById(userId);
    }

    private RefreshToken getRefreshTokenEntity(String refreshToken) {
        return jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUNT_REFRESH));
    }
}
