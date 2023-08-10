package com.sosim.server.group.domain.entity;

import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.DayOfWeek;

@NoArgsConstructor
@Embeddable
public class DaysOfWeek {
    public static final String DELIMITER = ",";

    private String daysOfWeek;

    public DaysOfWeek(String[] dayOfWeeks) {
        if (dayOfWeeks == null || dayOfWeeks.length == 0) {
            this.daysOfWeek = "";
        }
        this.daysOfWeek = makeString(dayOfWeeks);
    }

    public boolean contain(DayOfWeek checkWeek) {
        String[] saveWeekValues = daysOfWeek.split(DELIMITER);
        for (String saveWeekValue : saveWeekValues) {
            DayOfWeek saveWeek = DayOfWeek.valueOf(saveWeekValue);
            if (saveWeek.equals(checkWeek)) {
                return true;
            }
        }
        return false;
    }

    public String[] getDaysOfWeekValues() {
        return daysOfWeek.split(DELIMITER);
    }

    private String makeString(String[] dayOfWeeks) {
        StringBuilder sb = new StringBuilder();
        for (String week : dayOfWeeks) {
            sb.append(week).append(DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public DayOfWeek getLastWeek() {
        int lastDelimiterIndex = daysOfWeek.lastIndexOf(DELIMITER);
        return DayOfWeek.valueOf(daysOfWeek.substring(lastDelimiterIndex + 1));
    }
}
