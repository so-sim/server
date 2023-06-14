package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @PostMapping("/event/penalty")
    public ResponseEntity<?> createEvent(@AuthenticationPrincipal AuthUser authUser,
                                         @Validated @RequestBody CreateEventRequest createEventRequest,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

        EventIdResponse eventIdResponse = eventService.createEvent(authUser.getId(), createEventRequest);
        ResponseCode createEvent = ResponseCode.CREATE_EVENT;

        return new ResponseEntity<>(Response.create(createEvent, eventIdResponse), createEvent.getHttpStatus());
    }

    private void bindingError(BindingResult bindingResult) {
        throw new CustomException(ResponseCode.BINDING_ERROR, bindingResult.getFieldError().getField(),
                bindingResult.getFieldError().getDefaultMessage());
    }
}
