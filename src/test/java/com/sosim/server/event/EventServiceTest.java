package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.event.dto.request.ModifyEventRequest;
import com.sosim.server.event.dto.request.ModifySituationRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventCalendarResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.event.dto.response.ModifySituationResponse;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.notification.util.NotificationUtil;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    private long userId = 1L;
    private long eventId = 1L;
    private long groupId = 1L;

    @InjectMocks
    EventService eventService;

    @Mock
    EventRepository eventRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    NotificationUtil notificationUtil;

    @DisplayName("상세 내역 생성 / 성공")
    @Test
    void create_event() {
        //given
        String nickname = "닉네임";
        CreateEventRequest request = makeCreateEventRequest(nickname);

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
        Event event = Event.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);

        Participant participant = group.createParticipant(user, nickname, true);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant)).when(participantRepository).findByNicknameAndGroupId(nickname, groupId);
        doReturn(event).when(eventRepository).save(any(Event.class));

        //when
        EventIdResponse response = eventService.createEvent(userId, request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId);

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @DisplayName("상세 내역 생성 / 모임이 없는 경우")
    @Test
    void create_event_not_found_group() {
        //given
        CreateEventRequest request = makeCreateEventRequest("닉네임");

        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.createEvent(userId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);

        verify(eventRepository, times(0)).save(any(Event.class));
    }

    @DisplayName("상세 내역 생성 / 참가자 정보가 없는 경우")
    @Test
    void create_event_not_found_participant() {
        //given
        String nickname = "닉네임";
        CreateEventRequest request = makeCreateEventRequest(nickname);

        Group group = Group.builder().build();

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.empty()).when(participantRepository).findByNicknameAndGroupId(nickname, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.createEvent(userId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);

        verify(eventRepository, times(0)).save(any(Event.class));
    }

    @DisplayName("상세 내역 생성 / 총무가 아닌 경우")
    @Test
    void create_event_none_admin() {
        //given
        String nickname = "닉네임";
        CreateEventRequest request = makeCreateEventRequest(nickname);

        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Participant participant = group.createParticipant(user, nickname, true);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant)).when(participantRepository).findByNicknameAndGroupId(nickname, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.createEvent(userId + 1, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ADMIN);

        verify(eventRepository, times(0)).save(any(Event.class));
    }

    @DisplayName("상세 내역 단건 조회 / 성공")
    @Test
    void get_event() {
        //given
        Event event = Event.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "situation", Situation.NONE);
        ReflectionTestUtils.setField(event, "ground", Ground.ETC);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);

        //when
        GetEventResponse response = eventService.getEvent(eventId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getGround()).isEqualTo(Ground.ETC.getComment());
        assertThat(response.getSituation()).isEqualTo(Situation.NONE.getComment());
    }

    @DisplayName("상세 내역 단건 조회 / Event가 없을 경우")
    @Test
    void get_event_not_found_event() {
        //given
        doReturn(Optional.empty()).when(eventRepository).findByIdWithGroup(eventId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.getEvent(eventId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_EVENT);
    }

    @DisplayName("상세 내역 단건 수정 / 성공")
    @Test
    void modify_event() {
        //given
        String nickname = "닉네임";
        ModifyEventRequest request = makeModifyEventRequest(nickname);

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(event, "nickname", nickname);
        ReflectionTestUtils.setField(event, "situation", Situation.NONE);
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(user, "id", userId);

        Participant participant = group.createParticipant(user, nickname, true);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);
        doReturn(Optional.of(participant)).when(participantRepository).findByNicknameAndGroupId(nickname, groupId);

        //when
        GetEventResponse response = eventService.modifyEvent(userId, eventId, request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getGround()).isEqualTo(Ground.ETC.getComment());
        assertThat(response.getSituation()).isEqualTo(Situation.NONE.getComment());
    }

    @DisplayName("상세 내역 단건 수정 / Event가 없을 경우")
    @Test
    void modify_event_not_found_event() {
        //given
        ModifyEventRequest request = ModifyEventRequest.builder().build();
        doReturn(Optional.empty()).when(eventRepository).findByIdWithGroup(eventId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.modifyEvent(userId, eventId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_EVENT);
    }

    @DisplayName("상세 내역 단건 수정 / 총무가 아닐 경우")
    @Test
    void modify_event_none_admin() {
        //given
        String nickname = "닉네임";
        ModifyEventRequest request = makeModifyEventRequest(nickname);

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(group, "id", groupId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, nickname, true);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.modifyEvent(userId + 1, eventId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ADMIN);
    }

    @DisplayName("상세 내역 단건 수정 / 변경할 팀원이 없는 경우")
    @Test
    void modify_event_not_found_participant() {
        //given
        String nickname = "닉네임";
        ModifyEventRequest request = makeModifyEventRequest(nickname + "2");

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(group, "id", groupId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, nickname, true);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);
        doReturn(Optional.empty()).when(participantRepository).findByNicknameAndGroupId(nickname + "2", groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () -> eventService.modifyEvent(userId, eventId, request));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);
    }

    @DisplayName("상세 내역 삭제 / 성공")
    @Test
    void delete_event() {
        // given
        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, "닉네임", true);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);

        // when
        eventService.deleteEvent(userId, eventId);

        // then
        assertThat(event.getStatus()).isEqualTo(Status.DELETED);
    }

    @DisplayName("상세 내역 삭제 / Event가 없는 경우")
    @Test
    void delete_event_not_found_event() {
        // given
        doReturn(Optional.empty()).when(eventRepository).findByIdWithGroup(eventId);

        // when
        CustomException e = assertThrows(CustomException.class, () -> eventService.deleteEvent(userId, eventId));

        // then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_EVENT);
    }

    @DisplayName("상세 내역 삭제 / 총무가 아닌 경우")
    @Test
    void delete_event_none_admin() {
        // given
        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, "닉네임", true);

        doReturn(Optional.of(event)).when(eventRepository).findByIdWithGroup(eventId);

        // when
        CustomException e = assertThrows(CustomException.class, () -> eventService.deleteEvent(userId + 1, eventId));

        // then
        assertThat(e.getResponseCode()).isEqualTo(NONE_ADMIN);
    }

    @DisplayName("상세 내역 납부 여부 N개 변경 / 성공")
    @Test
    void modify_event_situation() {
        // given
        ModifySituationRequest request = makeModifySituationRequest(Situation.NONE);

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, "닉네임", true);

        doReturn(List.of(event)).when(eventRepository).findAllById(request.getEventIdList());
        doNothing().when(eventRepository).updateSituationAll(request.getEventIdList(), request.getSituation());

        // when
        ModifySituationResponse response = eventService.modifyEventSituation(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSituation()).isEqualTo(request.getSituation().getComment());
        assertThat(response.getEventIdList()).isEqualTo(request.getEventIdList());
    }

    @DisplayName("상세 내역 납부 여부 N개 변경 / 총무가 확인 중으로 변경할 경우")
    @Test
    void modify_event_situation_admin_to_check() {
        // given
        ModifySituationRequest request = makeModifySituationRequest(Situation.CHECK);

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, "닉네임", true);

        doReturn(List.of(event)).when(eventRepository).findAllById(request.getEventIdList());

        // when
        CustomException e = assertThrows(CustomException.class, () -> eventService.modifyEventSituation(userId, request));

        // then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FULL_OR_NON_SITUATION);
    }

    @DisplayName("상세 내역 납부 여부 N개 변경 / 팀원이 미납 or 완납 으로 변경할 경우")
    @Test
    void modify_event_situation_none_admin() {
        // given
        ModifySituationRequest request = makeModifySituationRequest(Situation.FULL);

        Event event = Event.builder().build();
        Group group = Group.builder().build();
        User user = User.builder().build();
        ReflectionTestUtils.setField(event, "group", group);
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(user, "id", userId);

        group.createParticipant(user, "닉네임", true);

        doReturn(List.of(event)).when(eventRepository).findAllById(request.getEventIdList());

        // when
        CustomException e = assertThrows(CustomException.class, () -> eventService.modifyEventSituation(userId + 1, request));

        // then
        assertThat(e.getResponseCode()).isEqualTo(NOT_CHECK_SITUATION);
    }

    @DisplayName("상세 내역 캘린더 조회 / 성공")
    @Test
    void get_event_calendar() {
        // given
        FilterEventRequest request = makeFilterEventRequest(null, null);

        Event event = Event.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "date", LocalDate.now());
        ReflectionTestUtils.setField(event, "situation", Situation.NONE);

        doReturn(List.of(event)).when(eventRepository).searchAll(request);

        // when
        GetEventCalendarResponse response = eventService.getEventCalendar(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusOfDay()).isNotEmpty();
    }

    @DisplayName("상세 내역 리스트 조회 / 성공")
    @Test
    void get_event_list() {
        // given
        FilterEventRequest request = makeFilterEventRequest("닉네임", Situation.NONE);
        Pageable pageable = PageRequest.of(0, 15);

        Event event = Event.builder().build();
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "date", LocalDate.now());
        ReflectionTestUtils.setField(event, "situation", Situation.NONE);

        doReturn(List.of(event)).when(eventRepository).searchAll(request);

        // when
        GetEventCalendarResponse response = eventService.getEventCalendar(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusOfDay()).isNotEmpty();
    }

    private CreateEventRequest makeCreateEventRequest(String nickname) {
        return CreateEventRequest.builder()
                .groupId(groupId)
                .nickname(nickname)
                .build();
    }

    private ModifyEventRequest makeModifyEventRequest(String nickname) {
        return ModifyEventRequest.builder()
                .nickname(nickname)
                .ground(Ground.ETC)
                .situation(Situation.NONE)
                .build();
    }

    private ModifySituationRequest makeModifySituationRequest(Situation situation) {
        return ModifySituationRequest.builder()
                .eventIdList(List.of(eventId))
                .situation(situation)
                .build();
    }

    private FilterEventRequest makeFilterEventRequest(String nickname, Situation situation) {
        FilterEventRequest request = new FilterEventRequest();
        request.setGroupId(groupId);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());
        request.setNickname(nickname);
        request.setSituation(situation);
        return request;
    }
}
