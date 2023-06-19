package com.sosim.server.event.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class FilterEventRequest {

    private long groupId;

    @DateTimeFormat(pattern = "yyyy.MM.dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy.MM.dd")
    private LocalDate endDate;

    private String nickname;

    private String situation;
}
