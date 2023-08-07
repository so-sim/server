package com.sosim.server.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sosim.server.event.domain.entity.Ground;
import com.sosim.server.event.domain.entity.Situation;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static com.sosim.server.event.domain.entity.Situation.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ModifyEventRequest {

    private String nickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @Max(value = 1_000_000, message = "최대 1,000,000원 까지 입력 가능합니다.")
    @Min(value = 0, message = "최소 0원 이상 입력 가능합니다.")
    private int amount;

    @NotNull(message = "존재하지 않는 사유 목록입니다.")
    private Ground ground;

    private String memo;

    @NotNull(message = "존재하지 않는 납부 여부 목록입니다.")
    private Situation situation;

    public boolean isChangedFull() {
        return FULL.equals(situation);
    }
}
