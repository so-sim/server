package com.sosim.server.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.*;
import com.sosim.server.group.Group;
import com.sosim.server.group.dto.response.MyGroupDto;
import com.sosim.server.group.dto.response.MyGroupsResponse;
import com.sosim.server.security.WithMockCustomUser;
import com.sosim.server.security.WithMockCustomUserSecurityContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "납부 전");
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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "납부 전");
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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "납부 전");
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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, "기타", "메모", "납부 전");
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
        CreateEventRequest overAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), -1, "기타", "메모", "납부 전");
        CreateEventRequest underAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1_000_001, "기타", "메모", "납부 전");

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, null, "메모", "납부 전");

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
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "납부 전");
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
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "납부 전");
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
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "납부 전");
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
        ModifyEventRequest request = makeModifyRequest("닉네임", 1000, "기타", "메모", "납부 전");
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
        CreateEventRequest overAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), -1, "기타", "메모", "납부 전");
        CreateEventRequest underAmount = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1_000_001, "기타", "메모", "납부 전");

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
        CreateEventRequest request = makeCreateRequest(groupId, "닉네임", LocalDate.now(), 1000, null, "메모", "납부 전");

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

    @WithMockCustomUser
    @DisplayName("상세 내역 삭제 / 성공")
    @Test
    void delete_event() throws Exception {
        // given

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(delete(url));

        // then
        resultActions.andExpect(status().is(DELETE_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(DELETE_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(DELETE_EVENT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());

        verify(eventService, times(1)).deleteEvent(userId, eventId);
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 삭제 / 상세 내역이 없는 경우")
    @Test
    void delete_event_not_found_event() throws Exception {
        // given
        CustomException e = new CustomException(NOT_FOUND_EVENT);
        doThrow(e).when(eventService).deleteEvent(userId, eventId);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(delete(url));

        // then
        resultActions.andExpect(status().is(NOT_FOUND_EVENT.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_EVENT.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_EVENT.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 삭제 / 총무 권한이 없는 경우")
    @Test
    void delete_event_none_admin() throws Exception {
        // given
        CustomException e = new CustomException(NONE_ADMIN);
        doThrow(e).when(eventService).deleteEvent(userId, eventId);

        // when
        String url = URI_PREFIX.concat(String.format("/%d", eventId));
        ResultActions resultActions = mvc.perform(delete(url));

        // then
        resultActions.andExpect(status().is(NONE_ADMIN.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 납부여부 변경 / 성공")
    @Test
    void modify_event_situation() throws Exception {
        // given
        String situation = "확인중";
        ModifySituationRequest request = makeModifySituationRequest(situation);
        ModifySituationResponse response = makeModifySituationResponse(situation);
        doReturn(response).when(eventService).modifyEventSituation(userId, request);

        // when
        ResultActions resultActions = mvc.perform(patch(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, null, request.getSituation())));

        // then
        resultActions.andExpect(status().is(MODIFY_EVENT_SITUATION.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(MODIFY_EVENT_SITUATION.getCode()))
                .andExpect(jsonPath("$.status.message").value(MODIFY_EVENT_SITUATION.getMessage()))
                .andExpect(jsonPath("$.content.situation").value(situation));

        verify(eventService, times(1)).modifyEventSituation(userId, request);
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 납부여부 변경 / 완납 변경 시 총무 권한 없는 경우")
    @Test
    void modify_event_situation_none_admin() throws Exception {
        // given
        String situation = "완납";
        ModifySituationRequest request = makeModifySituationRequest(situation);
        CustomException e = new CustomException(NONE_ADMIN);
        doThrow(e).when(eventService).modifyEventSituation(userId, request);

        // when
        ResultActions resultActions = mvc.perform(patch(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, null, request.getSituation())));

        // then
        resultActions.andExpect(status().is(NONE_ADMIN.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(NONE_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(NONE_ADMIN.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 납부여부 변경 / 확인중 변경 시 이미 완납인 경우")
    @Test
    void modify_event_situation_fail_to_check() throws Exception {
        // given
        String situation = "확인중";
        ModifySituationRequest request = makeModifySituationRequest(situation);
        CustomException e = new CustomException(FAIL_TO_CHECK);
        doThrow(e).when(eventService).modifyEventSituation(userId, request);

        // when
        ResultActions resultActions = mvc.perform(patch(URI_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertGroundAndSituation(request, null, request.getSituation())));

        // then
        resultActions.andExpect(status().is(FAIL_TO_CHECK.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(FAIL_TO_CHECK.getCode()))
                .andExpect(jsonPath("$.status.message").value(FAIL_TO_CHECK.getMessage()))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 캘린더 조회 / 성공")
    @Test
    void get_event_calendar() throws Exception {
        // given
        FilterEventRequest request = makeFilterEventRequest(LocalDate.now(), LocalDate.now());
        GetEventCalendarResponse response = GetEventCalendarResponse.toDto(new ArrayList<>());

        doReturn(response).when(eventService).getEventCalendar(request);

        // when
        String url = "/api/event/penalty/calendar";
        ResultActions resultActions = mvc.perform(get(url)
                .param("groupId", String.valueOf(groupId))
                .param("startDate", "2023.01.01")
                .param("endDate", "2023.01.01")
        );

        // then
        resultActions.andExpect(status().is(GET_EVENT_CALENDAR.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(GET_EVENT_CALENDAR.getCode()))
                .andExpect(jsonPath("$.status.message").value(GET_EVENT_CALENDAR.getMessage()));
    }

    @WithMockCustomUser
    @DisplayName("상세 내역 필터링 / 성공")
    @Test
    void get_event_list_filter() throws Exception {
        //given
        FilterEventRequest request = makeFilterEventRequest(LocalDate.now(), LocalDate.now());
        GetEventListResponse response = GetEventListResponse.toDto(new ArrayList<>(), 0);

        doReturn(response).when(eventService).getEvents(request, PageRequest.of(0, 15));

        //when
        String url = "/api/event/penalties";
        ResultActions resultActions = mvc.perform(get(url)
                .param("page", "0")
                .param("size", "15")
                .param("groupId", String.valueOf(groupId))
                .param("startDate", "2023.01.01")
                .param("endDate", "2023.01.01")
        );

        //then
        resultActions.andExpect(status().is(GET_EVENTS.getHttpStatus().value()))
                .andExpect(jsonPath("$.status.code").value(GET_EVENTS.getCode()))
                .andExpect(jsonPath("$.status.message").value(GET_EVENTS.getMessage()));
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

    private ModifySituationRequest makeModifySituationRequest(String situation) {
        return ModifySituationRequest.builder()
                .situation(Situation.getSituation(situation))
                .build();
    }

    private ModifySituationResponse makeModifySituationResponse(String situation) {
        return ModifySituationResponse.builder()
                .situation(situation)
                .build();
    }

    private FilterEventRequest makeFilterEventRequest(LocalDate startDate, LocalDate endDate) {
        return FilterEventRequest.builder()
                .groupId(groupId)
                .startDate(startDate)
                .endDate(endDate)
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
