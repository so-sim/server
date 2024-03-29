package com.sosim.server.group.service;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.domain.repository.EventRepository;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.group.domain.util.MyGroupPaginationUtil;
import com.sosim.server.group.dto.MyGroupPageDto;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.response.*;
import com.sosim.server.notification.util.NotificationUtil;
import com.sosim.server.participant.domain.entity.Participant;
import com.sosim.server.participant.domain.repository.ParticipantRepository;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.user.domain.entity.User;
import com.sosim.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_GROUP;
import static com.sosim.server.common.response.ResponseCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final NotificationUtil notificationUtil;

    @Transactional
    public GroupIdResponse createGroup(long userId, CreateGroupRequest createGroupRequest) {
        User user = findUser(userId);
        Group group = createGroupRequest.toEntity();

        long groupId = saveGroupAndAdmin(createGroupRequest, user, group);

        return GroupIdResponse.toDto(groupId);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(long userId, long groupId) {
        Group group = findGroup(groupId);

        boolean isAdmin = group.isAdminUser(userId);
        boolean isInto = group.hasParticipant(userId);
        return GetGroupResponse.toDto(group, isAdmin, isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(long userId, long groupId, ModifyGroupRequest modifyGroupRequest) {
        Group group = findGroupWithParticipantsIgnoreStatus(groupId);
        group.update(userId, modifyGroupRequest);

        notificationUtil.modifyGroupTitle(groupId, group.getTitle());
        changeAdminNickname(group, modifyGroupRequest.getNickname());

        return GroupIdResponse.toDto(group.getId());
    }

    @Transactional
    public void deleteGroup(long userId, long groupId) {
        Group group = findGroup(groupId);

        group.deleteGroup(userId);
    }

    @Transactional
    public void modifyAdmin(long userId, long groupId, ParticipantNicknameRequest nicknameRequest) {
        String newAdminNickname = nicknameRequest.getNickname();

        Group group = findGroup(groupId);
        group.modifyAdmin(userId, newAdminNickname);

        notificationUtil.sendModifyAdminNotification(group);
    }

    @Transactional(readOnly = true)
    public MyGroupsResponse getMyGroups(long userId, int page) {
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);
        Slice<MyGroupDto> myGroups = groupRepository.findMyGroups(userId, pageDto.getOffset(), pageDto.getLimit());

        return MyGroupsResponse.toResponseDto(myGroups.hasNext(), myGroups.getContent());
    }

    @Transactional(readOnly = true)
    public GroupInvitationResponse getGroupForInvitation(long userId, long groupId) {
        Group group = findGroup(groupId);
        Optional<Participant> participant = participantRepository.findByUserIdAndGroupId(userId, groupId);
        boolean isInto = group.hasParticipant(userId);

        return GroupInvitationResponse.toDto(group, isInto, participant.isPresent(), participant.map(Participant::getNickname).orElse(null));
    }

    private void changeAdminNickname(Group group, String newNickname) {
        Participant admin = group.getAdminParticipant();
        String preNickname = admin.getNickname();
        admin.modifyNickname(group, newNickname);
        eventRepository.updateNicknameAll(newNickname, preNickname, group.getId());
        notificationUtil.modifyNickname(group.getId(), preNickname, newNickname);
    }

    private List<MyGroupDto> toMyGroupDtoList(long userId, Slice<Group> myGroups) {
        return myGroups.stream()
                .map(g -> MyGroupDto.toDto(g, g.isAdminUser(userId)))
                .collect(Collectors.toList());
    }

    private long saveGroupAndAdmin(CreateGroupRequest createGroupRequest, User user, Group group) {
        String adminNickname = createGroupRequest.getNickname();
        Participant admin = group.createParticipant(user, adminNickname, true);
        Group saveGroup = groupRepository.save(group);
        participantRepository.save(admin);
        return saveGroup.getId();
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    private Group findGroup(long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

    private Group findGroupWithParticipantsIgnoreStatus(long groupId) {
        return groupRepository.findByIdWithParticipantsIgnoreStatus(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }
}
