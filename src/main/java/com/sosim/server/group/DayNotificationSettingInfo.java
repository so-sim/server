package com.sosim.server.group;

import lombok.AccessLevel;
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

    public DayNotificationSettingInfo(boolean allowedNotification, int repeatCycle, LocalTime sendTime) {
        super(allowedNotification, repeatCycle, sendTime);
        setStartSendDate();
    }

    private void setStartSendDate() {
        nextSendDateTime = LocalDateTime.of(LocalDate.now(), sendTime);
        if (nextSendDateTime.isBefore(LocalDateTime.now())) {
            //현재 정책: 알림 설정 했을 때, 시간이 현재보다 이전이면 내일부터 알림 주기 시작
            nextSendDateTime = nextSendDateTime.plusDays(1);
        }
    }

    @Override
    public LocalDateTime getNextNotifyDateTime() {
        LocalDateTime now = LocalDateTime.now();
        if (nextSendDateTime.isAfter(now)) {
            return nextSendDateTime;
        }
        nextSendDateTime = calculateNextSendDateTime();
        return nextSendDateTime;
    }

    private LocalDateTime calculateNextSendDateTime() {
        LocalDateTime sendDateTime = nextSendDateTime.plusDays(1);
        int cycle = 1;
        while (cycle++ < repeatCycle) {
            sendDateTime = sendDateTime.plusDays(1);
        }
        return sendDateTime;
    }

}


