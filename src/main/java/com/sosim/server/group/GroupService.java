package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.MyGroupPageDto;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.group.dto.response.MyGroupDto;
import com.sosim.server.group.dto.response.MyGroupsResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_GROUP;
import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public GroupIdResponse createGroup(long userId, CreateGroupRequest createGroupRequest) {
        User user = findUser(userId);
        Group group = groupRepository.save(createGroupRequest.toEntity());

        saveAdminParticipant(createGroupRequest, user, group);

        return GroupIdResponse.toDto(group);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);

        boolean isAdmin = group.isAdminUser(userId);
        boolean isInto = group.hasParticipant(userId);
        int numberOfParticipants = group.getNumberOfParticipants();
        return GetGroupResponse.toDto(group, isAdmin, numberOfParticipants, isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(long userId, long groupId, ModifyGroupRequest modifyGroupRequest) {
        Group group = findGroup(groupId);
        group.update(userId, modifyGroupRequest);

        return GroupIdResponse.toDto(group);
    }

    @Transactional
    public void deleteGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);

        group.deleteGroup(userId);
    }

    @Transactional
    public void modifyAdmin(long userId, long groupId, ParticipantNicknameRequest nicknameRequest) {
        Group group = findGroupWithParticipants(groupId);

        group.modifyAdmin(userId, nicknameRequest.getNickname());
    }

    @Transactional(readOnly = true)
    public MyGroupsResponse getMyGroups(long userId, int page) {
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);
        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageDto.getOffset(), pageDto.getLimit());

        List<MyGroupDto> myGroupDtoList = toMyGroupDtoList(userId, myGroups);
        return MyGroupsResponse.toResponseDto(myGroups.hasNext(), myGroupDtoList);
    }

    private static List<MyGroupDto> toMyGroupDtoList(long userId, Slice<Group> myGroups) {
        return myGroups.stream()
                .map(g -> MyGroupDto.toDto(g, g.isAdminUser(userId)))
                .collect(Collectors.toList());
    }

    private void saveAdminParticipant(CreateGroupRequest createGroupRequest, User user, Group group) {
        String adminNickname = createGroupRequest.getNickname();
        Participant admin = Participant.create(user, group, adminNickname, true);
        participantRepository.save(admin);
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    private Group findGroup(long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

    private Group findGroupWithParticipants(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }
}
