package com.sosim.server.group;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.participant.QParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static com.sosim.server.common.auditing.Status.*;
import static com.sosim.server.group.QGroup.*;
import static com.sosim.server.participant.QParticipant.*;

@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Slice<Group> findMyGroups(long userId, int offset, int limit) {
        QParticipant participantSub = new QParticipant("participantSub");

        List<Group> contents = jpaQueryFactory.selectFrom(group)
                .join(group.participantList, participant).fetchJoin()
                .where(group.id
                        .in(select(participantSub.group.id)
                                .from(participantSub)
                                .where(participantSub.user.id.eq(userId)
                                        .and(participantSub.status.eq(ACTIVE)))
                                .orderBy(participantSub.id.desc())
                                .offset(offset)
                                .limit(limit + 1)))
                .fetch();

        boolean hasNext = hasNextContent(limit, contents);
        return new SliceImpl<>(contents, PageRequest.ofSize(limit), hasNext);
    }

    private static boolean hasNextContent(int limit, List<Group> groups) {
        boolean hasNext = false;
        if (groups.size() > limit) {
            groups.remove(limit);
            hasNext = true;
        }
        return hasNext;
    }
}
