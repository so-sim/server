package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.group.dto.response.MyGroupsResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.auditing.Status.ACTIVE;
import static com.sosim.server.common.response.ResponseCode.*;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public GroupIdResponse createGroup(long userId, CreateGroupRequest createGroupRequest) {
        User user = findUser(userId);
        Group group = groupRepository.save(createGroupRequest.toEntity(userId));

        Participant participant = Participant.create(user, createGroupRequest.getNickname());
        participant.addGroup(group);
        participantRepository.save(participant);

        return GroupIdResponse.toDto(group);
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);

        boolean isAdmin = group.isAdminUser(userId);
        //TODO : user N + 1 발생하는지 테스트 필요
        boolean isInto = group.hasParticipant(userId);
        int numberOfParticipants = group.getNumberOfParticipants();
        return GetGroupResponse.toDto(group, isAdmin, numberOfParticipants, isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(long userId, long groupId, UpdateGroupRequest updateGroupRequest) {
        Group group = findGroup(groupId);

        group.update(userId, updateGroupRequest);

        //TODO 논의 후 지우기
//        if (updateGroupRequest.getNickname() != null) {
//            modifyNickname(userId, groupId, new ParticipantNicknameRequest(updateGroupRequest.getNickname()));
//        }
        return GroupIdResponse.toDto(group);
    }

    @Transactional
    public void deleteGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);

        group.deleteGroup(userId);
    }

    @Transactional
    public void modifyAdmin(long userId, long groupId, ParticipantNicknameRequest nicknameRequest) {
        Group groupEntity = findGroupWithParticipants(groupId);

        groupEntity.modifyAdmin(userId, nicknameRequest.getNickname());
    }

    public MyGroupsResponse getMyGroups(long userId, Pageable pageable) {
        //TODO : 데이터 구조 변경 후 작업
//        Slice<Participant> slice = participantService.getParticipantSlice(index, userId);
        Slice<Participant> slice = null;
        List<Participant> participantList = slice.getContent();

        if (participantList.isEmpty()) {
            throw new CustomException(NO_MORE_GROUP);
        }

        List<GetGroupResponse> groupList = new ArrayList<>();

        for (Participant participant : participantList) {
            Group group = participant.getGroup();
            groupList.add(GetGroupResponse.toDto(group, group.isAdminUser(userId),
                    (int) group.getParticipantList().stream()
                            .filter(p -> p.getStatus().equals(ACTIVE)).count(),true));
        }

        return MyGroupsResponse.create(participantList.get(participantList.size() - 1).getId(),
                slice.hasNext(), groupList);
    }

    private Slice<Participant> getMyParticipants(long userId, Pageable pageable) {
        return participantRepository.findByUserId(userId, pageable);
    }
    public Group findGroup(long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

    public Group findGroupWithParticipants(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }
}
