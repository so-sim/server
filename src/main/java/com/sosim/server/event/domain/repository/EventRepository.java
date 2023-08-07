package com.sosim.server.event.domain.repository;

import com.sosim.server.event.domain.entity.Situation;
import com.sosim.server.event.domain.entity.Event;
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
    @Query("UPDATE Event e SET e.situation = :situation " +
            "WHERE e.id IN (:eventIdList)")
    void updateSituationAll(@Param("eventIdList") List<Long> eventIdList, @Param("situation") Situation situation);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.nickname = :newNickname " +
            "WHERE e.nickname IN (:nickname)")
    void updateNicknameAll(@Param("newNickname") String newNickname, @Param("nickname") String nickname);

    @Query("SELECT e FROM Event e JOIN FETCH e.group " +
            "WHERE e.id = :eventId AND e.status = 'ACTIVE'")
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
}
