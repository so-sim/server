package com.sosim.server.group.domain.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.participant.QParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.group.QGroup.group;
import static com.sosim.server.participant.QParticipant.participant;

@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Slice<Group> findMyGroups(long userId, long offset, long limit) {
        QParticipant participant2 = new QParticipant("participant2");
        List<Group> contents = jpaQueryFactory.selectFrom(group)
                .join(group.participantList, participant).fetchJoin()
                .join(group.participantList, participant2)
                .where(participant2.user.id.eq(userId), participant.status.eq(ACTIVE),
                        group.status.eq(ACTIVE))
                .orderBy(group.id.desc())
                .offset(offset)
                .limit(limit + 1)
                .fetch();
        return toSlice(limit, contents);
    }

    private Slice<Group> toSlice(long limit, List<Group> contents) {
        boolean hasNext = contents.size() > limit;
        if (hasNext) {
            contents = contents.subList(0, (int) limit);
        }
        return new SliceImpl<>(contents, PageRequest.ofSize((int) limit), hasNext);
    }

}
