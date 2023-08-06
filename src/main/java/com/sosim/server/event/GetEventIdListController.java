package com.sosim.server.event;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.event.dto.request.GetEventIdListRequest;
import com.sosim.server.event.dto.response.GetEventListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> getEventsByEventIdList(@AuthUserId long userId, @Validated GetEventIdListRequest getEventIdListRequest,
                                                    Pageable pageable) {
        GetEventListResponse response = eventService.getEventsByEventIdList(userId, getEventIdListRequest, pageable);

        return new ResponseEntity<>(Response.create(GET_EVENTS, response), GET_EVENTS.getHttpStatus());
    }
}
