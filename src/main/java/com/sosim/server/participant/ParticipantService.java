package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.Group;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public void creteParticipant(User user, Group group, String nickname) {
        if (participantRepository.existsByUserIdAndGroupId(user.getId(), group.getId())) {
            throw new CustomException(ResponseCode.ALREADY_INTO_GROUP);
        }

        if (participantRepository.existsByGroupIdAndNickname(group.getId(), nickname)) {
            throw new CustomException(ResponseCode.ALREADY_USE_NICKNAME);
        }

        saveParticipantEntity(Participant.create(user, group, nickname));
    }

    public void deleteParticipant(Long userId, Long groupId) {
        getParticipantEntity(userId, groupId).delete();
    }

    @Transactional
    public Participant modifyNickname(Long userId, Long groupId, ParticipantNicknameRequest participantNicknameRequest) {
        if (participantRepository.existsByGroupIdAndNickname(groupId, participantNicknameRequest.getNickname())) {
            throw new CustomException(ResponseCode.ALREADY_USE_NICKNAME);
        }

        Participant participantEntity = getParticipantEntity(userId, groupId);
        participantEntity.modifyNickname(participantNicknameRequest);
        return participantEntity;
    }

    public GetNicknameResponse getMyNickname(Long userId, Long groupId) {
        return GetNicknameResponse.create(getParticipantEntity(userId, groupId));
    }

    public void saveParticipantEntity(Participant participant) {
        participantRepository.save(participant);
    }

    public Participant getParticipantEntity(Long userId, Long groupId) {
        return participantRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT));
    }

    public Participant getParticipantEntity(String nickname, Long groupId) {
        return participantRepository.findByNicknameAndGroupId(nickname, groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT));
    }
}
