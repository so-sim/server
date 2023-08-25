package com.sosim.server.group.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sosim.server.group.dto.response.MyGroupDto;
import com.sosim.server.participant.domain.entity.QParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.group.domain.entity.QGroup.*;
import static com.sosim.server.participant.domain.entity.QParticipant.participant;

@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Slice<MyGroupDto> findMyGroups(long userId, long offset, long limit) {
        QParticipant admin = new QParticipant("participant2");
        List<MyGroupDto> contents = jpaQueryFactory
                .select(
                        Projections.constructor(MyGroupDto.class,
                        group.id, group.title, group.coverColor, group.groupType,
                        admin.nickname, participant.isAdmin)
                )
                .from(group)
                .join(group.participantList, admin)
                .join(group.participantList, participant)
                .where(participant.user.id.eq(userId),
                        participant.status.eq(ACTIVE),
                        admin.isAdmin.eq(true),
                        group.status.eq(ACTIVE))
                .orderBy(participant.id.desc())
                .offset(offset)
                .limit(limit + 1)
                .fetch();
        return toSlice(limit, contents);
    }

    private Slice<MyGroupDto> toSlice(long limit, List<MyGroupDto> contents) {
        boolean hasNext = contents.size() > limit;
        if (hasNext) {
            contents = contents.subList(0, (int) limit);
        }
        return new SliceImpl<>(contents, PageRequest.ofSize((int) limit), hasNext);
    }

}
