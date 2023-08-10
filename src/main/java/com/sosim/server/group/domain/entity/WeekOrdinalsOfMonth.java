package com.sosim.server.group.domain.entity;

import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Embeddable;
import java.util.Arrays;

@NoArgsConstructor
@Embeddable
public class WeekOrdinalsOfMonth {
    private static final String DELIMITER = ",";

    private String ordinalNumbers;

    public WeekOrdinalsOfMonth(int[] ordinalNo) {
        if (ordinalNo == null || ordinalNo.length == 0) {
            this.ordinalNumbers = "";
        }
        this.ordinalNumbers = makeOrdinalNumberString(ordinalNo);
    }

    public int[] getOrdinalNumbers() {
        if (!StringUtils.hasText(ordinalNumbers)) {
            return new int[0];
        }
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
