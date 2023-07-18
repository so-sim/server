package com.sosim.server.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "미납");
        EventIdResponse response = EventIdResponse.builder().eventId(eventId).build();

        doReturn(response).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_GROUP);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NONE_ADMIN);

        doThrow(e).when(eventService).createEvent(userId, request);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(NONE_ADMIN.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / amount 유효성 검사")
    @Test
    void create_event_valid_amount() throws Exception {
        // given
        CreateEventRequest overAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), -1, "기타", "메모", "미납");
        CreateEventRequest underAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1_000_001, "기타", "메모", "미납");

        // when
        ResultActions overResult = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(overAmount, overAmount.getGround(), overAmount.getSituation())));
        ResultActions underResult = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(underAmount, underAmount.getGround(), underAmount.getSituation())));

        // then
        overResult.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("amount"));
        underResult.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("amount"));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / ground 유효성 검사")
    @Test
    void create_event_valid_ground() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, null, "메모", "미납");

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("ground"));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 생성 / situation 유효성 검사")
    @Test
    void create_event_valid_situation() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", null);

        // when
        ResultActions resultActions = mvc.perform(post(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("situation"));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 조회 / 성공")
    @Test
    void get_event() throws Exception {
        // given
        GetEventResponse getEventResponse = makeGetEventResponse();
        doReturn(getEventResponse).when(eventService).getEvent(eventId);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(get(url));

        // then
        resultActions.andExpect(status().is(GET_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(GET_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(GET_EVENT.getMessage()))
                .andExpect(jsonPath("$.content.eventId").value(eventId));

        verify(eventService, times(1)).getEvent(eventId);
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 조회 / 상세 내역이 없는 경우")
    @Test
    void get_event_not_found_event() throws Exception {
        // given
        CustomException e = new CustomException(NOT_FOUND_EVENT);
        doThrow(e).when(eventService).getEvent(eventId);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(get(url));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_EVENT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 수정 / 성공")
    @Test
    void modify_event() throws Exception {
        // given
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "미납");
        GetEventResponse response = makeGetEventResponse();
        doReturn(response).when(eventService).modifyEvent(userId, eventId, request);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(MODIFY_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(MODIFY_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(MODIFY_EVENT.getMessage()))
                .andExpect(jsonPath("$.content.eventId").value(eventId));

        verify(eventService, times(1)).modifyEvent(userId, eventId, request);
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 수정 / 상세 내역이 없는 경우")
    @Test
    void modify_event_not_found_event() throws Exception {
        // given
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_EVENT);
        doThrow(e).when(eventService).modifyEvent(userId, eventId, request);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_EVENT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 수정 / 총무 권한이 없는 경우")
    @Test
    void modify_event_none_admin() throws Exception {
        // given
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NONE_ADMIN);
        doThrow(e).when(eventService).modifyEvent(userId, eventId, request);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(NONE_ADMIN.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 단건 수정 / 팀원 변경 시, 해당 팀원이 없는 경우")
    @Test
    void modify_event_not_found_participant() throws Exception {
        // given
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "미납");
        CustomException e = new CustomException(NOT_FOUND_PARTICIPANT);
        doThrow(e).when(eventService).modifyEvent(userId, eventId, request);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_PARTICIPANT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_PARTICIPANT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_PARTICIPANT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 수정 / amount 유효성 검사")
    @Test
    void modify_event_valid_amount() throws Exception {
        // given
        CreateEventRequest overAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), -1, "기타", "메모", "미납");
        CreateEventRequest underAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1_000_001, "기타", "메모", "미납");

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions overResult = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(overAmount, overAmount.getGround(), overAmount.getSituation())));
        ResultActions underResult = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(underAmount, underAmount.getGround(), underAmount.getSituation())));

        // then
        overResult.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("amount"));
        underResult.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("amount"));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 수정 / ground 유효성 검사")
    @Test
    void modify_event_valid_ground() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, null, "메모", "미납");

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("ground"));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 수정 / situation 유효성 검사")
    @Test
    void modify_event_valid_situation() throws Exception {
        // given
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", null);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, request.getGround(), request.getSituation())));

        // then
        resultActions.andExpect(status().is(BINDING_ERROR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(BINDING_ERROR.getCode()))
                .andExpect(jsonPath("$.status.message").value(BINDING_ERROR.getMessage()))
                .andExpect(jsonPath("$.content.field").value("situation"));
    }

    private CreateEventRequest makeCreateRequest(long groupId, String nickname, LocalDate date, int amount, String ground, String memo, String situation) {
        return CreateEventRequest.builder()
                .groupId(groupId)
                .nickname(nickname)
                .date(date)
                .amount(amount)
                .ground(Ground.getGround(ground))
                .memo(memo)
                .situation(Situation.getSituation(situation))
                .build();
    }

    private GetEventResponse makeGetEventResponse() {
        return GetEventResponse.builder()
                .eventId(eventId)
                .build();
    }

    private ModifyEventRequest makeModifyRequest(String nickname, int amount, String ground, String memo, String situation) {
        return ModifyEventRequest.builder()
                .nickname(nickname)
                .amount(amount)
                .ground(Ground.getGround(ground))
                .memo(memo)
                .situation(Situation.getSituation(situation))
                .build();
    }

    private String convertGroundAndSituation(Object request, Ground ground, Situation situation) throws Exception {
        String requestBody = om.writeValueAsString(request);
        if (ground != null) {
            requestBody = requestBody.replace(ground.name(), ground.getComment());
        }
        if (situation != null) {
            requestBody = requestBody.replace(situation.name(), situation.getComment());
        }
        return requestBody;
    }
}
