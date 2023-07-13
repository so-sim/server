package com.sosim.server.group;

import javax.persistence.Embeddable;

@Embeddable
public class WeeksOfMonth {
    private static final String DELIMITER = ",";

    private String ordinalNumbers;

    public WeeksOfMonth(String ordinalNumbers) {
        this.ordinalNumbers = ordinalNumbers;
    }

    public WeeksOfMonth(int... ordinalNo) {
        this.ordinalNumbers = makeOrdinalNumberString(ordinalNo);
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
