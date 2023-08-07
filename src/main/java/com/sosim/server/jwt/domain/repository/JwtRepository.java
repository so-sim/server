package com.sosim.server.jwt.domain.repository;

import com.sosim.server.jwt.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
