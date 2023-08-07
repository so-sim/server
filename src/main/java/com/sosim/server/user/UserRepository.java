package com.sosim.server.user;

import com.sosim.server.oauth.domain.domain.Social;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.social = :social and u.socialId = :socialId and u.status = 'ACTIVE'")
    Optional<User> findBySocialAndSocialId(@Param("social") Social social, @Param("socialId") Long socialId);
}
