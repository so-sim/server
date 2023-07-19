package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("D")
public class DayNotificationSettingInfo extends NotificationSettingInfo {

    @Builder
    public DayNotificationSettingInfo(boolean enableNotification, LocalDate startDate, int repeatCycle, LocalTime sendTime) {
        super(enableNotification, startDate, repeatCycle, sendTime);
        setNextSendDate();
    }

    private void setNextSendDate() {
        nextSendDateTime = LocalDateTime.of(LocalDate.now(), sendTime);
        while (isBeforeThanNowOrStartDate()) {
            nextSendDateTime = nextSendDateTime.plusDays(1);
        }
    }

    private boolean isBeforeThanNowOrStartDate() {
        return nextSendDateTime.isBefore(LocalDateTime.now()) || nextSendDateTime.toLocalDate().isBefore(startDate);
    }

    @Override
    public LocalDateTime calculateNextSendDateTime() {
        LocalDateTime sendDateTime = LocalDateTime.of(nextSendDateTime.toLocalDate(), sendTime);
        LocalDateTime now = LocalDateTime.now();
        while (sendDateTime.isBefore(now)) {
            sendDateTime = sendDateTime.plusDays(repeatCycle);
        }
        return sendDateTime;
    }

    @Override
    public String getSettingType() {
        return "W";
    }

    @Override
    public void changeSettingInfoDetail(NotificationSettingInfo newSettingInfo) {
        setNextSendDate();
    }
}


