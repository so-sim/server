package com.sosim.server.event.dto.request;

import com.sosim.server.event.domain.entity.Situation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class FilterEventRequest {
    @NotBlank
    private long groupId;

    @NotBlank
    private LocalDate startDate;

    @NotBlank
    private LocalDate endDate;

    private String nickname;

    private Situation situation;
}
