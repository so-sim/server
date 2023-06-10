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

    public void creteParticipant(User userEntity, Group groupEntity, String nickname) {
        if (participantRepository.existsByUserAndGroup(userEntity, groupEntity)) {
            throw new CustomException(ResponseCode.ALREADY_INTO_GROUP);
        }

        if (participantRepository.existsByGroupAndNickname(groupEntity, nickname)) {
            throw new CustomException(ResponseCode.ALREADY_USE_NICKNAME);
        }

        saveParticipantEntity(Participant.create(userEntity, groupEntity, nickname));
    }

    public void deleteParticipant(User user, Group group) {
        getParticipantEntity(user, group).delete();
    }

    @Transactional
    public Participant modifyNickname(User user, Group group, ParticipantNicknameRequest participantNicknameRequest) {
        if (participantRepository.existsByGroupAndNickname(group, participantNicknameRequest.getNickname())) {
            throw new CustomException(ResponseCode.ALREADY_USE_NICKNAME);
        }

        Participant participantEntity = getParticipantEntity(user, group);
        participantEntity.modifyNickname(participantNicknameRequest);
        return participantEntity;
    }

    public GetNicknameResponse getMyNickname(User user, Group group) {
        return GetNicknameResponse.create(getParticipantEntity(user, group));
    }

    public void saveParticipantEntity(Participant participant) {
        participantRepository.save(participant);
    }

    private Participant getParticipantEntity(User user, Group group) {
        return participantRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT));
    }
}
