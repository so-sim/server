package com.sosim.server.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.group.dto.response.MyGroupDto;
import com.sosim.server.group.dto.response.MyGroupsResponse;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.security.WithMockCustomUser;
import com.sosim.server.security.WithMockCustomUserSecurityContextFactory;
import com.sosim.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GroupController.class})
class GroupControllerTest {

    static final String URI_PREFIX = "/api/group";
    private Long userId = WithMockCustomUserSecurityContextFactory.USER_ID;
    private long groupId = 1L;

    @MockBean
    @Autowired
    GroupService groupService;

    MockMvc mvc;

    @Autowired
    WebApplicationContext was;

    ObjectMapper om;

    @BeforeEach
    public void init() {
        mvc = MockMvcBuilders.webAppContextSetup(was).build();
        om = new ObjectMapper();
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / 성공")
    @Test
    void create_group() throws Exception {
        //given
        CreateGroupRequest request = makeCreateRequest("그루비룸", "닉네임", "스터디", "색");
        GroupIdResponse response = GroupIdResponse.builder().groupId(groupId).build();

        doReturn(response).when(groupService).createGroup(userId, request);

        //when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(CREATE_GROUP.getCode()))
                .andExpect(jsonPath("$.content.groupId").value(groupId));

        verify(groupService, times(1)).createGroup(userId, request);
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / User 없는 경우")
    @Test
    void create_group_no_user() throws Exception {
        //given
        CreateGroupRequest request = makeCreateRequest("그루비룸", "닉네임", "스터디", "색");
        CustomException e = new CustomException(NOT_FOUND_USER);

        doThrow(e).when(groupService).createGroup(userId, request);

        //when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_USER.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_USER.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / title 유효성 검사")
    @Test
    void create_title_fail() throws Exception {
        //given
        CreateGroupRequest shortTitle = makeCreateRequest("", "닉네임", "스터디", "색");
        CreateGroupRequest longTitle = makeCreateRequest("그루비룸그루비룸그루비룸그루비룸", "닉네임", "스터디", "색");

        //when
        ResultActions shortActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(shortTitle)));
        ResultActions longActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(longTitle)));

        //then
        shortActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("title"));

        longActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("title"))
                .andExpect(jsonPath("$.content.message").value("모임 이름은 최소 1글자, 최대 15글자까지 허용됩니다."));
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / nickname 유효성 검사")
    @Test
    void create_nickname_fail() throws Exception {
        //given
        CreateGroupRequest shortNickname = makeCreateRequest("그루비룸", "", "스터디", "색");
        CreateGroupRequest longNickname = makeCreateRequest("그루비룸", "닉네임1닉네임1닉네임1닉네임1", "스터디", "색");

        //when
        ResultActions shortActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(shortNickname)));
        ResultActions longActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(longNickname)));

        //then
        shortActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("nickname"));

        longActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("nickname"))
                .andExpect(jsonPath("$.content.message").value("닉네임은 최소 1글자, 최대 15글자까지 허용됩니다."));
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / groupType 유효성 검사")
    @Test
    void create_groupType_fail() throws Exception {
        //given
        CreateGroupRequest nullType = makeCreateRequest("그루비룸", "닉네임", null, "색");

        //when
        ResultActions nullActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(nullType)));

        //then
        nullActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("groupType"));
    }

    @WithMockCustomUser
    @DisplayName("그룹 생성 / coverColor 유효성 검사")
    @Test
    void create_coverColor_fail() throws Exception {
        //given
        CreateGroupRequest nullType = makeCreateRequest("그루비룸", "닉네임", "타입", null);

        //when
        ResultActions nullActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(nullType)));

        //then
        nullActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("coverColor"));
    }

    @WithMockCustomUser
    @DisplayName("그룹 조회 / 성공")
    @Test
    void get_group() throws Exception {
        //given
        GetGroupResponse getGroupResponse = makeGetGroupResponse();

        doReturn(getGroupResponse).when(groupService).getGroup(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(GET_GROUP.getCode()))
                .andExpect(jsonPath("$.content.groupId").value(groupId))
                .andExpect(jsonPath("$.content.isAdmin").value(false))
                .andExpect(jsonPath("$.content.isInto").value(true));

        verify(groupService, times(1)).getGroup(userId, groupId);
    }

    @WithMockCustomUser
    @DisplayName("그룹 조회 / Group이 없는 경우")
    @Test
    void get_group_fail() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_GROUP);

        doThrow(e).when(groupService).getGroup(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / 성공")
    @Test
    void update_group() throws Exception {
        //given
        ModifyGroupRequest request = makeUpdateRequest("그루비룸", "스터디", "색");
        GroupIdResponse response = GroupIdResponse.builder().groupId(groupId).build();

        doReturn(response).when(groupService).updateGroup(userId, groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(MODIFY_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(MODIFY_GROUP.getMessage()))
                .andExpect(jsonPath("$.content.groupId").value(groupId));

        verify(groupService, times(1)).updateGroup(userId, groupId, request);
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / Group이 없는 경우")
    @Test
    void update_group_no_group() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_GROUP);
        ModifyGroupRequest request = makeUpdateRequest("그루비룸", "스터디", "색");

        doThrow(e).when(groupService).updateGroup(userId, groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / 그룹 Admin이 아닌 경우")
    @Test
    void update_group_not_admin() throws Exception {
        //given
        CustomException e = new CustomException(NONE_ADMIN);
        ModifyGroupRequest request = makeUpdateRequest("그루비룸", "스터디", "색");

        doThrow(e).when(groupService).updateGroup(userId, groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / title 유효성 검사")
    @Test
    void update_title_fail() throws Exception {
        //given
        ModifyGroupRequest shortTitle = makeUpdateRequest("", "스터디", "색");
        ModifyGroupRequest longTitle = makeUpdateRequest("그루비룸그루비룸그루비룸그루비룸", "스터디", "색");

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions shortActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(shortTitle)));
        ResultActions longActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(longTitle)));

        //then
        shortActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("title"));

        longActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("title"))
                .andExpect(jsonPath("$.content.message").value("모임 이름은 최소 1글자, 최대 15글자까지 허용됩니다."));
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / groupType 유효성 검사")
    @Test
    void update_groupType_fail() throws Exception {
        //given
        ModifyGroupRequest nullType = makeUpdateRequest("그루비룸", null, "색");

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions nullActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(nullType)));

        //then
        nullActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("type"));
    }

    @WithMockCustomUser
    @DisplayName("그룹 수정 / coverColor 유효성 검사")
    @Test
    void update_coverColor_fail() throws Exception {
        //given
        ModifyGroupRequest nullType = makeUpdateRequest("그루비룸", "타입", null);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions nullActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(nullType)));

        //then
        nullActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("coverColor"));
    }

    @WithMockCustomUser
    @DisplayName("그룹 삭제 / 성공")
    @Test
    void delete_group() throws Exception {
        //given

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(DELETE_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(DELETE_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());

        verify(groupService, times(1)).deleteGroup(userId, groupId);
    }

    @WithMockCustomUser
    @DisplayName("그룹 삭제 / Admin이 아닌 경우")
    @Test
    void delete_group_not_admin() throws Exception {
        //given
        CustomException e = new CustomException(NONE_ADMIN);
        doThrow(e).when(groupService).deleteGroup(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 삭제 / 그룹 인원이 1명 이상인 경우")
    @Test
    void delete_group_more_than_1_participants() throws Exception {
        //given
        CustomException e = new CustomException(NONE_ZERO_PARTICIPANT);
        doThrow(e).when(groupService).deleteGroup(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(NONE_ZERO_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ZERO_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("그룹 삭제 / 해당 참가자가 없는 경우")
    @Test
    void delete_group_has_no_participant() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(groupService).deleteGroup(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("내 모임 조회 / 성공")
    @Test
    void get_my_groups() throws Exception {
        //given
        int page = 0;
        int resultSize = 17;
        long groupId = 1L;

        List<MyGroupDto> myGroupDtos = new ArrayList<>();

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId++);
        addParticipantInGroup(group, userId, true);
        myGroupDtos.add(MyGroupDto.toDto(group, true));
        for (int i = 0; i < resultSize - 1; i++) {
            Group temp = Group.builder().build();
            ReflectionTestUtils.setField(temp, "id", groupId++);
            addParticipantInGroup(temp, userId + 1, true);
            addParticipantInGroup(temp, userId, false);
            myGroupDtos.add(MyGroupDto.toDto(temp, false));
        }
        MyGroupsResponse response = MyGroupsResponse.toResponseDto(true, myGroupDtos);

        doReturn(response).when(groupService).getMyGroups(userId, page);

        //when
        String url = "/api/groups";
        ResultActions resultActions = mvc.perform(get(url)
                .param("page", "0"));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(GET_MY_GROUPS.getCode()))
                .andExpect(jsonPath("$.status.message").value(GET_MY_GROUPS.getMessage()))
                .andExpect(jsonPath("$.content.hasNext").value(true))
                .andExpect(jsonPath("$.content.groupList[0].groupId").value(1L))
                .andExpect(jsonPath("$.content.groupList[0].isAdmin").value(true))
                .andExpect(jsonPath("$.content.groupList[1].isAdmin").value(false))
                ;
    }

    private Participant addParticipantInGroup(Group group, long userId, boolean isAdmin) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        return Participant.create(user, group, "닉네임" + userId, isAdmin);
    }

    @WithMockCustomUser
    @DisplayName("관리자 변경 / 성공")
    @Test
    void modify_admin() throws Exception {
        //given
        String nickname = "변경닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/admin", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(MODIFY_GROUP_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(MODIFY_GROUP_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("관리자 변경 / 관리자가 아닌 유저의 요청인 경우")
    @Test
    void modify_admin_not_admin_user() throws Exception {
        //given
        String nickname = "변경닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

        CustomException e = new CustomException(NONE_ADMIN);
        doThrow(e).when(groupService).modifyAdmin(userId, groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/admin", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("관리자 변경 / 해당 유저의 참가 기록이 없는 경우")
    @Test
    void modify_admin_no_participant_data() throws Exception {
        //given
        String nickname = "변경닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(groupService).modifyAdmin(userId, groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/admin", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    //--- Private Method ---

    private ModifyGroupRequest makeUpdateRequest(String title, String type, String color) {
        return ModifyGroupRequest.builder()
                .title(title)
                .type(type)
                .coverColor(color)
                .build();
    }

    private GetGroupResponse makeGetGroupResponse() {
        return GetGroupResponse.builder()
                .id(groupId)
                .isAdmin(false)
                .isInto(true)
                .build();
    }

    private CreateGroupRequest makeCreateRequest(String title, String nickname, String groupType, String color) {
        return CreateGroupRequest.builder()
                .title(title)
                .nickname(nickname)
                .groupType(groupType)
                .coverColor(color)
                .build();
    }
}