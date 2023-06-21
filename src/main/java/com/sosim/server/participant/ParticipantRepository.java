package com.sosim.server.participant;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, java.lang.Long> {

    @Query("select p from Participant p where p.user.id = :userId " +
            "and p.group.id = :groupId and p.status = 'ACTIVE'")
    Optional<Participant> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Query("select p from Participant p where p.nickname = :nickname " +
           "and p.group.id = :groupId and p.status = 'ACTIVE'")
    Optional<Participant> findByNicknameAndGroupId(@Param("nickname") String nickname, @Param("groupId") Long groupId);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.group.id = :groupId " +
            "AND p.nickname != :adminNickname " +
            "AND p.status = 'ACTIVE' " +
            "ORDER BY p.nickname ASC")
    List<Participant> findGroupNormalParticipants(@Param("groupId") long groupId, @Param("adminNickname") String adminNickname);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND p.user.id = :userId " +
            "AND p.isAdmin = true")
    List<Participant> findByUserIdAndIsAdminIsTrue(@Param("userId") long userId);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.status = 'ACTIVE' AND p.user.id = :userId")
    @EntityGraph(attributePaths = {"group"})
    List<Participant> findByUserIdWithGroup(@Param("userId") long userId);
}
