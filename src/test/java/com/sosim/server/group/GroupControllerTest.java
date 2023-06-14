package com.sosim.server.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.security.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GroupController.class})
class GroupControllerTest {

    static final String URI_PREFIX = "/api/group";
    private Long userId = 1L;
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
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private GetGroupResponse makeGetGroupResponse() {
        return GetGroupResponse.builder()
                .id(groupId)
                .isAdmin(false)
                .isInto(true)
                .build();
    }

    //--- Private Method ---
    private static CreateGroupRequest makeCreateRequest(String title, String nickname, String groupType, String color) {
        return CreateGroupRequest.builder()
                .title(title)
                .nickname(nickname)
                .groupType(groupType)
                .coverColor(color)
                .build();
    }
}