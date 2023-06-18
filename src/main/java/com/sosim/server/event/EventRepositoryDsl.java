package com.sosim.server.event;

import com.sosim.server.event.dto.request.FilterEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventRepositoryDsl {

    List<Event> searchAll(FilterEventRequest filterEventRequest);
    Page<Event> searchAll(FilterEventRequest filterEventRequest, Pageable pageable);

}
