package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder
    public WeekNotificationSettingInfo(boolean enableNotification, LocalDate startDate, int repeatCycle, LocalTime sendTime, DaysOfWeek daysOfWeek) {
        super(enableNotification, startDate, repeatCycle, sendTime);
        setNextSendDate();
        this.daysOfWeek = daysOfWeek;
    }

    private void setNextSendDate() {
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

    @Override
    public void changeSettingInfoDetail(NotificationSettingInfo newSettingInfo) {
        WeekNotificationSettingInfo weekSettingInfo = (WeekNotificationSettingInfo) newSettingInfo;
        daysOfWeek = weekSettingInfo.getDaysOfWeek();
        setNextSendDate();
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


