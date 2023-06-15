package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        ResponseCode getEvent = ResponseCode.GET_GROUP;

        return new ResponseEntity<>(Response.create(getEvent, getEventResponse), getEvent.getHttpStatus());
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<?> modifyEvent(@AuthUserId long userId, @PathVariable("eventId") long eventId) {
        EventIdResponse eventIdResponse = eventService.modifyEvent(userId, eventId);
        ResponseCode modifyEvent = ResponseCode.MODIFY_EVENT;

        return new ResponseEntity<>(Response.create(modifyEvent, eventIdResponse), modifyEvent.getHttpStatus());
    }
}
