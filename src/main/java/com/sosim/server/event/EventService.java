package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        return GetEventResponse.toDto(eventEntity, isAdmin(eventEntity, userId));
    }

    public EventIdResponse modifyEvent(long userId, long eventId) {
        Event eventEntity = getEventEntity(eventId);

        if (!isAdmin(eventEntity, userId)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }

        return EventIdResponse.create(eventEntity);
    }

    private Event saveEventEntity(Event event) {
        return eventRepository.save(event);
    }

    private Event getEventEntity(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_EVENT));
    }

    private boolean isAdmin(Event event, long userId) {
        return event.getGroup().getAdminId().equals(userId);
    }
}
