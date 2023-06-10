package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.Group;
import com.sosim.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void saveParticipantEntity(Participant participant) {
        participantRepository.save(participant);
    }

    private Participant getParticipantEntity(User user, Group group) {
        return participantRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new CustomException(ResponseCode.NONE_PARTICIPANT));
    }
}
