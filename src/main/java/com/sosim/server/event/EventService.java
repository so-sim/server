package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.*;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.notification.util.NotificationUtil;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        Participant participant = findParticipant(createEventRequest.getGroupId(), createEventRequest.getNickname());
        boolean participantIsWithdraw = participant.isWithdrawGroup();

        if (participantIsWithdraw) {
            throw new CustomException(NOT_FOUND_PARTICIPANT);
        }

        checkIsAdmin(group, userId);

        Event event = saveEventEntity(createEventRequest.toEntity(group, participant.getUser()));
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

        Participant participant = findParticipant(group.getId(), modifyEventRequest.getNickname());
        Situation preSituation = event.getSituation();

        event.modify(participant.getUser(), modifyEventRequest);

        boolean participantIsWithdraw = participant.isWithdrawGroup();
        Situation newSituation = modifyEventRequest.getSituation();
        if (!participantIsWithdraw && preSituation != newSituation) {
            notificationUtil.sendModifySituationNotifications(List.of(event), preSituation, newSituation);
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

        if (group.isAdminUser(userId)) {
            List<String> withdrawNicknames = getWithdrawNickname(events, group);
            events = events.stream().filter(e -> !withdrawNicknames.contains(e.getNickname())).collect(Collectors.toList());
            notificationUtil.sendModifySituationNotifications(events, preSituation, newSituation);
        } else {
            //TODO: events에 여러 사용자가 섞이는 경우 체크해야 하는지?
            notificationUtil.sendCheckSituationNotification(group, events);
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
        //TODO: 논의 후 페이지네이션 정보 변경하기
        return GetEventListResponse.toDto(events.getContent(), events.getTotalElements());
    }

    @Transactional(readOnly = true)
    public GetEventListResponse getEventsByEventIdList(long userId, GetEventIdListRequest getEventIdListRequest, Pageable pageable) {
        long groupId = getEventIdListRequest.getGroupId();
        Group group = findGroupWithParticipants(groupId);

        Page<Event> events = null;
        if (group.isAdminUser(userId)) {
            events = eventRepository.findAllByEventIdList(groupId, getEventIdListRequest.getEventIdList(), pageable);
        } else {
            events = eventRepository.findAllByEventIdList(userId, groupId, getEventIdListRequest.getEventIdList(), pageable);
        }
        //TODO: 논의 후 페이지네이션 정보 변경하기
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

    private Participant findParticipant(long groupId, String nickname) {
        return participantRepository.findByNicknameAndGroupId(nickname, groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT));
    }

    private List<String> getWithdrawNickname(List<Event> events, Group group) {
        List<String> nicknames = events.stream().map(Event::getNickname).collect(Collectors.toList());
        return participantRepository.findAllByNicknameInAndGroup(nicknames, group).stream()
                .map(Participant::getNickname)
                .collect(Collectors.toList());
    }

}
