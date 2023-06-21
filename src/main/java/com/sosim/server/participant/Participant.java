package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
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

    public static Participant create(User user, Group group, String nickname, boolean isAdmin) {
        checkAlreadyIntoGroup(user.getId(), group);
        checkUsedNickname(group, nickname);

        Participant participant = Participant.builder()
                .user(user)
                .group(group)
                .nickname(nickname)
                .isAdmin(isAdmin)
                .build();
        participant.addGroup(group);
        return participant;
    }

    public void modifyNickname(Group group, String newNickname) {
        if (group.existThatNickname(newNickname)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
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
        delete();
        group.removeParticipant(this);
        if (isAdmin && group.hasMoreParticipant()) {
            throw new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
        }
        if (group.hasNoParticipant()) {
            group.delete();
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

    private static void checkUsedNickname(Group group, String nickname) {
        if (group.existThatNickname(nickname)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
        }
    }

    private static void checkAlreadyIntoGroup(long userId, Group group) {
        if (group.hasParticipant(userId)) {
            throw new CustomException(ALREADY_INTO_GROUP);
        }
    }
}
