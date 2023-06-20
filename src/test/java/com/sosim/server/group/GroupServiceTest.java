package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private long userId = 1L;
    private long groupId = 1L;

    @InjectMocks
    GroupService groupService;
    @Mock
    GroupRepository groupRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ParticipantRepository participantRepository;

    @DisplayName("그룹 생성 / 성공")
    @Test
    void create_group() {
        //given
        CreateGroupRequest request = CreateGroupRequest.builder().build();

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(group).when(groupRepository).save(any(Group.class));
        
        //when
        GroupIdResponse response = groupService.createGroup(userId, request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(groupId);

        verify(groupRepository, times(1)).save(any(Group.class));
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @DisplayName("그룹 생성 / User가 없는 경우 CustomException(NOT_FOUND_USER)")
    @Test
    void create_group_no_user() {
        //given
        CreateGroupRequest request = CreateGroupRequest.builder().build();

        doReturn(Optional.empty()).when(userRepository).findById(userId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.createGroup(userId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_USER);

        verify(groupRepository, times(0)).save(any(Group.class));
    }

    @DisplayName("그룹 상세조회 / 성공")
    @Test
    void get_group() {
        //given
        Group group = Group.builder().build();
        String title = "타이틀";
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "title", title);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        String nickname = "닉네임";
        Participant admin = Participant.create(user, group, nickname, true);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetGroupResponse response = groupService.getGroup(userId, groupId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(groupId);
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getAdminNickname()).isEqualTo(admin.getNickname());
        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getIsInto()).isTrue();
    }

    @DisplayName("그룹 상세조회 / 참여하지 않은 그룹인 경우 isInto는 False")
    @Test
    void get_group_is_not_into() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId + 1, true);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetGroupResponse response = groupService.getGroup(userId, groupId);

        //then
        assertThat(response.getIsInto()).isFalse();
    }

    @DisplayName("그룹 상세조회 / 일반 유저인 경우 isAdmin은 False, isInto는 true")
    @Test
    void get_group_is_not_admin() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetGroupResponse response = groupService.getGroup(userId, groupId);

        //then
        assertThat(response.getIsAdmin()).isFalse();
        assertThat(response.getIsInto()).isTrue();
    }

    @DisplayName("그룹 상세조회 / 조회한 유저가 참가하지 않은 경우 IsInto는 False")
    @Test
    void get_group_no_group() {
        //given
        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.getGroup(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);
    }

    @DisplayName("그룹 변경 / 성공")
    @Test
    void modify_group() {
        //given
        String title = "타이틀";
        String groupType = "그룹 타입";
        String colorType = "색";
        ModifyGroupRequest request = makeUpdateGroupRequest(title, groupType, colorType);

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId, true);

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

        //when
        GroupIdResponse response = groupService.updateGroup(userId, groupId, request);

        //then
        assertThat(response.getGroupId()).isEqualTo(groupId);
        assertThat(group.getTitle()).isEqualTo(title);
        assertThat(group.getGroupType()).isEqualTo(groupType);
        assertThat(group.getCoverColor()).isEqualTo(colorType);
    }

    @DisplayName("그룹 변경 / 관리자가 아닌 경우 CustomException(NONE_ADMIN)")
    @Test
    void modify_group_not_admin() {
        //given
        ModifyGroupRequest request = makeUpdateGroupRequest("타이틀", "그룹 타입", "색");

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId, false);

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.updateGroup(userId, groupId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ADMIN);
    }

    @DisplayName("그룹 삭제 / 성공")
    @Test
    void delete_group() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId, true);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        groupService.deleteGroup(userId, groupId);

        //then
        assertThat(group.getStatus()).isEqualTo(Status.DELETED);
        assertThat(group.getDeleteDate()).isNotNull();
    }

    @DisplayName("그룹 삭제 / 요청자가 Admin이 아닌 경우 CustomException(NONE_ADMIN)")
    @Test
    void delete_group_not_admin() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.deleteGroup(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ADMIN);
    }

    @DisplayName("그룹 삭제 / 다른 참가자가 1명 이상 존재하면 CustomException(NONE_ZERO_PARTICIPANT) ")
    @Test
    void delete_group_participant_more_than_1() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        addParticipantInGroup(group, userId, true);
        addParticipantInGroup(group, userId + 1, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.deleteGroup(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ZERO_PARTICIPANT);
    }

    private Participant addParticipantInGroup(Group group, long userId, boolean isAdmin) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        return Participant.create(user, group, "닉네임" + userId, isAdmin);
    }

    private static ModifyGroupRequest makeUpdateGroupRequest(String title, String groupType, String colorType) {
        return ModifyGroupRequest.builder()
                .title(title)
                .type(groupType)
                .coverColor(colorType)
                .build();
    }

}