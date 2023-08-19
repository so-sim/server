package com.sosim.server.group.domain.entity;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.participant.domain.entity.Participant;
import com.sosim.server.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.common.response.ResponseCode.*;

@Entity
@Getter()
@NoArgsConstructor
@Table(name = "`GROUPS`")
public class Group extends BaseTimeEntity {
    public static final int DEFAULT_REPEAT_CYCLE = 1;
    public static final LocalTime DEFAULT_SEND_TIME = LocalTime.of(12, 0);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "COVER_COLOR")
    private String coverColor;

    @Column(name = "GROUP_TYPE")
    private String groupType;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Participant> participantList = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "NOTIFICATION_SETTING_INFO_ID")
    private NotificationSettingInfo notificationSettingInfo;

    @Column
    private LocalDateTime reservedSendNotificationDateTime;

    @Builder
    private Group(String title, String coverColor, String groupType) {
        this.title = title;
        this.coverColor = coverColor;
        this.groupType = groupType;
        status = ACTIVE;
    }

    public Participant createParticipant(User user, String nickname, boolean isAdmin) {
        checkAlreadyIntoGroup(user.getId());
        checkUsedNickname(nickname);

        Participant participant = Participant.builder()
                .user(user)
                .group(this)
                .nickname(nickname)
                .isAdmin(isAdmin)
                .build();
        participant.addGroup(this);
        return participant;
    }

    public void update(long userId, ModifyGroupRequest updateGroupRequest) {
        checkIsAdmin(userId);
        this.title = updateGroupRequest.getTitle();
        this.groupType = updateGroupRequest.getType();
        this.coverColor = updateGroupRequest.getCoverColor();
    }

    public void deleteGroup(long userId) {
        checkIsAdmin(userId);
        checkNotRemainParticipantWithoutAdmin();

        deleteGroupAndAdmin();
    }

    public void modifyAdmin(long userId, String newAdminNickname) {
        Participant preAdmin = getAdminParticipant();
        checkIsAdmin(userId, preAdmin);
        Participant newAdmin = checkNewAdminIsInGroup(newAdminNickname);

        preAdmin.resign();
        newAdmin.signOn();
    }

    public boolean removeParticipant(Participant participant) {
        return participantList.remove(participant);
    }

    public boolean hasNoParticipant() {
        return participantList.stream()
                .noneMatch(Participant::isActive);
    }

    public boolean hasParticipant(long userId) {
        return participantList.stream()
                .anyMatch(p -> p.isActive() && p.isMine(userId));
    }

    public boolean hasMoreNormalParticipant() {
        return participantList.stream()
                .anyMatch(p -> p.isActive() && !p.isAdmin());
    }

    public Participant getAdminParticipant() {
        return participantList.stream()
                .filter(Participant::isAdmin)
                .findFirst()
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_ADMIN));
    }

    public NotificationSettingInfo getNotificationSettingInfo(long userId) {
        checkIsAdmin(userId);
        return notificationSettingInfo;
    }

    public LocalDateTime getNextNotifyDateTime() {
        return notificationSettingInfo.getNextNotifyDateTime();
    }

    private Participant getParticipantByNickname(String nickname) {
        return participantList.stream()
                .filter(p -> p.isActive() && p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT));
    }

    public boolean existThatNickname(String nickname) {
        return getParticipantList().stream()
                .anyMatch(p -> p.isActive() && p.getNickname().equals(nickname));
    }

    public boolean isAdminUser(long userId) {
        Participant admin = getAdminParticipant();
        return admin.isMine(userId);
    }

    public int getNumberOfParticipants() {
        return (int) participantList.stream()
                .filter(p -> ACTIVE.equals(p.getStatus()))
                .count();
    }

    public void changeNotificationSettingInfo(long userId, NotificationSettingInfo settingInfo) {
        checkIsAdmin(userId);
        notificationSettingInfo = settingInfo;
    }

    public void setNextSendNotificationTime() {
        this.reservedSendNotificationDateTime = notificationSettingInfo.calculateNextSendDateTime();
    }

    public boolean isReserveNotificationOn() {
        return notificationSettingInfo.isEnableNotification();
    }

    private boolean noSettingInfo() {
        return notificationSettingInfo == null;
    }

    private boolean isSameSettingType(NotificationSettingInfo newSettingInfo) {
        if (noSettingInfo()) {
            return false;
        }
        return notificationSettingInfo.getSettingType().equals(newSettingInfo.getSettingType());
    }

    private void deleteGroupAndAdmin() {
        Participant adminParticipant = getAdminParticipant();
        adminParticipant.withdrawGroup(this);
    }

    private Participant checkNewAdminIsInGroup(String newAdminName) {
        return getParticipantByNickname(newAdminName);
    }

    private void checkNotRemainParticipantWithoutAdmin() {
        if (getNumberOfParticipants() > 1) {
            throw new CustomException(NONE_ZERO_PARTICIPANT);
        }
    }

    private void checkIsAdmin(long userId) {
        if (!isAdminUser(userId)) {
            throw new CustomException(NONE_ADMIN);
        }
    }

    private void checkIsAdmin(long userId, Participant adminParticipant) {
        if (!adminParticipant.isMine(userId)) {
            throw new CustomException(NONE_ADMIN);
        }
    }

    private void checkUsedNickname(String nickname) {
        if (existThatNickname(nickname)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
        }
    }

    private void checkAlreadyIntoGroup(long userId) {
        if (hasParticipant(userId)) {
            throw new CustomException(ALREADY_INTO_GROUP);
        }
    }

}
