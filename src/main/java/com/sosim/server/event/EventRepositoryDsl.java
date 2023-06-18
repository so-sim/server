package com.sosim.server.event;

import com.sosim.server.event.dto.request.FilterEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepositoryDsl {

    Page<Event> searchAll(FilterEventRequest filterEventRequest, Pageable pageable);

}
