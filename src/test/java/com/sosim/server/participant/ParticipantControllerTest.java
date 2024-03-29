package com.sosim.server.participant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.participant.controller.ParticipantController;
import com.sosim.server.participant.dto.NicknameDto;
import com.sosim.server.participant.dto.NicknameSearchRequest;
import com.sosim.server.participant.dto.NicknameSearchResponse;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.participant.service.ParticipantService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("모임 참가자 리스트 조회 / 요청한 유저의 Participant 정보가 없는 경우")
    @Test
    void get_participants_no_participant() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(participantService).getGroupParticipants(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participants", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 가입 / 성공")
    @Test
    void create_participant() throws Exception {
        //given
        String nickname = "닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

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
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

        CustomException e = new CustomException(NOT_FOUND_USER);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
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
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

        CustomException e = new CustomException(NOT_FOUND_GROUP);
        doThrow(e).when(participantService).createParticipant(userId, groupId, nickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
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
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

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
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(nickname);

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

    @WithMockCustomUser
    @DisplayName("참가자 탈퇴 / 성공")
    @Test
    void delete_participant() throws Exception {
        //given
        doNothing().when(participantService).deleteParticipant(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(WITHDRAW_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(WITHDRAW_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 탈퇴 / 모임이 없는 경우 400 / NOT_FOUND_GROUP")
    @Test
    void delete_participant_no_group() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_GROUP);
        doThrow(e).when(participantService).deleteParticipant(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 탈퇴 / 참가자가 없는 경우 404 / NONE_PARTICIPANT")
    @Test
    void delete_participant_no_participant() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(participantService).deleteParticipant(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(delete(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 닉네임 변경 / 성공")
    @Test
    void modify_participant() throws Exception {
        //given
        String newNickname = "새닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(newNickname);
        doNothing().when(participantService).modifyNickname(userId, groupId, newNickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(MODIFY_NICKNAME.getCode()))
                .andExpect(jsonPath("$.status.message").value(MODIFY_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 닉네임 변경 / 그룹이 없는 경우 NOT_FOUND_GROUP")
    @Test
    void modify_participant_no_group() throws Exception {
        //given
        String newNickname = "새닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(newNickname);

        CustomException e = new CustomException(NOT_FOUND_GROUP);
        doThrow(e).when(participantService).modifyNickname(userId, groupId, newNickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
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
    @DisplayName("참가자 닉네임 변경 / 참가자가 없는 경우 NONE_PARTICIPANT")
    @Test
    void modify_participant_no_participant() throws Exception {
        //given
        String newNickname = "새닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(newNickname);

        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(participantService).modifyNickname(userId, groupId, newNickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("참가자 닉네임 변경 / 중복된 닉네임인 경우 ALREADY_USE_NICKNAME")
    @Test
    void modify_participant_already_used_nickname() throws Exception {
        //given
        String newNickname = "새닉네임";
        ParticipantNicknameRequest request = new ParticipantNicknameRequest(newNickname);

        CustomException e = new CustomException(ALREADY_USE_NICKNAME);
        doThrow(e).when(participantService).modifyNickname(userId, groupId, newNickname);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(ALREADY_USE_NICKNAME.getCode()))
                .andExpect(jsonPath("$.status.message").value(ALREADY_USE_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("내 닉네임 조회 / 성공")
    @Test
    void get_my_nickname() throws Exception {
        //given
        String nickname = "닉네임";
        GetNicknameResponse response = new GetNicknameResponse(nickname);

        doReturn(response).when(participantService).getMyNickname(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(GET_NICKNAME.getCode()))
                .andExpect(jsonPath("$.status.message").value(GET_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.content.nickname").value(nickname));
    }

    @WithMockCustomUser
    @DisplayName("내 닉네임 조회 / 그룹 or 참가자가 없는 경우 NONE_PARTICIPANT")
    @Test
    void get_my_nickname_no_participant_or_group() throws Exception {
        //given
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(participantService).getMyNickname(userId, groupId);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participant", groupId));
        ResultActions resultActions = mvc.perform(get(url));

        //then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @DisplayName("참가자 검색 / 정상")
    @Test
    void search_participant() throws Exception {
        //given
        NicknameSearchRequest request = new NicknameSearchRequest();
        String keyword = "닉네임";
        request.setKeyword(keyword);

        List<NicknameDto> list = List.of(new NicknameDto("닉네임1", false), new NicknameDto("닉네임2", false));
        NicknameSearchResponse response = new NicknameSearchResponse(list);

        doReturn(response).when(participantService).searchParticipants(groupId, request);

        //when
        String url = URI_PREFIX.concat(String.format("/%d/participants-nickname", groupId));
        ResultActions resultActions = mvc.perform(get(url)
                .param("keyword", keyword));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(SEARCH_PARTICIPANTS.getCode()))
                .andExpect(jsonPath("$.status.message").value(SEARCH_PARTICIPANTS.getMessage()))
                .andExpect(jsonPath("$.content.nicknameList").isArray())
                .andExpect(jsonPath("$.content.nicknameList[0].nickname").value("닉네임1"))
                .andExpect(jsonPath("$.content.nicknameList[0].withdraw").value(false))
                .andExpect(jsonPath("$.content.nicknameList[1].nickname").value("닉네임2"))
                .andExpect(jsonPath("$.content.nicknameList[1].withdraw").value(false));
    }

    private static GetParticipantListResponse makeGetParticipantsResponse(String adminNickname) {
        return GetParticipantListResponse.builder()
                .adminNickname(adminNickname)
                .nicknameList(List.of("유저1", "유저2", "유저3"))
                .build();
    }

}