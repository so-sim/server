package com.sosim.server.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
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

import java.time.LocalDate;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EventController.class})
public class EventControllerTest {

    static final String URI_PREFIX = "/api/event/penalty";
    private Long userId = WithMockCustomUserSecurityContextFactory.USER_ID;
    private long groupId = 1L;
    private long eventId = 1L;

    @MockBean
    EventService eventService;

    MockMvc mvc;

    @Autowired
    WebApplicationContext was;

    @Autowired
    ObjectMapper om;

    @BeforeEach
    public void init() {
        mvc = MockMvcBuilders.webAppContextSetup(was).build();
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / 성공")
    @Test
    void create_event() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "사유", "메모", "미납");
        EventIdResponse response = EventIdResponse.builder().eventId(eventId).build();

        doReturn(response).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().is(CREATE_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(CREATE_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(CREATE_EVENT.getMessage()))
                .andExpect(jsonPath("$.content.eventId").value(eventId));

        verify(eventService, times(1)).createEvent(userId, request);
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / 모임이 없는 경우")
    @Test
    void create_event_not_found_group() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "사유", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_GROUP);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_GROUP.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_GROUP.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_GROUP.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());

    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / 참가자 정보가 없는 경우")
    @Test
    void create_event_not_found_participant() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "사유", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_PARTICIPANT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());

    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / 총무가 아닌 경우")
    @Test
    void create_event_none_admin() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "사유", "메모", "미납");
        CustomException e = new CustomException(NONE_ADMIN);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().is(NONE_ADMIN.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());

    }

    private CreateEventRequest makeCreateRequest(long groupId, String nickname, LocalDate date, int amount, String ground, String memo, String situation) {
        return CreateEventRequest.builder()
                .groupId(groupId)
                .nickname(nickname)
                .date(date)
                .amount(amount)
                .ground(ground)
                .memo(memo)
                .situation(situation)
                .build();
    }
}
