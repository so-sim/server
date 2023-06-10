package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.CreateGroupResponse;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.participant.ParticipantService;
import com.sosim.server.user.User;
import com.sosim.server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ParticipantService participantService;

    public CreateGroupResponse createGroup(Long userId, CreateGroupRequest createGroupRequest) {
        User userEntity = userService.getUser(userId);
        Group groupEntity = saveGroupEntity(Group.create(userId, createGroupRequest));
        participantService.creteParticipant(userEntity, groupEntity, createGroupRequest.getNickname());

        return CreateGroupResponse.create(groupEntity);
    }

    public GetGroupResponse getGroup(Long userId, Long groupId) {
        Group groupEntity = getGroupEntity(groupId);
        boolean isInto = false;

        try {
            if (userId != 0) {
                isInto = participantService.getParticipantEntity(
                        userService.getUser(userId), groupEntity) != null;
            }
        } catch (CustomException ignored) {}

        return GetGroupResponse.create(groupEntity, groupEntity.getAdminId().equals(userId),
                (int) groupEntity.getParticipantList().stream()
                        .filter(p -> p.getStatus().equals(Status.ACTIVE)).count(), isInto);
    }

    public Group saveGroupEntity(Group group) {
        return groupRepository.save(group);
    }

    public Group getGroupEntity(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_GROUP));
    }
}
