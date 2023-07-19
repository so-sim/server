package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.group.MonthSettingType.SIMPLE_DATE;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("M")
public class MonthNotificationSettingInfo extends NotificationSettingInfo {

    @Enumerated(EnumType.STRING)
    private MonthSettingType monthSettingType;

    @Embedded
    private WeekOrdinalsOfMonth weekOrdinalsOfMonth;

    @Embedded
    private DaysOfWeek daysOfWeek;

    private int sendDay;

    @Builder
    public MonthNotificationSettingInfo(boolean enableNotification, LocalDate startDate, int repeatCycle, LocalTime sendTime,
                                        MonthSettingType monthSettingType, WeekOrdinalsOfMonth weekOrdinalsOfMonth, DaysOfWeek daysOfWeek, int sendDay) {
        super(enableNotification, startDate, repeatCycle, sendTime);
        this.monthSettingType = monthSettingType;
        this.weekOrdinalsOfMonth = weekOrdinalsOfMonth;
        this.daysOfWeek = daysOfWeek;
        this.sendDay = sendDay;
        setNextSendTime();
    }

    private void setNextSendTime() {
        nextSendDateTime = LocalDateTime.of(startDate, sendTime);
        nextSendDateTime = calculateNextSendDateTime();
    }

    @Override
    public LocalDateTime calculateNextSendDateTime() {
        if (SIMPLE_DATE.equals(monthSettingType)) {
            return calcNextSimpleDateTime();
        }
        return calcNextWeekDateTime();
    }

    @Override
    public String getSettingType() {
        return "M";
    }

    @Override
    public void changeSettingInfoDetail(NotificationSettingInfo newSettingInfo) {
        MonthNotificationSettingInfo monthSettingInfo = (MonthNotificationSettingInfo) newSettingInfo;
        if (SIMPLE_DATE.equals(monthSettingInfo.getMonthSettingType())) {
            sendDay = monthSettingInfo.getSendDay();
        } else {
            weekOrdinalsOfMonth = monthSettingInfo.getWeekOrdinalsOfMonth();
            daysOfWeek = monthSettingInfo.getDaysOfWeek();
        }
        setNextSendTime();
    }

    private LocalDateTime calcNextSimpleDateTime() {
        LocalDate sendDate = setDayInLocalDateMonth(nextSendDateTime.toLocalDate(), sendDay);
        LocalDateTime sendDateTime = LocalDateTime.of(sendDate, sendTime);
        while (isBeforeNowOrStartDate(sendDateTime)) {
            sendDate = setDayInLocalDateMonth(sendDate.plusMonths(1), sendDay);
            sendDateTime = LocalDateTime.of(sendDate, sendTime);
        }
        return sendDateTime;
    }

    private boolean isBeforeNowOrStartDate(LocalDateTime sendDateTime) {
        return sendDateTime.isBefore(LocalDateTime.now()) ||
                sendDateTime.toLocalDate().isBefore(startDate);
    }

    private LocalDate setDayInLocalDateMonth(LocalDate localDate, int day) {
        LocalDate date;
        try {
            date = localDate.withDayOfMonth(day);
        } catch (DateTimeException e) {
            date = localDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        return date;
    }

    private LocalDateTime calcNextWeekDateTime() {
        LocalDate sendMonthDate = calcNextSendMonth();
        List<LocalDate> availableDates = findAvailableDatesInMonth(sendMonthDate);
        return findSendDateTime(availableDates);
    }

    private LocalDateTime findSendDateTime(List<LocalDate> availableDates) {
        LocalDate sendDate = availableDates.get(0);
        for (LocalDate availableDate : availableDates) {
            if (canSendDate(availableDate)) {
                sendDate = availableDate;
                break;
            }
        }
        return LocalDateTime.of(sendDate, sendTime);
    }

    private List<LocalDate> findAvailableDatesInMonth(LocalDate monthDate) {
        List<LocalDate> list = new ArrayList<>();

        int[] ordinalNumbers = weekOrdinalsOfMonth.getOrdinalNumbers();
        String[] daysOfWeekValues = daysOfWeek.getDaysOfWeekValues();
        for (int ordinal : ordinalNumbers) {
            for (String daysOfWeekValue : daysOfWeekValues) {
                DayOfWeek week = DayOfWeek.valueOf(daysOfWeekValue);
                list.add(monthDate.with(TemporalAdjusters.dayOfWeekInMonth(ordinal, week)));
            }
        }
        list.sort(LocalDate::compareTo);
        return list;
    }

    private LocalDate calcNextSendMonth() {
        LocalDate nextMonthDate = nextSendDateTime.toLocalDate();
        LocalDate lastDateInMonth = getAvailableLastDateInMonth(nextMonthDate);
        while (canSendDate(lastDateInMonth)) {
            nextMonthDate.plusMonths(1);
            lastDateInMonth = getAvailableLastDateInMonth(nextMonthDate);
        }
        return nextMonthDate;
    }

    private LocalDate getAvailableLastDateInMonth(LocalDate localDate) {
        int lastOrdinal = weekOrdinalsOfMonth.getLastOrdinalNumber();
        DayOfWeek lastWeek = daysOfWeek.getLastWeek();

        return localDate.with(TemporalAdjusters.dayOfWeekInMonth(lastOrdinal, lastWeek));
    }

    private boolean canSendDate(LocalDate localDate) {
        return localDate.isBefore(startDate) ||
                LocalDateTime.of(localDate, sendTime).isBefore(LocalDateTime.now());
    }
}

