package com.sosim.server.group.domain.entity;

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
        this.daysOfWeek = setDaysOfWeek(daysOfWeek);
        setNextSendDate();
    }

    private DaysOfWeek setDaysOfWeek(DaysOfWeek daysOfWeek) {
        if (daysOfWeek == null) {
            return new DaysOfWeek(null);
        }
        return daysOfWeek;
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
        return isValidCycle(diffCycle) && isContain(currentWeek) && isAfterOrEqualsNow(sendDateTime);
    }

    private boolean isContain(DayOfWeek currentWeek) {
        return daysOfWeek.contain(currentWeek);
    }

    private boolean isValidCycle(int diffCycle) {
        return diffCycle % repeatCycle == 0;
    }

    private boolean isAfterOrEqualsNow(LocalDateTime sendDateTime) {
        LocalDateTime now = LocalDateTime.now();
        return sendDateTime.isAfter(now) || sendDateTime.isEqual(now);
    }
}


