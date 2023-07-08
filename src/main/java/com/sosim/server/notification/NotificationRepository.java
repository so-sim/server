package com.sosim.server.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT COUNT(*) FROM Notification n " +
            "WHERE n.userId = :userId AND n.createDate >= :time " +
            "AND n.view = false")
    Long countByUserIdBetweenMonth(@Param("userId") long userId, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.view = true " +
            "WHERE n.userId = :userId")
    void updateViewByUserId(@Param("userId") long userId);

    Slice<Notification> findByUserIdAndCreateDateGreaterThan(long userId, LocalDateTime time, Pageable pageable);
}
