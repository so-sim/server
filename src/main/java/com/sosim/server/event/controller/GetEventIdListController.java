package com.sosim.server.event.controller;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.event.dto.request.GetEventIdListRequest;
import com.sosim.server.event.dto.response.GetEventIdListResponse;
import com.sosim.server.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sosim.server.common.response.ResponseCode.GET_EVENTS;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class GetEventIdListController {

    private final EventService eventService;

    @GetMapping("/events")
    public ResponseEntity<?> getEventsByEventIdList(@AuthUserId long userId, @Validated GetEventIdListRequest getEventIdListRequest) {
        GetEventIdListResponse response = eventService.getEventsByEventIdList(userId, getEventIdListRequest);

        return new ResponseEntity<>(Response.create(GET_EVENTS, response), GET_EVENTS.getHttpStatus());
    }
}
