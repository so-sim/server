package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
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
import org.springframework.data.domain.Pageable;
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

        String adminNickname = createGroupRequest.getNickname();
        Participant admin = Participant.create(user, group, adminNickname, true);
        participantRepository.save(admin);

        return GroupIdResponse.toDto(group);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);
        //TODO : user N + 1 발생하는지 테스트 필요
        Participant myParticipant = group.getUserParticipant(userId);

        //TODO : IsInto 결정되면 리팩토링
        boolean isAdmin = group.isAdminUser(userId);
        boolean isInto = group.hasParticipant(userId);
        int numberOfParticipants = group.getNumberOfParticipants();
        return GetGroupResponse.toDto(group, isAdmin, numberOfParticipants, isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(long userId, long groupId, UpdateGroupRequest updateGroupRequest) {
        Group group = findGroup(groupId);
        group.update(userId, updateGroupRequest);

        return GroupIdResponse.toDto(group);
    }

    @Transactional
    public void deleteGroup(long userId, long groupId) {
        Group group = findGroupWithParticipants(groupId);

        //TODO : 참가자 삭제 로직과 겹치므로 의논 후 수정
        group.deleteGroup(userId);
    }

    @Transactional
    public void modifyAdmin(long userId, long groupId, ParticipantNicknameRequest nicknameRequest) {
        Group group = findGroupWithParticipants(groupId);

        group.modifyAdmin(userId, nicknameRequest.getNickname());
    }

    @Transactional(readOnly = true)
    public MyGroupsResponse getMyGroups(long userId, Pageable pageable) {
        //TODO : 조회 결과가 없을 때 Exception 처리를 넣어야 하는지?
        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageable);

        List<MyGroupDto> myGroupDtos = myGroups.stream()
                .map(g -> MyGroupDto.toDto(g, g.isAdminUser(userId)))
                .collect(Collectors.toList());

        return MyGroupsResponse.toResponseDto(myGroups.hasNext(), myGroupDtos);
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    private Slice<Participant> getMyParticipants(long userId, Pageable pageable) {
        return participantRepository.findByUserId(userId, pageable);
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
