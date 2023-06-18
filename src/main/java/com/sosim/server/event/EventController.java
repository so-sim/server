package com.sosim.server.event;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventCalendarResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event/penalty")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<?> createEvent(@AuthenticationPrincipal AuthUser authUser,
                                         @Validated @RequestBody CreateEventRequest createEventRequest) {
        EventIdResponse eventIdResponse = eventService.createEvent(authUser.getId(), createEventRequest);
        ResponseCode createEvent = ResponseCode.CREATE_EVENT;

        return new ResponseEntity<>(Response.create(createEvent, eventIdResponse), createEvent.getHttpStatus());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId) {
        GetEventResponse getEventResponse = eventService.getEvent(userId, eventId);
        ResponseCode getEvent = ResponseCode.GET_EVENT;

        return new ResponseEntity<>(Response.create(getEvent, getEventResponse), getEvent.getHttpStatus());
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<?> modifyEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId,
                                         @Validated @RequestBody ModifyEventRequest modifyEventRequest) {
        EventIdResponse eventIdResponse = eventService.modifyEvent(userId, eventId, modifyEventRequest);
        ResponseCode modifyEvent = ResponseCode.MODIFY_EVENT;

        return new ResponseEntity<>(Response.create(modifyEvent, eventIdResponse), modifyEvent.getHttpStatus());
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId) {
        eventService.deleteEvent(userId, eventId);
        ResponseCode deleteEvent = ResponseCode.DELETE_EVENT;

        return new ResponseEntity<>(Response.create(deleteEvent, null), deleteEvent.getHttpStatus());
    }

    @PatchMapping
    public ResponseEntity<?> modifyEventSituation(@AuthUserId long userId,
                                                  @Validated @RequestBody ModifySituationRequest modifySituationRequest) {
        List<Long> eventIdListResponse = eventService.modifyEventSituation(userId, modifySituationRequest);
        ResponseCode modifySituation = ResponseCode.MODIFY_EVENT_SITUATION;

        return new ResponseEntity<>(Response.create(modifySituation, eventIdListResponse), modifySituation.getHttpStatus());
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getEventCalendar(FilterEventRequest filterEventRequest) {
        GetEventCalendarResponse eventCalendarResponse = eventService.getEventCalendar(filterEventRequest);
        ResponseCode eventCalendar = ResponseCode.GET_EVENT_CALENDAR;

        return new ResponseEntity<>(Response.create(eventCalendar, eventCalendarResponse), eventCalendar.getHttpStatus());
    }
}
