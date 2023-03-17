package com.sosim.server.jwt;

import com.sosim.server.jwt.dto.response.JwtResponse;
import com.sosim.server.jwt.util.JwtFactory;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtRepository jwtRepository;
    private final JwtFactory jwtFactory;

    public JwtResponse createToken(User user) {
        String refreshToken = jwtFactory.createRefreshToken();
        jwtRepository.save(RefreshToken.create(refreshToken));

        return JwtResponse.create(jwtFactory.createAccessToken(user), refreshToken);
    }
}
