package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.MyGroupPageDto;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.request.NotificationSettingRequest;
import com.sosim.server.group.dto.response.*;
import com.sosim.server.notification.dto.request.ModifyAdminNotificationRequest;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GroupIdResponse createGroup(long userId, CreateGroupRequest createGroupRequest) {
        User user = findUser(userId);
        //TODO: Group Setting Info 추가
        Group group = createGroupRequest.toEntity();

        long groupId = saveGroupAndAdmin(createGroupRequest, user, group);

        return GroupIdResponse.toDto(groupId);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(long userId, long groupId) {
        Group group = findGroup(groupId);

        boolean isAdmin = group.isAdminUser(userId);
        boolean isInto = group.hasParticipant(userId);
        int numberOfParticipants = group.getNumberOfParticipants();
        return GetGroupResponse.toDto(group, isAdmin, numberOfParticipants, isInto);
    }

    @Transactional
    public GroupIdResponse updateGroup(long userId, long groupId, ModifyGroupRequest modifyGroupRequest) {
        Group group = findGroup(groupId);
        group.update(userId, modifyGroupRequest);

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
        Group group = findGroup(groupId);
        group.modifyAdmin(userId, nicknameRequest.getNickname());
        List<Long> receiverUserIdList = participantRepository.getReceiverUserIdList(groupId);

        ModifyAdminNotificationRequest notification = ModifyAdminNotificationRequest.toDto(group, nicknameRequest.getNickname(), receiverUserIdList);
        eventPublisher.publishEvent(notification);
    }

    @Transactional(readOnly = true)
    public MyGroupsResponse getMyGroups(long userId, int page) {
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);
        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageDto.getOffset(), pageDto.getLimit());

        List<MyGroupDto> myGroupDtoList = toMyGroupDtoList(userId, myGroups);
        return MyGroupsResponse.toResponseDto(myGroups.hasNext(), myGroupDtoList);
    }

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSetting(long userId, long groupId) {
        Group group = findGroupWithNotificationSettingInfo(groupId);
        NotificationSettingInfo settingInfo = group.getNotificationSettingInfo(userId);

        return NotificationSettingResponse.toResponse(settingInfo);
    }

    @Transactional
    public void setNotificationSetting(long userId, long groupId, NotificationSettingRequest settingRequest) {
        NotificationSettingInfo settingInfo = settingRequest.toSettingInfoVO();

        Group group = findGroupWithNotificationSettingInfo(groupId);
        group.changeNotificationSettingInfo(userId, settingInfo);
    }

    private void changeAdminNickname(Group group, String newNickname) {
        Participant admin = group.getAdminParticipant();
        admin.modifyNickname(group, newNickname);
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

    private Group findGroupWithNotificationSettingInfo(long groupId) {
        return groupRepository.findByIdWithNotificationSettingInfo(groupId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_GROUP));
    }

}
