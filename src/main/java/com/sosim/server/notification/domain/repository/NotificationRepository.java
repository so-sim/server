package com.sosim.server.notification.domain.repository;

import com.sosim.server.common.auditing.Status;
import com.sosim.server.notification.domain.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("SELECT n FROM Notification n " +
            "WHERE n.userId = :userId ")
    Slice<Notification> findMyNotifications(@Param("userId") long userId, Pageable pageable);

    @Query(value = "SELECT * FROM notifications n " +
            "WHERE n.send_dateTime <= CURRENT_TIMESTAMP", nativeQuery = true)
    List<Notification> findReservedNotifications();

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n " +
            "WHERE n.groupInfo.groupId = :groupId")
    void deleteReservedNotifications(@Param("groupId") long groupId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.content.data1 = :newNickname " +
            "WHERE n.content.data1 = :nickname " +
            "AND n.groupInfo.groupId = :groupId " +
            "AND (n.status <> 'LOCK' OR n.status IS NULL)")
    void updateAllNicknameByGroupIdAndNickname(@Param("groupId") long groupId, @Param("nickname") String nickname, @Param("newNickname") String newNickname);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.groupInfo.groupTitle = :title " +
            "WHERE n.groupInfo.groupId = :groupId")
    void updateAllGroupTitleByGroupId(@Param("groupId") long groupId, @Param("title") String title);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.status = :status " +
            "WHERE n.content.data1 = :nickname " +
            "AND n.groupInfo.groupId = :groupId")
    void updateAllStatusByNicknameAndGroupId(@Param("status") Status status, @Param("nickname") String nickname, @Param("groupId") long groupId);
}
