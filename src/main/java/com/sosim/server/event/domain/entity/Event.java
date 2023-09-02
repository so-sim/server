package com.sosim.server.event.domain.entity;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.common.response.ResponseCode.NOT_CHECK_SITUATION;
import static com.sosim.server.common.response.ResponseCode.NOT_FULL_OR_NON_SITUATION;

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

    @Column(name = "PRE_SITUATION")
    @Enumerated(EnumType.STRING)
    private Situation preSituation;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "MADE_BY")
    private long madeByUserId;

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
        this.madeByUserId = user.getId();
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

        if (modifyEventRequest.getSituation().equals(Situation.FULL)) {
            preSituation = situation;
        }
        this.situation = modifyEventRequest.getSituation();
    }

    public boolean isNotNonePaymentSituation() {
        return !Situation.NONE.equals(situation);
    }

    public void modifySituation(Situation situation) {
        validSituation(situation);
        this.situation = situation;
    }

    public boolean isMine(long userId) {
        return  user.getId().equals(userId);
    }

    public boolean included(Group group) {
        return this.group.getId().equals(group.getId());
    }

    public boolean isLock() {return this.status.equals(Status.LOCK);}

    private void validSituation(Situation situation) {
        boolean isAdminUser = group.isAdminUser(user.getId());
        if (!isAdminUser && !situation.canModifyByParticipant()) {
            throw new CustomException(NOT_CHECK_SITUATION);
        }
        if (isAdminUser && !situation.canModifyByAdmin()) {
            throw new CustomException(NOT_FULL_OR_NON_SITUATION);
        }
    }

    private boolean isDiffUser(String nickname) {
        return !this.nickname.equals(nickname);
    }
}
