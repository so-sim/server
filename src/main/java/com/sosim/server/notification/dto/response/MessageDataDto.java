package com.sosim.server.notification.dto.response;

import com.sosim.server.event.Situation;
import lombok.Data;

@Data
public class MessageDataDto {
    private int amount;

    private Situation beforeSituation;

    private Situation afterSituation;

    private String nickname;
}
