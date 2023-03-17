package com.sosim.server.jwt;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
