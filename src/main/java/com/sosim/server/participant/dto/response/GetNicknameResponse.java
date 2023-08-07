package com.sosim.server.participant.dto.response;

import com.sosim.server.participant.domain.entity.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetNicknameResponse {
    private String nickname;

    public static GetNicknameResponse toDto(Participant participant) {
        return new GetNicknameResponse(participant.getNickname());
    }
}
