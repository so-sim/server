package com.sosim.server.user;

import com.sosim.server.oauth.Social;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialAndSocialId(Social social, Long socialId);
}
