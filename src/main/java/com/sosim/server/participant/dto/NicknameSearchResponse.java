package com.sosim.server.participant.dto;

import com.sosim.server.participant.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class NicknameSearchResponse {
    private List<NicknameDto> nicknameDtoList;

    public static NicknameSearchResponse toDto(List<Participant> participantList) {
        return new NicknameSearchResponse(mapNicknameDtoList(participantList));
    }

    private static List<NicknameDto> mapNicknameDtoList(List<Participant> participantList) {
        return participantList.stream()
                .map(p -> new NicknameDto(p.getNickname()))
                .collect(Collectors.toList());
    }
}
