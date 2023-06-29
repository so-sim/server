package com.sosim.server.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.event.dto.request.FilterEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.sosim.server.event.QEvent.event;


@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Event> searchAll(FilterEventRequest filterEventRequest) {
        return filterEvent(filterEventRequest).fetch();
    }

    @Override
    public Page<Event> searchAll(FilterEventRequest filterEventRequest, Pageable pageable) {
        JPAQuery<Event> filterEvent = filterEvent(filterEventRequest);
        return doPageable(filterEvent, pageable, filterEvent.fetch().size());
    }

    private JPAQuery<Event> filterEvent(FilterEventRequest filterEventRequest) {
        return jpaQueryFactory
                .selectFrom(event)
                .where(
                        equalsGroup(filterEventRequest.getGroupId()),
                        betweenTime(filterEventRequest.getStartDate(), filterEventRequest.getEndDate()),
                        equalsNickname(filterEventRequest.getNickname()),
                        equalsSituation(filterEventRequest.getSituation()),
                        event.status.eq(Status.ACTIVE)
                )
                .orderBy(event.date.asc(), event.id.asc());
    }

    private BooleanExpression equalsGroup(long groupId) {
        return groupId == 0 ? null : event.group.id.stringValue().contains(String.valueOf(groupId));
    }

    private BooleanExpression betweenTime(LocalDate startDate, LocalDate endDate) {
        return startDate == null ? null : event.date.between(startDate, endDate);
    }

    private BooleanExpression equalsNickname(String nickname) {
        return nickname == null ? null : event.nickname.eq(nickname);
    }

    private BooleanExpression equalsSituation(String situation) {
        return situation == null ? null : event.situation.contains(situation);
    }

    private Page<Event> doPageable(JPAQuery<Event> filterEvents, Pageable pageable, long totalSize) {
        return new PageImpl<>(filterEvents.offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch(), pageable, totalSize);
    }
}
