package com.sosim.server.participant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosim.server.group.Group;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetParticipantListResponse {
    @JsonProperty("adminNickname")
    private String adminNickname;

    @JsonProperty("memberList")
    private List<String> nicknameList;

    public static GetParticipantListResponse create(Group group, List<String> nicknameList) {
        return GetParticipantListResponse.builder()
                .adminNickname(group.getAdminNickname())
                .nicknameList(nicknameList)
                .build();
    }
}
