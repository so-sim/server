package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.participant.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.common.response.ResponseCode.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "GROUPS")
public class Group extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "ADMIN_ID")
    private long adminId;

    @Column(name = "ADMIN_NICKNAME")
    private String adminNickname;

    @Column(name = "COVER_COLOR")
    private String coverColor;

    @Column(name = "GROUP_TYPE")
    private String groupType;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Participant> participantList = new ArrayList<>();

    @Builder
    private Group(String title, long adminId, String adminNickname, String coverColor, String groupType) {
        this.title = title;
        this.adminId = adminId;
        this.adminNickname = adminNickname;
        this.coverColor = coverColor;
        this.groupType = groupType;
        status = ACTIVE;
    }

    public static Group create(long adminId, CreateGroupRequest createGroupRequest) {
        return Group.builder()
                .title(createGroupRequest.getTitle())
                .adminId(adminId)
                .adminNickname(createGroupRequest.getNickname())
                .groupType(createGroupRequest.getGroupType())
                .coverColor(createGroupRequest.getCoverColor())
                .build();
    }

    public void update(long userId, UpdateGroupRequest updateGroupRequest) {
        checkIsAdmin(userId);
        this.title = updateGroupRequest.getTitle();
        this.groupType = updateGroupRequest.getGroupType();
        this.coverColor = updateGroupRequest.getCoverColorType();
    }

    public void deleteGroup(long userId) {
        checkIsAdmin(userId);
        checkNotRemainParticipantWithoutAdmin();

        deleteGroupAndAdmin();
    }

    public void modifyAdmin(long userId, String newAdminName) {
        checkIsAdmin(userId);
        Participant newAdmin = checkNewAdminIsInGroup(newAdminName);

        adminId = newAdmin.getUser().getId();
        adminNickname = newAdminName;
    }

    private Participant checkNewAdminIsInGroup(String newAdminName) {
        return findParticipantByNickname(newAdminName);
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
                .anyMatch(p -> p.getUser().getId().equals(userId));
    }

    private Participant findParticipantByNickname(String nickname) {
        return participantList.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new CustomException(NONE_PARTICIPANT));
    }

    public boolean existThatNickname(String nickname) {
        return getParticipantList().stream()
                .anyMatch(p -> p.getNickname().equals(nickname));
    }

    public boolean isAdminNickname(String nickname) {
        return nickname.equals(adminNickname);
    }

    public boolean isAdminUser(long userId) {
        return adminId == userId;
    }

    public int getNumberOfParticipants() {
        return (int) participantList.stream()
                .filter(p -> ACTIVE.equals(p.getStatus()))
                .count();
    }

    private void deleteGroupAndAdmin() {
        Participant adminParticipant = participantList.get(0);
        adminParticipant.withdrawGroup(this);
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
}
