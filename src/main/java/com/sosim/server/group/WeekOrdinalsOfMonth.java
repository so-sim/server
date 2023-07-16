package com.sosim.server.group;

import javax.persistence.Embeddable;
import java.util.Arrays;

@Embeddable
public class WeekOrdinalsOfMonth {
    private static final String DELIMITER = ",";

    private String ordinalNumbers;

    public WeekOrdinalsOfMonth(String ordinalNumbers) {
        this.ordinalNumbers = ordinalNumbers;
    }

    public WeekOrdinalsOfMonth(int... ordinalNo) {
        this.ordinalNumbers = makeOrdinalNumberString(ordinalNo);
    }

    public int[] getOrdinalNumbers() {
        return Arrays.stream(ordinalNumbers.split(DELIMITER))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public int getLastOrdinalNumber() {
        String[] split = ordinalNumbers.split(DELIMITER);
        return Integer.parseInt(split[split.length - 1]);
    }

    private String makeOrdinalNumberString(int[] ordinalNo) {
        StringBuilder sb = new StringBuilder();
        for (int no : ordinalNo) {
            sb.append(no).append(DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
