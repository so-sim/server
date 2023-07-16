package com.sosim.server.group;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.DayOfWeek;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class DaysOfWeek {
    public static final String DELIMITER = ",";

    private String daysOfWeek;

    public DaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public DaysOfWeek(DayOfWeek... dayOfWeeks) {
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

    private String makeString(DayOfWeek[] dayOfWeeks) {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek dayOfWeek : dayOfWeeks) {
            sb.append(dayOfWeek.name()).append(DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public DayOfWeek getLastWeek() {
        int lastDelimiterIndex = daysOfWeek.lastIndexOf(DELIMITER);
        return DayOfWeek.valueOf(daysOfWeek.substring(lastDelimiterIndex + 1));
    }
}
