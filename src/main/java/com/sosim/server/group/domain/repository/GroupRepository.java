package com.sosim.server.group.domain.repository;

import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.dao.GroupRepositoryDsl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @EntityGraph(attributePaths = {"participantList", "notificationSettingInfo"})
    Optional<Group> findByIdWithNotificationSettingInfo(long groupId);
}
