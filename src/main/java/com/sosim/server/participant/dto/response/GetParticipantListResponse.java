package com.sosim.server.participant.dto.response;

import com.sosim.server.group.Group;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetParticipantListResponse {
    private String adminNickname;

    private List<String> nicknameList;

    public static GetParticipantListResponse toDto(Group group, List<String> nicknameList) {
        return GetParticipantListResponse.builder()
                .adminNickname(group.getAdminNickname())
                .nicknameList(nicknameList)
                .build();
    }
}
