package com.sosim.server.group.dto.response;

import com.sosim.server.group.DayNotificationSettingInfo;
import com.sosim.server.group.MonthNotificationSettingInfo;
import com.sosim.server.group.NotificationSettingInfo;
import com.sosim.server.group.WeekNotificationSettingInfo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
public class NotificationSettingResponse {
    private boolean enableNotification;

    private String settingType;

    private LocalDate startDate;

    private int repeatCycle;

    private LocalTime sendTime;

    private String monthSettingType;

    private int sendDay;

    private int[] ordinalNumbers;

    private String[] daysOfWeek;

    public static NotificationSettingResponse toResponse(NotificationSettingInfo settingInfo) {
        if (settingInfo == null) {
            return null;
        }

        if ("M".equals(settingInfo.getSettingType())) {
            return toResponse((MonthNotificationSettingInfo) settingInfo);
        } else if ("W".equals(settingInfo.getSettingType())) {
            return toResponse((WeekNotificationSettingInfo) settingInfo);
        }
        return toResponse((DayNotificationSettingInfo) settingInfo);
    }

    private static NotificationSettingResponse toResponse(MonthNotificationSettingInfo settingInfo) {
        return NotificationSettingResponse.builder()
                .enableNotification(settingInfo.isEnableNotification())
                .settingType(settingInfo.getSettingType())
                .startDate(settingInfo.getStartDate())
                .repeatCycle(settingInfo.getRepeatCycle())
                .sendTime(settingInfo.getSendTime())
                .monthSettingType(settingInfo.getMonthSettingType().name())
                .sendDay(settingInfo.getSendDay())
                .ordinalNumbers(settingInfo.getWeekOrdinalsOfMonth().getOrdinalNumbers())
                .daysOfWeek(settingInfo.getDaysOfWeek().getDaysOfWeekValues())
                .build();
    }

    private static NotificationSettingResponse toResponse(WeekNotificationSettingInfo settingInfo) {
        return NotificationSettingResponse.builder()
                .enableNotification(settingInfo.isEnableNotification())
                .settingType(settingInfo.getSettingType())
                .startDate(settingInfo.getStartDate())
                .repeatCycle(settingInfo.getRepeatCycle())
                .sendTime(settingInfo.getSendTime())
                .daysOfWeek(settingInfo.getDaysOfWeek().getDaysOfWeekValues())
                .build();
    }

    private static NotificationSettingResponse toResponse(DayNotificationSettingInfo settingInfo) {
        return NotificationSettingResponse.builder()
                .enableNotification(settingInfo.isEnableNotification())
                .settingType(settingInfo.getSettingType())
                .startDate(settingInfo.getStartDate())
                .repeatCycle(settingInfo.getRepeatCycle())
                .sendTime(settingInfo.getSendTime())
                .build();
    }
}
