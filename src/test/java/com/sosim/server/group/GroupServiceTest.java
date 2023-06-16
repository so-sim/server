package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantService;
import com.sosim.server.user.User;
import com.sosim.server.user.UserService;
import org.junit.jupiter.api.Disabled;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private long userId = 1L;
    private long groupId = 1L;

    @InjectMocks
    GroupService groupService;
    @Mock
    GroupRepository groupRepository;
    @Mock
    UserService userService;
    @Mock
    ParticipantService participantService;

    @Disabled //TODO Group 리팩토링 후 제거
    @DisplayName("그룹 생성 / 성공")
    @Test
    void create_group() {
        //given
        CreateGroupRequest request = CreateGroupRequest.builder().build();

        User user = new User();
        Group group = Group.create(userId, request);
        ReflectionTestUtils.setField(group, "id", groupId);

        doReturn(user).when(userService).getUserEntity(userId);
        doReturn(group).when(groupRepository).save(any(Group.class));
        
        //when
        GroupIdResponse response = groupService.createGroup(userId, request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(groupId);
    }

    @Disabled //TODO Group 리팩토링 후 제거
    @DisplayName("그룹 생성 / User가 없는 경우 CustomException(NOT_FOUND_USER)")
    @Test
    void create_group_no_user() {
        //given
        CreateGroupRequest request = CreateGroupRequest.builder().build();

        CustomException e = new CustomException(NOT_FOUND_USER);
        doThrow(e).when(userService).getUserEntity(userId);

        //when
        CustomException exception = assertThrows(CustomException.class, () ->
                groupService.createGroup(userId, request));

        //then
        assertThat(exception.getResponseCode()).isEqualTo(NOT_FOUND_USER);
    }

    @DisplayName("그룹 상세조회 / 성공")
    @Test
    void get_group() {
        //given
        Group group = Group.builder().build();
        String title = "타이틀";
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "title", title);
        ReflectionTestUtils.setField(group, "adminId", userId);
        Participant participant = new Participant();

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(participant).when(participantService).findParticipant(userId, groupId);

        //when
        GetGroupResponse response = groupService.getGroup(userId, groupId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(groupId);
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getIsInto()).isEqualTo(true);
    }

    @DisplayName("그룹 상세조회 / 참가자 데이터가 없는 경우 isInto는 false")
    @Test
    void get_group_no_participant() {
        //given
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "adminId", userId);

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        CustomException e = new CustomException(NONE_PARTICIPANT);
        doThrow(e).when(participantService).findParticipant(userId, groupId);

        //when
        GetGroupResponse response = groupService.getGroup(userId, groupId);

        //then
        assertThat(response.getIsInto()).isFalse();
    }

    @DisplayName("그룹 변경 / 성공")
    @Test
    void modify_group() {
        //given
        String title = "타이틀";
        String nickname = null;
        String groupType = "그룹 타입";
        String colorType = "색";
        UpdateGroupRequest request = makeUpdateGroupRequest(title, nickname, groupType, colorType);

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "adminId", userId);

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
        String title = "타이틀";
        String nickname = null;
        String groupType = "그룹 타입";
        String colorType = "색";
        UpdateGroupRequest request = makeUpdateGroupRequest(title, nickname, groupType, colorType);

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        long isNotAdminId = userId + 1;
        ReflectionTestUtils.setField(group, "adminId", isNotAdminId);

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
        ReflectionTestUtils.setField(group, "adminId", userId);

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

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
        long notAdminId = userId + 1;
        ReflectionTestUtils.setField(group, "adminId", notAdminId);

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

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
        ReflectionTestUtils.setField(group, "adminId", userId);
        group.getParticipantList().add(Participant.builder().build());
        group.getParticipantList().add(Participant.builder().build());

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                groupService.deleteGroup(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ZERO_PARTICIPANT);
    }

    private static UpdateGroupRequest makeUpdateGroupRequest(String title, String nickname, String groupType, String colorType) {
        return UpdateGroupRequest.builder()
                .title(title)
                .nickname(nickname)
                .groupType(groupType)
                .coverColorType(colorType)
                .build();
    }

}