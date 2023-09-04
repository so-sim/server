package com.sosim.server.participant.domain.entity;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.sosim.server.common.response.ResponseCode.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "PARTICIPANTS")
public class Participant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARTICIPANT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "IS_ADMIN")
    private boolean isAdmin;

    @Builder
    private Participant(User user, Group group, String nickname, boolean isAdmin) {
        this.user = user;
        this.group = group;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        status = Status.ACTIVE;
    }

    public void modifyNickname(Group group, String newNickname) {
        if (nickname.equals(newNickname)) {
            return;
        }
        if (group.existThatNickname(newNickname)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
        }
        if (group.existThatNicknameIgnoreStatus(newNickname)) {
            throw new CustomException(USED_NICKNAME);
        }
        nickname = newNickname;
    }

    public void addGroup(Group group) {
        group.getParticipantList().add(this);
        this.group = group;
    }

    public void withdrawGroup() {
        withdrawGroup(this.group);
    }

    public void withdrawGroup(Group group) {
        checkCanWithdrawGroup(group);
        delete();
        group.removeParticipant(this);
        if (group.hasNoParticipant()) {
            group.delete();
        }
    }

    public void checkCanWithdrawGroup() {
        checkCanWithdrawGroup(this.group);
    }

    public void checkCanWithdrawGroup(Group group) {
        if (isAdmin && group.hasMoreNormalParticipant()) {
            throw new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
        }
    }

    public boolean isMine(long userId) {
        return user.getId().equals(userId);
    }

    public void resign() {
        this.isAdmin = false;
    }

    public void signOn() {
        this.isAdmin = true;
    }

    public boolean isWithdrawGroup() {
        return this.status.equals(Status.DELETED);
    }
}
