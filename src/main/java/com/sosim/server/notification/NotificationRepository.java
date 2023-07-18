package com.sosim.server.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT COUNT(*) FROM Notification n " +
            "WHERE n.userId = :userId AND n.createDate >= :time " +
            "AND n.view = false")
    Long countByUserIdBetweenMonth(@Param("userId") long userId, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.view = true " +
            "WHERE n.userId = :userId")
    void updateViewByUserId(@Param("userId") long userId);

    @Query("SELECT * FROM Notification n " +
            "WHERE n.userId = :userId " +
            "AND n.reserved = false " +
            "AND n.sentDateTime >= :time")
    Slice<Notification> findMyNotifications(@Param("userId") long userId, @Param("time") LocalDateTime time, Pageable pageable);

    @Query("SELECT * FROM Notification n " +
            "WHERE n.sentDate <= CURRENT_TIMESTAMP AND n.reserved = true ")
    List<Notification> findReservedNotifications();

    @Query("DELETE FROM Notification n " +
            "WHERE n.groupId = :groupId AND n.reserved = true")
    void deleteReservedNotifications(@Param("groupId") long groupId);
}
