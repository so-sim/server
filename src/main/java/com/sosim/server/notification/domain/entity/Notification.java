package com.sosim.server.notification.domain.entity;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.notification.dto.response.MessageDataDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.sosim.server.common.response.ResponseCode.IS_NOT_NOTIFICATION_RECEIVER;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_ids", joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "line_idx")
    private List<Long> eventIdList;

    @Builder
    public Notification(long userId, long groupId, String groupTitle, Content content, LocalDateTime sendDateTime, boolean reserved, List<Long> eventIdList) {
        this.userId = userId;
        this.groupInfo = setGroupInfo(groupId, groupTitle);
        this.content = content;
        this.sendDateTime = setSendDateTime(sendDateTime);
        this.reserved = reserved;
        this.eventIdList = eventIdList;
    }

    public static Notification toEntity(long userId, Group group, Content content) {
        return toEntity(userId, group.getId(), group.getTitle(), content, null);
    }

    public static Notification toEntity(long userId, Group group, Content content, List<Long> eventIdList) {
        return toEntity(userId, group.getId(), group.getTitle(), content, eventIdList);
    }

    public static Notification toEntity(long userId, long groupId, String groupTitle, Content content, List<Long> eventIdList) {
        return Notification.builder()
                .userId(userId)
                .groupId(groupId)
                .groupTitle(groupTitle)
                .content(content)
                .eventIdList(eventIdList)
                .build();
    }

    public void sendComplete() {
        reserved = false;
    }

    public void read(long userId) {
        checkUserIsReceiver(userId);
        view = true;
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

    public MessageDataDto getMessageData() {
        return content.getData();
    }

    private void checkUserIsReceiver(long userId) {
        if (this.userId != userId) {
            throw new CustomException(IS_NOT_NOTIFICATION_RECEIVER);
        }
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


