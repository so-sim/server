package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    protected int repeatCycle;

    protected LocalTime sendTime;

    protected LocalDateTime nextSendDateTime;

    public NotificationSettingInfo(boolean enableNotification, int repeatCycle, LocalTime sendTime) {
        this.enableNotification = enableNotification;
        this.repeatCycle = repeatCycle;
        this.sendTime = sendTime;
        nextSendDateTime = LocalDateTime.now();
    }

    public abstract LocalDateTime getNextNotifyDateTime();

//TODO    public abstract void changeSettingInfo();

    public void enableNotification() {
        if (!enableNotification) {
            this.enableNotification = true;
        }
    }

    public void disableNotification() {
        this.enableNotification = false;
    }

    public int getHour() {
        return sendTime.getHour();
    }

    public int getMinute() {
        return sendTime.getMinute();
    }
}
