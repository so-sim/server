package com.sosim.server.notification;

import com.sosim.server.common.auditing.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "NOTIFICATIONS")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private long id;

    @Column(name = "USER_ID")
    private long userId;

    @Embedded
    private GroupInfo groupInfo;

    @Enumerated(EnumType.STRING)
    private Content content;

    @Column(name = "VIEW")
    private boolean view;

    @Builder
    public Notification(long userId, long groupId, String groupTitle, Content content) {
        this.userId = userId;
        this.groupInfo = setGroupInfo(groupId, groupTitle);
        this.content = content;
    }

    public long getGroupId() {
        return groupInfo.getGroupId();
    }

    public String getGroupTitle() {
        return groupInfo.getGroupTitle();
    }

    private GroupInfo setGroupInfo(long groupId, String groupTitle) {
        return GroupInfo.builder()
                .groupId(groupId)
                .groupTitle(groupTitle)
                .build();
    }

    public static Notification toEntity(long userId, long groupId, String groupTitle, Content content) {
        return Notification.builder()
                .userId(userId)
                .groupId(groupId)
                .groupTitle(groupTitle)
                .content(content)
                .build();
    }
}


