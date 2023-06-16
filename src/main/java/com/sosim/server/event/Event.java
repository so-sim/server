package com.sosim.server.event;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

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
    private String ground;

    @Column(name = "MEMO")
    private String memo;

    @Column(name = "SITUATION")
    private String situation;

    @Column(name = "NICKNAME")
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    private Event(LocalDate date, int amount, String ground, String memo, String situation,
                  String nickname, Group group, User user) {
        this.amount = amount;
        this.ground = ground;
        this.memo = memo;
        this.situation = situation;
        this.nickname = nickname;
        this.group = group;
        this.user = user;
    }

    public static Event create(Group group, User user, CreateEventRequest createEventRequest) {
        return Event.builder()
                .amount(createEventRequest.getAmount())
                .ground(createEventRequest.getGround())
                .memo(createEventRequest.getMemo())
                .situation(createEventRequest.getSituation())
                .nickname(createEventRequest.getNickname())
                .group(group)
                .user(user)
                .build();
    }

    public void modify(User user, ModifyEventRequest modifyEventRequest) {
        if (user != null) {
            this.nickname = modifyEventRequest.getNickname();
            this.user = user;
        }
        this.amount = modifyEventRequest.getAmount();
        this.ground = modifyEventRequest.getGround();
        this.memo = modifyEventRequest.getMemo();
        this.situation = modifyEventRequest.getSituation();
    }
}
