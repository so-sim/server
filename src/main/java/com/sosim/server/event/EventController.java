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

import static com.sosim.server.common.response.ResponseCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @PostMapping("/penalty")
    public ResponseEntity<?> createEvent(@AuthUserId long userId,
                                         @Validated @RequestBody CreateEventRequest createEventRequest) {
        EventIdResponse eventIdResponse = eventService.createEvent(userId, createEventRequest);

        return new ResponseEntity<>(Response.create(CREATE_EVENT, eventIdResponse), CREATE_EVENT.getHttpStatus());
    }

    @GetMapping("/penalty/{eventId}")
    public ResponseEntity<?> getEvent(@PathVariable long eventId) {
        GetEventResponse getEventResponse = eventService.getEvent(eventId);

        return new ResponseEntity<>(Response.create(GET_EVENT, getEventResponse), GET_EVENT.getHttpStatus());
    }

    @PatchMapping("/penalty/{eventId}")
    public ResponseEntity<?> modifyEvent(@AuthUserId long userId, @PathVariable long eventId,
                                         @Validated @RequestBody ModifyEventRequest modifyEventRequest) {
        GetEventResponse eventIdResponse = eventService.modifyEvent(userId, eventId, modifyEventRequest);

        return new ResponseEntity<>(Response.create(MODIFY_EVENT, eventIdResponse), MODIFY_EVENT.getHttpStatus());
    }

    @DeleteMapping("/penalty/{eventId}")
    public ResponseEntity<?> deleteEvent(@AuthUserId long userId, @PathVariable long eventId) {
        eventService.deleteEvent(userId, eventId);

        return new ResponseEntity<>(Response.create(DELETE_EVENT, null), DELETE_EVENT.getHttpStatus());
    }

    @PatchMapping("/penalty")
    public ResponseEntity<?> modifyEventSituation(@Validated @RequestBody ModifySituationRequest modifySituationRequest) {
        ModifySituationResponse modifySituationResponse = eventService.modifyEventSituation(modifySituationRequest);

        return new ResponseEntity<>(Response.create(MODIFY_EVENT_SITUATION, modifySituationResponse), MODIFY_EVENT_SITUATION.getHttpStatus());
    }

    @GetMapping("/penalty/calendar")
    public ResponseEntity<?> getEventCalendar(FilterEventRequest filterEventRequest) {
        GetEventCalendarResponse eventCalendarResponse = eventService.getEventCalendar(filterEventRequest);

        return new ResponseEntity<>(Response.create(GET_EVENT_CALENDAR, eventCalendarResponse), GET_EVENT_CALENDAR.getHttpStatus());
    }

    @GetMapping("/penalties")
    public ResponseEntity<?> getEvents(FilterEventRequest filterEventRequest, Pageable pageable) {
        GetEventListResponse eventList = eventService.getEvents(filterEventRequest, pageable);

        return new ResponseEntity<>(Response.create(GET_EVENTS, eventList), GET_EVENTS.getHttpStatus());
    }
}
