package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.DayOfWeek.SUNDAY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("W")
public class WeekNotificationSettingInfo extends NotificationSettingInfo {

    private DaysOfWeek weeks;

    public WeekNotificationSettingInfo(boolean allowedNotification, int repeatCycle, LocalTime sendTime, DaysOfWeek weeks) {
        super(allowedNotification, repeatCycle, sendTime);
        setStartSendDate();
        this.weeks = weeks;
    }

    private void setStartSendDate() {
        nextSendDateTime = LocalDateTime.now();
        nextSendDateTime = getNextNotifyDateTime();
    }

    @Override
    public LocalDateTime getNextNotifyDateTime() {
        LocalDateTime now = LocalDateTime.now();
        if (nextSendDateTime.isAfter(now)) {
            return nextSendDateTime;
        }

        LocalDate sendDate = calculateNextSendDate();
        nextSendDateTime = LocalDateTime.of(sendDate, sendTime);
        return nextSendDateTime;
    }

    private LocalDate calculateNextSendDate() {
        LocalDate sendDate = nextSendDateTime.toLocalDate().plusDays(1);
        DayOfWeek currentWeek = sendDate.getDayOfWeek();
        int cycle = 0;
        while (!isSendCondition(currentWeek, cycle)) {
            if (SUNDAY.equals(currentWeek)) {
                cycle++;
            }
            sendDate = sendDate.plusDays(1);
            currentWeek = sendDate.getDayOfWeek();
        }
        return sendDate;
    }

    private boolean isSendCondition(DayOfWeek currentWeek, int cycle) {
        return cycle % repeatCycle == 0 && weeks.contain(currentWeek);
    }
}


