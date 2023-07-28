package com.sosim.server.notification;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "NOTIFICATIONS")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @Column(name = "USER_ID")
    private long userId;

    @Embedded
    private GroupInfo groupInfo;

    @Embedded
    private Content content;

    @Column(name = "VIEW")
    private boolean view;

    @Column(name = "SEND_DATETIME")
    private LocalDateTime sendDateTime;

    @Column(name = "RESERVED")
    private boolean reserved;

    @Builder
    public Notification(long userId, long groupId, String groupTitle, Content content, LocalDateTime sendDateTime, boolean reserved) {
        this.userId = userId;
        this.groupInfo = setGroupInfo(groupId, groupTitle);
        this.content = content;
        this.sendDateTime = setSendDateTime(sendDateTime);
        this.reserved = reserved;
    }

    public static Notification toEntity(long userId, Group group, Content content) {
        return toEntity(userId, group.getId(), group.getTitle(), content);
    }

    public static Notification toEntity(long userId, long groupId, String groupTitle, Content content) {
        return Notification.builder()
                .userId(userId)
                .groupId(groupId)
                .groupTitle(groupTitle)
                .content(content)
                .build();
    }

    public void sendComplete() {
        reserved = false;
    }

    public long getGroupId() {
        return groupInfo.getGroupId();
    }

    public String getGroupTitle() {
        return groupInfo.getGroupTitle();
    }

    public String getCategory() {
        return content.getContentType().getCategory();
    }

    public String getSummary() {
        return content.getContentType().getCategory();
    }

    public String getType() {
        return content.getContentType().getType();
    }

    public String getMessage() {
        return content.getMessage(groupInfo.getGroupTitle());
    }

    private LocalDateTime setSendDateTime(LocalDateTime sendDateTime) {
        return sendDateTime == null ? LocalDateTime.now() : sendDateTime;
    }

    private GroupInfo setGroupInfo(long groupId, String groupTitle) {
        return GroupInfo.builder()
                .groupId(groupId)
                .groupTitle(groupTitle)
                .build();
    }
}


