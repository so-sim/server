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

    private DaysOfWeek daysOfWeek;

    public WeekNotificationSettingInfo(boolean allowedNotification, LocalDate startDate, int repeatCycle, LocalTime sendTime, DaysOfWeek daysOfWeek) {
        super(allowedNotification, startDate, repeatCycle, sendTime);
        setStartSendDate();
        this.daysOfWeek = daysOfWeek;
    }

    private void setStartSendDate() {
        nextSendDateTime = LocalDateTime.of(startDate, sendTime);
        nextSendDateTime = calculateNextSendDateTime();
    }

    @Override
    public LocalDateTime calculateNextSendDateTime() {
        LocalDateTime sendDateTime = LocalDateTime.of(nextSendDateTime.toLocalDate(), sendTime);

        DayOfWeek currentWeek = sendDateTime.getDayOfWeek();
        int diffCycle = 0;
        while (!isSendCondition(currentWeek, diffCycle, sendDateTime)) {
            if (SUNDAY.equals(currentWeek)) {
                diffCycle++;
            }
            sendDateTime = sendDateTime.plusDays(1);
            currentWeek = sendDateTime.getDayOfWeek();
        }
        return sendDateTime;
    }

    @Override
    public String getSettingType() {
        return "W";
    }

    private boolean isSendCondition(DayOfWeek currentWeek, int diffCycle, LocalDateTime sendDateTime) {
        return diffCycle % repeatCycle == 0 && daysOfWeek.contain(currentWeek)
                && isAfterOrEqualsNow(sendDateTime);
    }

    private boolean isAfterOrEqualsNow(LocalDateTime sendDateTime) {
        LocalDateTime now = LocalDateTime.now();
        return sendDateTime.isAfter(now) || sendDateTime.isEqual(now);
    }
}


