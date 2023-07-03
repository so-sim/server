package com.sosim.server.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryDsl {
    List<Event> findByIdIn(List<Long> eventIdList);

    @Query("SELECT e FROM Event e JOIN FETCH e.group " +
            "WHERE e.id = :eventId AND e.status = 'ACTIVE'")
    Optional<Event> findByIdWithGroup(@Param("eventId") long eventId);
}
