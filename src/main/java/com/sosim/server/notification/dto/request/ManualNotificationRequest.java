package com.sosim.server.notification.dto.request;

import com.sosim.server.event.Event;
import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ManualNotificationRequest {

    private long groupId;

    private String groupTitle;

    private Map<Long, Integer> userAmountMap;

    public static ManualNotificationRequest toDto(Group group, List<Event> eventList) {
        return ManualNotificationRequest.builder()
                .groupId(group.getId())
                .groupTitle(group.getTitle())
                .userAmountMap(createUserAmountMap(eventList))
                .build();
    }

    private static Map<Long, Integer> createUserAmountMap(List<Event> eventList) {
        Map<Long, Integer> userAmountMap = new HashMap<>();
        eventList.forEach(e -> userAmountMap.put(e.getUser().getId(),
                userAmountMap.getOrDefault(e.getUser().getId(), 0) + e.getAmount()));
        return userAmountMap;
    }
}
