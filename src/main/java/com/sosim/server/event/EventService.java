package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventIdResponse createEvent(Long id, CreateEventRequest createEventRequest) {
        Group group = findGroupWithParticipants(createEventRequest.getGroupId());
        User user = findParticipantWithUser(createEventRequest.getGroupId(), createEventRequest.getNickname()).getUser();

        isAdmin(group, user.getId());

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
        Event eventEntity = findEventWithGroup(eventId);
        isAdmin(eventEntity.getGroup(), userId);

        User userEntity = null;
        if (!eventEntity.getNickname().equals(modifyEventRequest.getNickname())) {
            userEntity = participantRepository.findByNicknameAndGroupId(
                            modifyEventRequest.getNickname(), eventEntity.getGroup().getId())
                    .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_PARTICIPANT)).getUser();
        }

        eventEntity.modify(userEntity, modifyEventRequest);

        return GetEventResponse.toDto(eventEntity);
    }

    @Transactional
    public void deleteEvent(long userId, long eventId) {
        Event eventEntity = findEventWithGroup(eventId);

        eventEntity.delete(userId);
    }

    @Transactional
    public ModifySituationResponse modifyEventSituation(ModifySituationRequest modifySituationRequest) {
        eventRepository.updateSituationAll(modifySituationRequest.getEventIdList(), modifySituationRequest.getSituation());

        return ModifySituationResponse.toDto(modifySituationRequest.getSituation(), modifySituationRequest.getEventIdList());
    }

    public GetEventCalendarResponse getEventCalendar(FilterEventRequest filterEventRequest) {
        List<Event> events = eventRepository.searchAll(filterEventRequest);
        return GetEventCalendarResponse.toDto(events);
    }

    public GetEventListResponse getEvents(FilterEventRequest filterEventRequest, Pageable pageable) {
        Page<Event> events = eventRepository.searchAll(filterEventRequest, pageable);
        return GetEventListResponse.toDto(events.getContent(), events.getTotalElements());
    }

    private Event saveEventEntity(Event event) {
        return eventRepository.save(event);
    }

    private Event findEventWithGroup(long eventId) {
        return eventRepository.findByIdWithGroup(eventId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_EVENT));
    }

    private void isAdmin(Group group, long userId) {
        if (!group.isAdminUser(userId)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }
    }

    private Group findGroupWithParticipants(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_GROUP));
    }

    private Participant findParticipantWithUser(long groupId, String nickname) {
        return participantRepository.findByNicknameAndGroupId(nickname, groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_PARTICIPANT));
    }
}
