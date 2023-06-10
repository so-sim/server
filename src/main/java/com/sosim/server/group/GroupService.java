package com.sosim.server.group;

import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.CreateGroupResponse;
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

    public Group saveGroupEntity(Group group) {
        return groupRepository.save(group);
    }
}
