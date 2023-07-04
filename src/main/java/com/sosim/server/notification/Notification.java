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

    @Column(name = "GROUP_ID")
    private long groupId;

    @Enumerated(EnumType.STRING)
    private Content content;

    @Column(name = "READ")
    private boolean read;

    @Builder
    public Notification(long userId, long groupId, Content content) {
        this.userId = userId;
        this.groupId = groupId;
        this.content = content;
    }
}


