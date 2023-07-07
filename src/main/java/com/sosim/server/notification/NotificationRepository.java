package com.sosim.server.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT COUNT(*) FROM Notification n " +
            "WHERE n.userId = :userId AND n.createDate >= :time" +
            "AND n.view = 'FALSE'")
    Long countByUserIdBetweenMonth(@Param("userId") long userId, @Param("time") LocalDateTime time);
}
