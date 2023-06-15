package com.sosim.server.participant;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.Group;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "PARTICIPANT")
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

    public static Participant create(User user, Group group, String nickname) {
        return Participant.builder()
                .user(user)
                .group(group)
                .nickname(nickname)
                .build();
    }

    public void modifyNickname(ParticipantNicknameRequest participantNicknameRequest) {
        this.nickname = participantNicknameRequest.getNickname();
    }
}
