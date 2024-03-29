package com.sosim.server.group.domain.repository;

import com.sosim.server.group.domain.entity.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryDsl {
    @Query("select g from Group g where g.id = :groupId and g.status = 'ACTIVE'")
    Optional<Group> findById(@Param("groupId") long groupId);

    @Query("SELECT g FROM Group g " +
            "JOIN FETCH g.participantList p " +
            "WHERE g.id = :groupId AND g.status = 'ACTIVE' " +
            "AND p.status = 'ACTIVE'")
    @EntityGraph(attributePaths = {"participantList"})
    Optional<Group> findByIdWithParticipants(@Param("groupId") long groupId);

    @Query("SELECT g FROM Group g " +
            "WHERE g.id = :groupId AND g.status = 'ACTIVE'")
    @EntityGraph(attributePaths = {"participantList"})
    Optional<Group> findByIdWithParticipantsIgnoreStatus(@Param("groupId") long groupId);

    @Query("SELECT g FROM Group g " +
            "WHERE g.id = :groupId AND g.status = 'ACTIVE'")
    @EntityGraph(attributePaths = {"participantList", "notificationSettingInfo"})
    Optional<Group> findByIdWithNotificationSettingInfo(@Param("groupId") long groupId);

    @Query("SELECT g FROM Group g " +
            "WHERE g.status = 'ACTIVE' " +
            "AND g.notificationSettingInfo.nextSendDateTime = :now " +
            "AND g.notificationSettingInfo.enableNotification = true")
    @EntityGraph(attributePaths = {"notificationSettingInfo"})
    List<Group> findToNextSendDateTime(@Param("now") LocalDateTime now);
}
