package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "NOTIFICATION_SETTINGS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "SETTING_TYPE")
public abstract class NotificationSettingInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enableNotification;

    protected LocalDate startDate;

    protected int repeatCycle;

    protected LocalTime sendTime;

    protected LocalDateTime nextSendDateTime;

    public NotificationSettingInfo(boolean enableNotification, LocalDate startDate, int repeatCycle, LocalTime sendTime) {
        this.enableNotification = enableNotification;
        this.startDate = startDate;
        this.repeatCycle = repeatCycle;
        this.sendTime = sendTime;
        nextSendDateTime = LocalDateTime.now();
    }

    public LocalDateTime getNextNotifyDateTime() {
        if (nextSendDateTime.isAfter(LocalDateTime.now())) {
            return nextSendDateTime;
        }

        nextSendDateTime = calculateNextSendDateTime();
        return nextSendDateTime;
    }

    public void changeSettingInfo(NotificationSettingInfo newSettingInfo) {
        enableNotification = newSettingInfo.isEnableNotification();
        if (!enableNotification) {
            return;
        }
        startDate = newSettingInfo.getStartDate();
        repeatCycle = newSettingInfo.getRepeatCycle();
        sendTime = newSettingInfo.getSendTime();
        changeSettingInfoDetail(newSettingInfo);
    }

    public abstract LocalDateTime calculateNextSendDateTime();

    public abstract String getSettingType();

    public abstract void changeSettingInfoDetail(NotificationSettingInfo newSettingInfo);

    public void enableNotification() {
        if (!enableNotification) {
            this.enableNotification = true;
        }
    }

    public void disableNotification() {
        this.enableNotification = false;
    }

}
