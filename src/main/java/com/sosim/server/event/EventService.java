package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.*;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.dto.request.ManualNotificationRequest;
import com.sosim.server.notification.dto.request.ModifySituationNotificationRequest;
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
        event.modify(user, modifyEventRequest);

        return GetEventResponse.toDto(event);
    }

    @Transactional
    public void deleteEvent(long userId, long eventId) {
        Event event = findEventWithGroup(eventId);

        Group group = event.getGroup();
        checkIsAdmin(group, userId);

        event.delete(userId);
    }

    @Transactional
    public ModifySituationResponse modifyEventSituation(long userId, ModifySituationRequest modifySituationRequest) {
        eventRepository.updateSituationAll(modifySituationRequest.getEventIdList(), modifySituationRequest.getSituation());

        Group group = findEventWithGroup(modifySituationRequest.getEventIdList().get(0)).getGroup();
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
