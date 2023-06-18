package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventCalendarResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;

    public EventIdResponse createEvent(Long id, CreateEventRequest createEventRequest) {
        Group groupEntity = groupRepository.findById(createEventRequest.getGroupId())
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_GROUP));
        Participant participantEntity = participantRepository.findByNicknameAndGroupId(
                createEventRequest.getNickname(), createEventRequest.getGroupId())
                .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT));

        if (!groupEntity.getAdminId().equals(id)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }

        Event eventEntity = saveEventEntity(Event.create(groupEntity, participantEntity.getUser(), createEventRequest));

        return EventIdResponse.create(eventEntity);
    }

    public GetEventResponse getEvent(long userId, long eventId) {
        Event eventEntity = getEventEntity(eventId);

        return GetEventResponse.toDto(eventEntity, isAdmin(eventEntity, userId, false));
    }

    @Transactional
    public EventIdResponse modifyEvent(long userId, long eventId, ModifyEventRequest modifyEventRequest) {
        Event eventEntity = getEventEntity(eventId);
        isAdmin(eventEntity, userId, true);

        User userEntity = null;
        if (!eventEntity.getNickname().equals(modifyEventRequest.getNickname())) {
            userEntity = participantRepository.findByNicknameAndGroupId(
                            modifyEventRequest.getNickname(), eventEntity.getGroup().getId())
                    .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT)).getUser();
        }

        eventEntity.modify(userEntity, modifyEventRequest);

        return EventIdResponse.create(eventEntity);
    }

    @Transactional
    public void deleteEvent(long userId, long eventId) {
        Event eventEntity = getEventEntity(eventId);
        isAdmin(eventEntity, userId, true);

        eventEntity.delete();
    }

    @Transactional
    public List<Long> modifyEventSituation(long userId, ModifySituationRequest modifySituationRequest) {
        List<Event> eventList = eventRepository.findByIdIn(modifySituationRequest.getEventIdList());
        for (Event event : eventList) {
            event.modifySituation(modifySituationRequest.getSituation());
        }

        return eventList.stream().map(Event::getId).collect(Collectors.toList());
    }

    public GetEventCalendarResponse getEventCalendar(FilterEventRequest filterEventRequest) {
        List<Event> events = eventRepository.searchAll(filterEventRequest);
        return GetEventCalendarResponse.toDto(events);
    }

    private Event saveEventEntity(Event event) {
        return eventRepository.save(event);
    }

    private Event getEventEntity(long eventId) {
        return eventRepository.findByIdWithGroup(eventId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_EVENT));
    }

    private boolean isAdmin(Event event, long userId, boolean throwException) {
        boolean isAdmin = event.getGroup().getAdminId().equals(userId);
        if (!isAdmin && throwException) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }
        return isAdmin;
    }
}
