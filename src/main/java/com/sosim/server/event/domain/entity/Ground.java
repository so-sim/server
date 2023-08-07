package com.sosim.server.event.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Ground {
    LATE("지각"),
    ABSENT("결석"),
    NONE_ASSIGN("과제 안 함"),
    ETC("기타")
    ;

    private String comment;

    @JsonCreator
    public static Ground getGround(String comment) {
        return Arrays.stream(Ground.values())
                .filter(ground -> ground.getComment().equals(comment))
                .findFirst()
                .orElse(null);
    }
}
