package com.sosim.server.participant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.participant.Participant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetNicknameResponse {
    @JsonProperty("nickname")
    private String nickname;

    public static GetNicknameResponse create(Participant participant) {
        return GetNicknameResponse.builder()
                .nickname(participant.getNickname())
                .build();
    }
}
