package com.sosim.server.event;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @PostMapping("/penalty")
    public ResponseEntity<?> createEvent(@AuthenticationPrincipal AuthUser authUser,
                                         @Validated @RequestBody CreateEventRequest createEventRequest) {
        EventIdResponse eventIdResponse = eventService.createEvent(authUser.getId(), createEventRequest);
        ResponseCode createEvent = ResponseCode.CREATE_EVENT;

        return new ResponseEntity<>(Response.create(createEvent, eventIdResponse), createEvent.getHttpStatus());
    }

    @GetMapping("/penalty/{eventId}")
    public ResponseEntity<?> getEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId) {
        GetEventResponse getEventResponse = eventService.getEvent(userId, eventId);
        ResponseCode getEvent = ResponseCode.GET_EVENT;

        return new ResponseEntity<>(Response.create(getEvent, getEventResponse), getEvent.getHttpStatus());
    }

    @PatchMapping("/penalty/{eventId}")
    public ResponseEntity<?> modifyEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId,
                                         @Validated @RequestBody ModifyEventRequest modifyEventRequest) {
        GetEventResponse eventIdResponse = eventService.modifyEvent(userId, eventId, modifyEventRequest);
        ResponseCode modifyEvent = ResponseCode.MODIFY_EVENT;

        return new ResponseEntity<>(Response.create(modifyEvent, eventIdResponse), modifyEvent.getHttpStatus());
    }

    @DeleteMapping("/penalty/{eventId}")
    public ResponseEntity<?> deleteEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId) {
        eventService.deleteEvent(userId, eventId);
        ResponseCode deleteEvent = ResponseCode.DELETE_EVENT;

        return new ResponseEntity<>(Response.create(deleteEvent, null), deleteEvent.getHttpStatus());
    }

    @PatchMapping("/penalty")
    public ResponseEntity<?> modifyEventSituation(@AuthUserId long userId,
                                                  @Validated @RequestBody ModifySituationRequest modifySituationRequest) {
        ModifySituationResponse modifySituationResponse = eventService.modifyEventSituation(userId, modifySituationRequest);
        ResponseCode modifySituation = ResponseCode.MODIFY_EVENT_SITUATION;

        return new ResponseEntity<>(Response.create(modifySituation, modifySituationResponse), modifySituation.getHttpStatus());
    }

    @GetMapping("/penalty/calendar")
    public ResponseEntity<?> getEventCalendar(FilterEventRequest filterEventRequest) {
        GetEventCalendarResponse eventCalendarResponse = eventService.getEventCalendar(filterEventRequest);
        ResponseCode eventCalendar = ResponseCode.GET_EVENT_CALENDAR;

        return new ResponseEntity<>(Response.create(eventCalendar, eventCalendarResponse), eventCalendar.getHttpStatus());
    }

    @GetMapping("/penalties")
    public ResponseEntity<?> getEvents(FilterEventRequest filterEventRequest, Pageable pageable) {
        GetEventListResponse eventList = eventService.getEvents(filterEventRequest, pageable);
        ResponseCode getEvents = ResponseCode.GET_EVENTS;

        return new ResponseEntity<>(Response.create(getEvents, eventList), getEvents.getHttpStatus());
    }
}
