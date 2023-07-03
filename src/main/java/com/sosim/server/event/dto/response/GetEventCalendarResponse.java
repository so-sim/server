package com.sosim.server.event.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.event.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

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

        private Payment increaseStatus(String situation) {
            if (situation.equals("미납")) non++;
            else if (situation.equals("완납")) full++;
            else check++;
            return this;
        }
    }
}

