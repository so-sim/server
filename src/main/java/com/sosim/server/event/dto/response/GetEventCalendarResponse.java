package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.event.domain.entity.Situation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.sosim.server.event.domain.entity.Situation.*;

@Getter
@NoArgsConstructor
public class GetEventCalendarResponse {

    private Map<Integer, Payment> statusOfDay = new TreeMap<>(Comparator.comparingInt(o -> o));

    public static GetEventCalendarResponse toDto(List<Event> eventList) {
        GetEventCalendarResponse calendarResponse = new GetEventCalendarResponse();
        for (Event event : eventList) {
            calendarResponse.getStatusOfDay().put(event.getDate().getDayOfMonth(),
                    calendarResponse.getStatusOfDay().getOrDefault(
                    event.getDate().getDayOfMonth(), new Payment()).increaseStatus(event.getSituation()));
        }
        return calendarResponse;
    }

    @Getter
    static class Payment {
        @JsonProperty("미납")
        private int non;

        @JsonProperty("완납")
        private int full;

        @JsonProperty("확인중")
        private int check;

        private Payment increaseStatus(Situation situation) {
            if (situation.equals(NONE)) non++;
            else if (situation.equals(FULL)) full++;
            else check++;
            return this;
        }
    }
}

