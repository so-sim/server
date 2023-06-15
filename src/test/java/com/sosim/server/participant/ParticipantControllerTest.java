package com.sosim.server.participant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.participant.dto.request.CreateParticipantRequest;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.security.WithMockCustomUser;
import com.sosim.server.security.WithMockCustomUserSecurityContextFactory;
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

import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ParticipantController.class})
class ParticipantControllerTest {

    static final String URI_PREFIX = "/api/group";
    private Long userId = WithMockCustomUserSecurityContextFactory.USER_ID;
    private long groupId = 1L;

    @MockBean
    @Autowired
    ParticipantService participantService;

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
    @DisplayName("모임 참가자 리스트 조회 / 성공")
    @Test
    void get_participants() throws Exception {
        //given
        String adminNickname = "총무닉네임";
        GetParticipantListResponse response = makeGetParticipantsResponse(adminNickname);

        doReturn(response).when(participantService).getGroupParticipants(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participants", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(GET_PARTICIPANTS.getCode()))
                .andExpect(jsonPath("$.content.adminNickname").value(adminNickname))
                .andExpect(jsonPath("$.content.nicknameList[0]").value("유저1"));

        verify(participantService, times(1)).getGroupParticipants(userId, groupId);
    }

    @WithMockCustomUser
    @DisplayName("모임 참가자 리스트 조회 / 그룹이 없는 경우")
    @Test
    void get_participants_no_group() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_GROUP);
        doThrow(e).when(participantService).getGroupParticipants(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participants", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("모임 참가자 리스트 조회 / 요청한 유저의 Participant 정보가 없는 경우")
    @Test
    void get_participants_no_participant() throws Exception {
        //given
        CustomException e = new CustomException(NONE_PARTICIPANT);
        doThrow(e).when(participantService).getGroupParticipants(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participants", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NONE_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 성공")
    @Test
    void create_participant() throws Exception {
        //given
        String nickname = "닉네임";
        CreateParticipantRequest request = new CreateParticipantRequest(nickname);

        doNothing().when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(INTO_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(INTO_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 요청한 유저가 없는 경우 NOT_FOUND_USER")
    @Test
    void create_participant_no_user() throws Exception {
        //given
        String nickname = "닉네임";
        CreateParticipantRequest request = new CreateParticipantRequest(nickname);

        CustomException e = new CustomException(NOT_FOUND_USER);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_USER.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_USER.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 모임이 없는 경우 NOT_FOUND_GROUP")
    @Test
    void create_participant_no_group() throws Exception {
        //given
        String nickname = "닉네임";
        CreateParticipantRequest request = new CreateParticipantRequest(nickname);

        CustomException e = new CustomException(NOT_FOUND_GROUP);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 이미 참가한 경우 ALREADY_INTO_GROUP")
    @Test
    void create_participant_already_into() throws Exception {
        //given
        String nickname = "닉네임";
        CreateParticipantRequest request = new CreateParticipantRequest(nickname);

        CustomException e = new CustomException(ALREADY_INTO_GROUP);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(ALREADY_INTO_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(ALREADY_INTO_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 중복된 Nickname이 있는 경우 ALREADY_USE_NICKNAME")
    @Test
    void create_participant_duplicate_nickname() throws Exception {
        //given
        String nickname = "닉네임";
        CreateParticipantRequest request = new CreateParticipantRequest(nickname);

        CustomException e = new CustomException(ALREADY_USE_NICKNAME);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(ALREADY_USE_NICKNAME.getCode()))
                .andExpect(jsonPath("$.status.message").value(ALREADY_USE_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private static GetParticipantListResponse makeGetParticipantsResponse(String adminNickname) {
        return GetParticipantListResponse.builder()
                .adminNickname(adminNickname)
                .nicknameList(List.of("유저1", "유저2", "유저3"))
                .build();
    }

}