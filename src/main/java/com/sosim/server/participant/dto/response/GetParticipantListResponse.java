package com.sosim.server.participant.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetParticipantListResponse {
    private String adminNickname;

    private List<String> nicknameList;

    public static GetParticipantListResponse toDto(String adminNickname, List<String> nicknameList) {
        return GetParticipantListResponse.builder()
                .adminNickname(adminNickname)
                .nicknameList(nicknameList)
                .build();
    }
}
