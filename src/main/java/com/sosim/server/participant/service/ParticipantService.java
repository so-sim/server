package com.sosim.server.participant.service;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.domain.repository.EventRepository;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.notification.util.NotificationUtil;
import com.sosim.server.participant.domain.entity.Participant;
import com.sosim.server.participant.domain.repository.ParticipantRepository;
import com.sosim.server.participant.dto.NicknameSearchRequest;
import com.sosim.server.participant.dto.NicknameSearchResponse;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.user.domain.entity.User;
import com.sosim.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final NotificationUtil notificationUtil;

    @Transactional
    public void createParticipant(long userId, long groupId, String nickname) {
        User user = findUser(userId);
        Group group = findGroupWithParticipants(groupId);

        Participant participant = group.createParticipant(user, nickname, false);
        participantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public GetParticipantListResponse getGroupParticipants(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);
        List<Participant> participants = getParticipants(group);

        Participant admin = removeAdminInList(participants);
        if (userIsNotAdmin(userId, admin)) {
            changeUserOrderToFirst(userId, participants);
        }
        return GetParticipantListResponse.toDto(admin.getNickname(), toNicknameList(participants));
    }

    @Transactional
    public void deleteParticipant(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);
        Participant participant = findParticipant(userId, groupId);

        participant.withdrawGroup(group);
        notificationUtil.lockNotification(participant.getNickname(), groupId);
    }

    @Transactional
    public void modifyNickname(long userId, long groupId, String newNickname) {
        Group group = findGroupWithParticipants(groupId);
        Participant participant = findParticipant(userId, groupId);

        String preNickname = participant.getNickname();
        participant.modifyNickname(group, newNickname);

        eventRepository.updateNicknameAll(newNickname, preNickname, groupId);
        notificationUtil.modifyNickname(groupId, preNickname, newNickname);
    }

    @Transactional(readOnly = true)
    public GetNicknameResponse getMyNickname(long userId, long groupId) {
        Participant participant = findParticipant(userId, groupId);

        return GetNicknameResponse.toDto(participant);
    }

    @Transactional(readOnly = true)
    public NicknameSearchResponse searchParticipants(long groupId, NicknameSearchRequest searchRequest) {
        Group group = findGroup(groupId);
        List<Participant> participantList = participantRepository.findByGroupAndNicknameContainsIgnoreCase(group, searchRequest.getKeyword());

        return NicknameSearchResponse.toDto(participantList);
    }

    private Participant findParticipant(long userId, long groupId) {
        return participantRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT));
    }

    private List<Participant> getParticipants(Group group) {
        List<Participant> participants = new ArrayList<>(group.getParticipantList());
        sortByNickname(participants);
        return participants;
    }

    private void sortByNickname(List<Participant> participants) {
        participants.sort(Comparator.comparing(Participant::getNickname));
    }

    private Participant removeAdminInList(List<Participant> participants) {
        Participant admin = participants.stream()
                .filter(Participant::isAdmin)
                .findFirst()
                .orElseThrow(() -> new CustomException(NOT_FOUND_PARTICIPANT));
        participants.remove(admin);
        return admin;
    }

    private boolean userIsNotAdmin(long userId, Participant admin) {
        return !admin.isMine(userId);
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

    private void changeUserOrderToFirst(long userId, List<Participant> participants) {
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
        throw new CustomException(NOT_FOUND_PARTICIPANT);
    }

    private boolean isParticipantOfUser(long userId, Participant participant) {
        return participant.getUser().getId().equals(userId);
    }

    private Group findGroupWithParticipants(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

    private Group findGroup(long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

}
