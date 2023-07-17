package com.sosim.server.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Situation {
    NON("미납"),
    FULL("완납"),
    CHECK("확인중"),
    ;

    private String comment;

    @JsonCreator
    public static Situation getSituation(String comment) {
        return Arrays.stream(Situation.values())
                .filter(situation -> situation.getComment().equals(comment))
                .findFirst()
                .orElse(null);
    }
}
