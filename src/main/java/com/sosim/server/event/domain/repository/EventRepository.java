package com.sosim.server.event.domain.repository;

import com.sosim.server.event.domain.entity.Situation;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.group.domain.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryDsl {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET " +
            "e.preSituation = CASE :situation WHEN 'FULL' THEN e.situation ELSE e.preSituation END, " +
            "e.situation = :situation " +
            "WHERE e.id IN (:eventIdList)")
    void updateSituationAll(@Param("eventIdList") List<Long> eventIdList, @Param("situation") Situation situation);

    @Modifying
    @Query("UPDATE Event e SET e.nickname = :newNickname " +
            "WHERE e.nickname IN (:nickname) AND " +
            "e.group.id = :groupId AND e.status <> 'LOCK'")
    void updateNicknameAll(@Param("newNickname") String newNickname, @Param("nickname") String nickname, @Param("groupId") long groupId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.id = :eventId AND e.status = 'ACTIVE'")
    @EntityGraph(attributePaths = {"user", "group"})
    Optional<Event> findByIdWithGroup(@Param("eventId") long eventId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.id IN (:eventIdList)")
    @EntityGraph(attributePaths = {"user", "group"})
    List<Event> findAllById(@Param("eventIdList") List<Long> eventIdList);

    @Query("SELECT e FROM Event e " +
            "WHERE e.id IN (:eventIdList)")
    @EntityGraph(attributePaths = {"user", "group"})
    Page<Event> findAllById(@Param("eventIdList") List<Long> eventIdList, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.group.id = :groupId " +
            "AND e.id IN (:eventIdList) ")
    @EntityGraph(attributePaths = {"user", "group"})
    List<Event> findAllByEventIdList(@Param("groupId") long groupId, @Param("eventIdList") List<Long> eventIdList);

    @Query("SELECT e FROM Event e " +
            "WHERE e.group.id = :groupId " +
            "AND e.user.id = :userId " +
            "AND e.id IN (:eventIdList) ")
    @EntityGraph(attributePaths = {"user", "group"})
    List<Event> findAllByEventIdList(@Param("userId") long userId, @Param("groupId") long groupId, @Param("eventIdList") List<Long> eventIdList);

    @Query("SELECT e FROM Event e " +
            "WHERE e.status = 'ACTIVE' " +
            "AND e.situation = 'NONE' " +
            "AND e.group IN (:groups) " +
            "ORDER BY e.user.id ASC, e.group.id ASC")
    @EntityGraph(attributePaths = {"user", "group", "group.participantList"})
    List<Event> findNoneEventsInGroups(@Param("groups") List<Group> groups);

    @Modifying
    @Query("UPDATE Event e SET " +
            "e.status = 'LOCK' " +
            "WHERE e.nickname = :nickname AND e.group = :group")
    void lockEvent(@Param("nickname") String nickname, @Param("group") Group group);
}
