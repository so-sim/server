package com.sosim.server.user;

import com.sosim.server.oauth.Social;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialAndSocialId(Social social, Long socialId);
}
