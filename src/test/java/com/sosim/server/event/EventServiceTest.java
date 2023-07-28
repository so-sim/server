package com.sosim.server.event;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.dto.request.CreateEventRequest;
import com.sosim.server.event.dto.response.EventIdResponse;
import com.sosim.server.event.dto.response.GetEventResponse;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private CreateEventRequest makeCreateEventRequest(String nickname) {
        return CreateEventRequest.builder()
                .groupId(groupId)
                .nickname(nickname)
                .build();
    }
}
