package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.*;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.dto.request.ManualNotificationRequest;
import com.sosim.server.notification.dto.request.ModifySituationNotificationRequest;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventIdResponse createEvent(Long id, CreateEventRequest createEventRequest) {
        Group groupEntity = groupRepository.findById(createEventRequest.getGroupId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
        Participant participantEntity = participantRepository.findByNicknameAndGroupId(
                createEventRequest.getNickname(), createEventRequest.getGroupId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT));

        if (!groupEntity.isAdminUser(id)) {
            throw new CustomException(NONE_ADMIN);
        }

        Event eventEntity = saveEventEntity(Event.create(groupEntity, participantEntity.getUser(), createEventRequest));

        return EventIdResponse.create(eventEntity);
    }

    @Transactional(readOnly = true)
    public GetEventResponse getEvent(long userId, long eventId) {
        Event eventEntity = getEventEntity(eventId);

        return GetEventResponse.toDto(eventEntity);
    }

    @Transactional
    public GetEventResponse modifyEvent(long userId, long eventId, ModifyEventRequest modifyEventRequest) {
        Event eventEntity = getEventEntity(eventId);
        isAdmin(eventEntity, userId, true);

        User userEntity = null;
        if (!eventEntity.getNickname().equals(modifyEventRequest.getNickname())) {
            userEntity = participantRepository.findByNicknameAndGroupId(
                            modifyEventRequest.getNickname(), eventEntity.getGroup().getId())
                    .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT)).getUser();
        }

        eventEntity.modify(userEntity, modifyEventRequest);

        return GetEventResponse.toDto(eventEntity);
    }

    @Transactional
    public void deleteEvent(long userId, long eventId) {
        Event eventEntity = getEventEntity(eventId);

        eventEntity.delete(userId);
    }

    @Transactional
    public ModifySituationResponse modifyEventSituation(long userId, ModifySituationRequest modifySituationRequest) {
        eventRepository.updateSituationAll(modifySituationRequest.getEventIdList(), modifySituationRequest.getSituation());

        Group group = getEventEntity(modifySituationRequest.getEventIdList().get(0)).getGroup();
        String nickname = getParticipantNickname(userId, group.getId());
        List<Long> receiverUserIdList = getReceiverUserIdList(modifySituationRequest);
        ModifySituationNotificationRequest notification = ModifySituationNotificationRequest.toDto(
                group, modifySituationRequest.getSituation(), nickname, receiverUserIdList);
        eventPublisher.publishEvent(notification);

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

    @Transactional(readOnly = true)
    public void notifyEvents(EventIdListRequest eventIdList) {
        List<Event> eventList = eventRepository.findAllById(eventIdList.getEventIdList());
        Group group = eventList.get(0).getGroup();
        ManualNotificationRequest notification = ManualNotificationRequest.toDto(group, eventList);
        eventPublisher.publishEvent(notification);
    }

    private Event saveEventEntity(Event event) {
        return eventRepository.save(event);
    }

    private Event getEventEntity(long eventId) {
        return eventRepository.findByIdWithGroup(eventId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_EVENT));
    }

    private boolean isAdmin(Event event, long userId, boolean throwException) {
        boolean isAdmin = event.getGroup().isAdminUser(userId);
        if (!isAdmin && throwException) {
            throw new CustomException(NONE_ADMIN);
        }
        return isAdmin;
    }

    private List<Long> getReceiverUserIdList(ModifySituationRequest modifySituationRequest) {
        if (modifySituationRequest.getSituation().equals("확인 필요")) {
            List<Long> receiverUserIdList = new ArrayList<>();
            long adminUserId = eventRepository.getAdminUserId(modifySituationRequest.getEventIdList().get(0));
            receiverUserIdList.add(adminUserId);
            return receiverUserIdList;
        }
        return eventRepository.getReceiverUserIdList(modifySituationRequest.getEventIdList());
    }

    private String getParticipantNickname(long userId, long groupId) {
        return participantRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT))
                .getNickname();
    }
}
