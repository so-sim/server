package com.sosim.server.participant.domain.repository;

import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.participant.domain.entity.Participant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByUserIdAndGroupId(long userId, long groupId);

    @Query("select p from Participant p where p.nickname = :nickname " +
           "and p.group.id = :groupId")
    @EntityGraph(attributePaths = {"user"})
    Optional<Participant> findByNicknameAndGroupId(@Param("nickname") String nickname, @Param("groupId") Long groupId);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.group.id = :groupId " +
            "AND p.nickname != :adminNickname " +
            "AND p.status = 'ACTIVE' " +
            "ORDER BY p.nickname ASC")
    List<Participant> findGroupNormalParticipants(@Param("groupId") long groupId, @Param("adminNickname") String adminNickname);

    @Query("SELECT DISTINCT p FROM Participant p " +
            "JOIN FETCH p.group g " +
            "JOIN FETCH g.participantList " +
            "WHERE p.status = 'ACTIVE' " +
            "AND p.user.id = :userId AND p.isAdmin = true")
    List<Participant> findByUserIdAndIsAdminIsTrue(@Param("userId") long userId);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.status = 'ACTIVE' AND p.user.id = :userId")
    @EntityGraph(attributePaths = {"group"})
    List<Participant> findByUserIdWithGroup(@Param("userId") long userId);

    List<Participant> findByGroupAndNicknameContainsIgnoreCase(Group group, String nickname);
  
    @Query("SELECT p.user.id FROM Participant p " +
            "WHERE p.status = 'ACTIVE' AND p.group.id = :groupId")
    List<Long> getReceiverUserIdList(@Param("groupId") long groupId);

    List<Participant> findAllByNicknameInAndGroupAndStatus(List<String> nicknames, Group group, Status status);

    boolean existsByGroupAndNickname(Group group, String nickname);
}
