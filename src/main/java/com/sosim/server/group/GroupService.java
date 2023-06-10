package com.sosim.server.group;

import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.CreateGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public CreateGroupResponse createGroup(Long userId, CreateGroupRequest createGroupRequest) {
        Group groupEntity = saveGroupEntity(Group.create(userId, createGroupRequest));

        return CreateGroupResponse.create(groupEntity);
    }

    public Group saveGroupEntity(Group group) {
        return groupRepository.save(group);
    }
}
