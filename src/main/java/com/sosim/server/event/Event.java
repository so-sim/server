package com.sosim.server.event;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static com.sosim.server.common.auditing.Status.ACTIVE;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "EVENTS")
public class Event extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_ID")
    private Long id;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "AMOUNT")
    private int amount;

    @Column(name = "GROUND")
    @Enumerated(EnumType.STRING)
    private Ground ground;

    @Column(name = "MEMO")
    private String memo;

    @Column(name = "SITUATION")
    @Enumerated(EnumType.STRING)
    private Situation situation;

    @Column(name = "NICKNAME")
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    private Event(LocalDate date, int amount, Ground ground, String memo, Situation situation,
                  String nickname, Group group, User user) {
        this.date = date;
        this.amount = amount;
        this.ground = ground;
        this.memo = memo;
        this.situation = situation;
        this.nickname = nickname;
        this.group = group;
        this.user = user;
        status = ACTIVE;
    }

    public void modify(User user, ModifyEventRequest modifyEventRequest) {
        if (isDiffUser(modifyEventRequest.getNickname())) {
            this.nickname = modifyEventRequest.getNickname();
            this.user = user;
        }
        this.date = modifyEventRequest.getDate();
        this.amount = modifyEventRequest.getAmount();
        this.ground = modifyEventRequest.getGround();
        this.memo = modifyEventRequest.getMemo();
        this.situation = modifyEventRequest.getSituation();
    }

    public boolean isNotNonePaymentSituation() {
        return !Situation.NONE.equals(situation);
    }

    private boolean isDiffUser(String nickname) {
        return !this.nickname.equals(nickname);
    }
}
