package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.group.dto.response.GetGroupListResponse;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantService;
import com.sosim.server.participant.dto.request.CreateParticipantRequest;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.user.User;
import com.sosim.server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ParticipantService participantService;

    public GroupIdResponse createGroup(Long userId, CreateGroupRequest createGroupRequest) {
        User userEntity = userService.getUserEntity(userId);
        Group groupEntity = saveGroupEntity(Group.create(userId, createGroupRequest));
        participantService.createParticipant(userId, groupEntity.getId(), createGroupRequest.getNickname());

        return GroupIdResponse.create(groupEntity);
    }

    public GetGroupResponse getGroup(Long userId, Long groupId) {
        Group groupEntity = getGroupEntity(groupId);
        boolean isInto = false;

        try {
            if (userId != 0) {
                isInto = participantService.findParticipant(userId, groupId) != null;
            }
        } catch (CustomException ignored) {}

        return GetGroupResponse.create(groupEntity, groupEntity.getAdminId().equals(userId),
                (int) groupEntity.getParticipantList().stream()
                        .filter(p -> p.getStatus().equals(Status.ACTIVE)).count(), isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(Long userId, Long groupId, UpdateGroupRequest updateGroupRequest) {
        Group groupEntity = getGroupEntity(groupId);

        if (!groupEntity.getAdminId().equals(userId)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }
        groupEntity.update(updateGroupRequest);

        if (updateGroupRequest.getNickname() != null) {
            modifyNickname(userId, groupId, new CreateParticipantRequest(updateGroupRequest.getNickname()));
        }

        return GroupIdResponse.create(groupEntity);
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        Group groupEntity = getGroupEntity(groupId);

        if (!groupEntity.getAdminId().equals(userId)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }

        if (groupEntity.getParticipantList().stream()
                .filter(p -> p.getStatus().equals(Status.ACTIVE)).count() > 1) {
            throw new CustomException(ResponseCode.NONE_ZERO_PARTICIPANT);
        }

        participantService.deleteParticipant(userId, groupId);
        groupEntity.delete();
    }

    @Transactional
    public void modifyAdmin(Long userId, Long groupId, CreateParticipantRequest createParticipantRequest) {
        Group groupEntity = getGroupEntity(groupId);

        if (!groupEntity.getAdminId().equals(userId)) {
            throw new CustomException(ResponseCode.NONE_ADMIN);
        }

        Participant participantEntity = participantService
                .findParticipant(createParticipantRequest.getNickname(), groupId);

        if (groupEntity.getParticipantList().stream()
                .noneMatch(p -> p.getId().equals(participantEntity.getId()))) {
            throw new CustomException(ResponseCode.NONE_PARTICIPANT);
        }

        groupEntity.modifyAdmin(participantEntity);
    }

//    @Transactional
//    public void withdrawGroup(Long userId, Long groupId) {
//        Group groupEntity = getGroupEntity(groupId);
//        participantService.deleteParticipant(userId, groupId);
//        if (groupEntity.getParticipantList().stream().noneMatch(p -> p.getStatus().equals(Status.ACTIVE))) {
//            groupEntity.delete();
//        }
//    }

    public void modifyNickname(Long userId, Long groupId, CreateParticipantRequest createParticipantRequest) {
        Group groupEntity = getGroupEntity(groupId);
        Participant participant = participantService.modifyNickname(userId, groupId, createParticipantRequest);

        if (groupEntity.getAdminId().equals(userId)) {
            groupEntity.modifyAdmin(participant);
        }
    }

    public GetGroupListResponse getMyGroups(Long index, Long userId) {
        Slice<Participant> slice = participantService.getParticipantSlice(index, userId);
        List<Participant> participantList = slice.getContent();

        if (participantList.isEmpty()) {
            throw new CustomException(ResponseCode.NO_MORE_GROUP);
        }

        List<GetGroupResponse> groupList = new ArrayList<>();
        for (Participant participant : participantList) {
            Group group = participant.getGroup();
            groupList.add(GetGroupResponse.create(group, group.getAdminId().equals(userId),
                    (int) group.getParticipantList().stream()
                            .filter(p -> p.getStatus().equals(Status.ACTIVE)).count(),true));
        }

        return GetGroupListResponse.create(participantList.get(participantList.size() - 1).getId(),
                slice.hasNext(), groupList);
    }

    public GetNicknameResponse getMyNickname(Long userId, Long groupId) {
        return participantService.getMyNickname(userId, groupId);
    }

    public Group saveGroupEntity(Group group) {
        return groupRepository.save(group);
    }

    public Group getGroupEntity(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_GROUP));
    }
}
