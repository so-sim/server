package com.sosim.server.event.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Situation {
    NONE("미납"),
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

    public boolean canModifyByParticipant() {
        return CHECK.equals(this);
    }

    public boolean canModifyByAdmin() {
        return FULL.equals(this) || NONE.equals(this);
    }

    public boolean canModifyToCheck(Situation newSituation) {
        if (!newSituation.equals(CHECK)) return false;
        return !this.equals(NONE);
    }
}
