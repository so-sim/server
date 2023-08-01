package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.util.NotificationUtil;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final NotificationUtil notificationUtil;

    @Transactional
    public EventIdResponse createEvent(Long userId, CreateEventRequest createEventRequest) {
        Group group = findGroupWithParticipants(createEventRequest.getGroupId());
        User user = findUserByParticipant(createEventRequest.getGroupId(), createEventRequest.getNickname());
        checkIsAdmin(group, userId);

        Event event = saveEventEntity(createEventRequest.toEntity(group, user));
        return EventIdResponse.toDto(event);
    }

    @Transactional(readOnly = true)
    public GetEventResponse getEvent(long eventId) {
        Event event = findEventWithGroup(eventId);

        return GetEventResponse.toDto(event);
    }

    @Transactional
    public GetEventResponse modifyEvent(long userId, long eventId, ModifyEventRequest modifyEventRequest) {
        Event event = findEventWithGroup(eventId);
        Group group = event.getGroup();
        checkIsAdmin(group, userId);
        User user = findUserByParticipant(group.getId(), modifyEventRequest.getNickname());

        boolean changedSituation = event.modifyAndCheckChangedSituation(user, modifyEventRequest);
        if (changedSituation) {
            notificationUtil.sendModifySituationNotifications(List.of(event), modifyEventRequest.getSituation());
        }

        return GetEventResponse.toDto(event);
    }

    @Transactional
    public void deleteEvent(long userId, long eventId) {
        Event event = findEventWithGroup(eventId);

        Group group = event.getGroup();
        checkIsAdmin(group, userId);

        event.delete();
    }

    @Transactional
    public ModifySituationResponse modifyEventSituation(long userId, ModifySituationRequest modifySituationRequest) {
        List<Event> events = eventRepository.findAllById(modifySituationRequest.getEventIdList());
        Group group = events.get(0).getGroup();
        Situation preSituation = events.get(0).getSituation();
        Situation newSituation = modifySituationRequest.getSituation();

        validSituation(userId, group, preSituation, newSituation);
        eventRepository.updateSituationAll(modifySituationRequest.getEventIdList(), modifySituationRequest.getSituation());
//        events.forEach(event -> event.modifySituation(situation)); update 쿼리가 여러개 나가는 문제 발생

        if (group.isAdminUser(userId)) {
            notificationUtil.sendModifySituationNotifications(events, newSituation);
        } else {
            notificationUtil.sendCheckSituationNotifications(userId, group, events);
        }

        return ModifySituationResponse.toDto(modifySituationRequest.getSituation(), modifySituationRequest.getEventIdList());
    }

    @Transactional(readOnly = true)
    public GetEventCalendarResponse getEventCalendar(FilterEventRequest filterEventRequest) {
        List<Event> events = eventRepository.searchAll(filterEventRequest);
        return GetEventCalendarResponse.toDto(events);
    }

    @Transactional(readOnly = true)
    public GetEventListResponse getEvents(FilterEventRequest filterEventRequest, Pageable pageable) {
        Page<Event> events = eventRepository.searchAll(filterEventRequest, pageable);
        return GetEventListResponse.toDto(events.getContent(), events.getTotalElements());
    }

    private void validSituation(long userId, Group group, Situation preSituation, Situation newSituation) {
        if (preSituation.canModifyToCheck(newSituation)) {
            throw new CustomException(NOT_FULL_TO_CHECK);
        }

        boolean isAdminUser = group.isAdminUser(userId);
        if (!isAdminUser && !newSituation.canModifyByParticipant()) {
            throw new CustomException(NOT_CHECK_SITUATION);
        }
        if (isAdminUser && !newSituation.canModifyByAdmin()) {
            throw new CustomException(NOT_FULL_OR_NON_SITUATION);
        }
    }

    private Event saveEventEntity(Event event) {
        return eventRepository.save(event);
    }

    private Event findEventWithGroup(long eventId) {
        return eventRepository.findByIdWithGroup(eventId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_EVENT));
    }

    private void checkIsAdmin(Group group, long userId) {
        if (!group.isAdminUser(userId)) {
            throw new CustomException(NONE_ADMIN);
        }
    }

    private Group findGroupWithParticipants(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

    private User findUserByParticipant(long groupId, String nickname) {
        return participantRepository.findByNicknameAndGroupId(nickname, groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT))
                .getUser();
    }

}
