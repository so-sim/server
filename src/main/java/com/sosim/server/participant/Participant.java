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

import static com.sosim.server.common.response.ResponseCode.ALREADY_USE_NICKNAME;

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

    @Builder
    private Participant(User user, Group group, String nickname) {
        this.user = user;
        this.group = group;
        this.nickname = nickname;
        status = Status.ACTIVE;
    }

    public static Participant create(User user, String nickname) {
        return Participant.builder()
                .user(user)
                .nickname(nickname)
                .build();
    }

    public void modifyNickname(Group group, String newNickname) {
        if (group.existThatNickname(newNickname)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
        }
        String preNickname = nickname;
        nickname = newNickname;
        //TODO 테이블 구조 변경 후 삭제
        if (group.isAdminNickname(preNickname)) {
            group.modifyAdmin(this);
        }
    }

    public void addGroup(Group group) {
        group.getParticipantList().add(this);
        this.group = group;
    }

    public void withdrawGroup(Group group) {
        delete();
        group.removeParticipant(this);
        if (group.hasNoParticipant()) {
            group.delete();
        }
    }

}
