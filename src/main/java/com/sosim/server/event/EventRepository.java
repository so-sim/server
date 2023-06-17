package com.sosim.server.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIdIn(List<Long> eventIdList);

    @Query("select e from Event e join fetch e.group where e.id = :eventId")
    Optional<Event> findByIdWithGroup(@Param("eventId") long eventId);
}
