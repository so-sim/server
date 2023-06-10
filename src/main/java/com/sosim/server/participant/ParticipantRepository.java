package com.sosim.server.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, java.lang.Long> {
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
    boolean existsByGroupIdAndNickname(Long groupId, String nickname);

    @Query("select p from Participant p where p.user.id = : userId " +
            "and p.group.id = : groupId and p.status = 'ACTIVE'")
    Optional<Participant> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

   @Query ("select p from Participant p where p.nickname = : nickname " +
           "and p.group.id = : groupId and p.status = 'ACTIVE'")
    Optional<Participant> findByNicknameAndGroupId(@Param("userId") String nickname, @Param("groupId") Long groupId);
}
