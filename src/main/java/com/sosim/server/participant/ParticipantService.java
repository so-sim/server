package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createParticipant(long userId, long groupId, String nickname) {
        User user = findUser(userId);
        Group group = findGroup(groupId);

        checkAlreadyIntoGroup(user, group);
        checkUsedNickname(group, nickname);

        saveNewParticipant(user, group, nickname);
    }

    @Transactional(readOnly = true)
    public GetParticipantListResponse getGroupParticipants(long userId, long groupId) {
        Group group = findGroup(groupId);
        //TODO admin 테이블 구조 변경 후 리팩토링
        List<Participant> normalParticipants = participantRepository.findGroupNormalParticipants(groupId, group.getAdminNickname());
        if (group.isAdminUser(userId)) {
            changeRequestUserOrderToFirst(userId, normalParticipants);
        }
        return GetParticipantListResponse.toDto(group, toNicknameList(normalParticipants));
    }

    @Transactional
    public void deleteParticipant(long userId, long groupId) {
        Group group = findGroup(groupId);
        Participant participant = findParticipant(userId, groupId);

        //TODO : 데이터 구조 변경 후, 총무 탈퇴 요청인데 다른 참가자 존재하는 경우 Exception 던지기
        participant.withdrawGroup(group);
    }

    @Transactional
    public void modifyNickname(long userId, long groupId, String newNickname) {
        Group group = findGroup(groupId);
        Participant participant = findParticipant(userId, groupId);

        participant.modifyNickname(group, newNickname);
    }

    @Transactional(readOnly = true)
    public GetNicknameResponse getMyNickname(long userId, long groupId) {
        Participant participant = findParticipant(userId, groupId);

        return GetNicknameResponse.toDto(participant);
    }

    public Participant findParticipant(long userId, long groupId) {
        return participantRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new CustomException(NONE_PARTICIPANT));
    }

    private void saveNewParticipant(User user, Group group, String nickname) {
        Participant intoParticipant = Participant.create(user, nickname);
        intoParticipant.addGroup(group);
        participantRepository.save(intoParticipant);
    }

    private void checkUsedNickname(Group group, String nickname) {
        if (participantRepository.existsByGroupIdAndNicknameAndStatus(group.getId(), nickname, Status.ACTIVE)) {
            throw new CustomException(ALREADY_USE_NICKNAME);
        }
    }

    private void checkAlreadyIntoGroup(User user, Group group) {
        if (participantRepository.existsByUserIdAndGroupIdAndStatus(user.getId(), group.getId(), Status.ACTIVE)) {
            throw new CustomException(ALREADY_INTO_GROUP);
        }
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    private static List<String> toNicknameList(List<Participant> normalParticipants) {
        return normalParticipants.stream()
                .map(Participant::getNickname)
                .collect(Collectors.toList());
    }

    private void changeRequestUserOrderToFirst(long userId, List<Participant> participants) {
        int index = getParticipantIndexOfUser(userId, participants);
        Participant participantOfUser = participants.remove(index);
        participants.add(0, participantOfUser);
    }

    private int getParticipantIndexOfUser(long userId, List<Participant> participants) {
        for (int i = 0; i < participants.size(); i++) {
            Participant participant = participants.get(i);
            if (isParticipantOfUser(userId, participant)) {
                return i;
            }
        }
        throw new CustomException(NONE_PARTICIPANT);
    }

    private boolean isParticipantOfUser(long userId, Participant participant) {
        return participant.getUser().getId().equals(userId);
    }

    private Group findGroup(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }
}
