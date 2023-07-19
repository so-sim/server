package com.sosim.server.group.dto.request;

import com.sosim.server.group.*;
import lombok.Data;
import org.springframework.validation.BindException;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.sosim.server.group.MonthSettingType.SIMPLE_DATE;

@Data
public class NotificationSettingRequest {

    public static final int FIRST_DAY_OF_MONTH = 1;
    public static final int LAST_DAY_OF_MONTH = 31;
    private boolean enableNotification;

    private String settingType;

    private LocalDate startDate;

    private int repeatCycle;

    private LocalTime sendTime;

    private MonthSettingType monthSettingType;

    private int sendDay;

    private int[] ordinalNumbers;

    private String[] daysOfWeek;

    public NotificationSettingInfo toNotificationSettingInfo() {
        switch (settingType) {
            case "M":
                return makeMonthNotificationSettingInfo();
            case "W":
                return makeWeekNotificationSettingInfo();
            case "D":
                return makeDayNotificationSettingInfo();
        }
        return null;
    }

    private NotificationSettingInfo makeDayNotificationSettingInfo() {
        return DayNotificationSettingInfo.builder()
                .enableNotification(enableNotification)
                .startDate(startDate)
                .repeatCycle(repeatCycle)
                .sendTime(sendTime)
                .build();
    }

    private NotificationSettingInfo makeWeekNotificationSettingInfo() {
        return WeekNotificationSettingInfo.builder()
                .enableNotification(enableNotification)
                .startDate(startDate)
                .repeatCycle(repeatCycle)
                .sendTime(sendTime)
                .daysOfWeek(new DaysOfWeek(daysOfWeek))
                .build();
    }

    private NotificationSettingInfo makeMonthNotificationSettingInfo() {
        return MonthNotificationSettingInfo.builder()
                .enableNotification(enableNotification)
                .startDate(startDate)
                .repeatCycle(repeatCycle)
                .sendTime(sendTime)
                .monthSettingType(monthSettingType)
                .sendDay(sendDay)
                .weekOrdinalsOfMonth(new WeekOrdinalsOfMonth(ordinalNumbers))
                .daysOfWeek(new DaysOfWeek(daysOfWeek))
                .build();
    }

    public void validate() throws BindException {
        if (!enableNotification) {
            return;
        }
        switch (settingType) {
            case "M":
                validateMonthRequest();
                return;
            case "W":
                validateWeekRequest();
                return;
            case "D":
//                validateDayRequest();
                return;
        }
        throwBindException("settingType", "settingType is not valid.");
    }

//    private void validateDayRequest() {
        //검사할 값이 없어서 주석처리
//    }

    private void validateWeekRequest() throws BindException {
        checkDaysOfWeek();
    }

    private void validateMonthRequest() throws BindException {
        if (SIMPLE_DATE.equals(monthSettingType)) {
            if (!isRangeOfMonth()) {
                throwBindException("sendDay", "sendDay is not valid.");
            }
            return;
        }
        if (!hasValidOrdinalNumbers()) {
            throwBindException("ordinalNumbers", "ordinalNumbers are not valid.");
        }
        checkDaysOfWeek();
    }

    private void checkDaysOfWeek() throws BindException {
        if (!hasValidDaysOfWeek()) {
            throwBindException("daysOfWeek", "daysOfWeek is not Valid.");
        }
    }

    private void throwBindException(String fieldName, String message) throws BindException {
        String objectName = this.getClass().getSimpleName();
        DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(this, objectName);
        FieldError error = new FieldError(objectName, fieldName, message);
        bindingResult.addError(error);
        throw new BindException(bindingResult);
    }

    private boolean isRangeOfMonth() {
        return FIRST_DAY_OF_MONTH <= sendDay && sendDay <= LAST_DAY_OF_MONTH;
    }

    private boolean hasValidOrdinalNumbers() {
        return isNotEmpty(ordinalNumbers) && isValidInput(ordinalNumbers);
    }

    private boolean hasValidDaysOfWeek() {
        return isNotEmpty(daysOfWeek) && isValidInput(daysOfWeek);
    }

    private boolean isValidInput(String[] daysOfWeek) {
        try {
            for (String dayOfWeekValue : daysOfWeek) {
                DayOfWeek.valueOf(dayOfWeekValue);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private boolean isValidInput(int[] ordinalNumbers) {
        for (int ordinalNumber : ordinalNumbers) {
            if (ordinalNumber <= 0 || ordinalNumber >= 6) {
                return false;
            }
        }
        return true;
    }

    private boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }

    private boolean isNotEmpty(int[] array) {
        return array != null && array.length > 0;
    }
}
