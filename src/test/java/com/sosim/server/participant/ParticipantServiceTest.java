package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.event.EventRepository;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.participant.dto.NicknameDto;
import com.sosim.server.participant.dto.NicknameSearchRequest;
import com.sosim.server.participant.dto.NicknameSearchResponse;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    private long userId = 1L;
    private long groupId = 1L;

    @InjectMocks
    ParticipantService participantService;

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    EventRepository eventRepository;

    @DisplayName("참가자 가입 / 정상")
    @Test
    void create_participant() {
        //given
        User user = makeUser();
        Group group = makeGroup();
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        participantService.createParticipant(userId, groupId, nickname);

        //then
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @DisplayName("참가자 가입 / 유저가 없는 경우 CustomException(NOT_FOUND_USER)")
    @Test
    void create_participant_no_user() {
        //given
        String nickname = "닉네임";

        doReturn(Optional.empty()).when(userRepository).findById(userId);

        //when
        CustomException exception = assertThrows(CustomException.class, () ->
                participantService.createParticipant(userId, groupId, nickname));

        //then
        assertThat(exception.getResponseCode()).isEqualTo(NOT_FOUND_USER);
    }

    @DisplayName("참가자 가입 / 모임이 없는 경우 CustomException(NOT_FOUND_GROUP)")
    @Test
    void create_participant_no_group() {
        //given
        User user = makeUser();
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.createParticipant(userId, groupId, nickname));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);
    }

    @DisplayName("참가자 가입 / 이미 가입한 경우 CustomException(ALREADY_INTO_GROUP)")
    @Test
    void create_participant_already_into() {
        //given
        User user = makeUser();
        Group group = makeGroup();
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId, false);
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException exception = assertThrows(CustomException.class, () ->
                participantService.createParticipant(userId, groupId, nickname));

        //then
        assertThat(exception.getResponseCode()).isEqualTo(ALREADY_INTO_GROUP);
    }

    @DisplayName("참가자 가입 / 중복된 Nickname인 경우 CustomException(ALREADY_USE_NICKNAME)")
    @Test
    void create_participant_duplicate_nickname() {
        //given
        User user = makeUser();
        Group group = makeGroup();
        String nickname = "닉네임" + (userId + 2);
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId + 2, false);

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException exception = assertThrows(CustomException.class, () ->
                participantService.createParticipant(userId, groupId, nickname));

        //then
        assertThat(exception.getResponseCode()).isEqualTo(ALREADY_USE_NICKNAME);
    }

    @DisplayName("모임 참가자 리스트 조회 / 응답 테스트")
    @Test
    void get_participants() {
        //given
        Group group = makeGroup();
        addParticipantInGroup(group, userId, true);
        addParticipantInGroup(group, userId + 1, false);
        addParticipantInGroup(group, userId + 2, false);
        addParticipantInGroup(group, userId + 3, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(userId, groupId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getAdminNickname()).isEqualTo("닉네임" + userId);
        assertThat(response.getNicknameList())
                .containsExactly("닉네임" + (userId + 1), "닉네임" + (userId + 2), "닉네임" + (userId + 3));
    }

    @DisplayName("모임 참가자 리스트 조회 / 총무가 아닌 유저가 nicknameList의 첫 번째 원소")
    @Test
    void get_participants_not_admin_request() {
        //given
        Group group = makeGroup();
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId + 2, false);
        addParticipantInGroup(group, userId, false);
        addParticipantInGroup(group, userId + 3, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(userId, groupId);

        //then
        assertThat(response.getAdminNickname()).isEqualTo("닉네임" + (userId + 1));
        assertThat(response.getNicknameList()).containsExactly("닉네임" + userId, "닉네임" + (userId + 2), "닉네임" + (userId + 3));
    }

    @DisplayName("모임 참가자 리스트 조회 / 요청 유저, Admin 제외하고 nickname 오름차순 정렬")
    @Test
    void get_participants_asc_nickname() {
        //given
        Group group = makeGroup();
        addParticipantInGroup(group, userId + 1, true);
        addParticipantInGroup(group, userId + 2, false);
        addParticipantInGroup(group, userId + 3, false);
        addParticipantInGroup(group, userId + 4, false);
        addParticipantInGroup(group, userId + 5, false);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(userId + 3, groupId);

        //then
        assertThat(response.getAdminNickname()).isEqualTo("닉네임" + (userId + 1));
        assertThat(response.getNicknameList())
                .containsExactly("닉네임" + (userId + 3),
                        "닉네임" + (userId + 2),
                        "닉네임" + (userId + 4),
                        "닉네임" + (userId + 5));
    }

    @DisplayName("모임 참가자 리스트 조회 / 그룹이 없는 경우 CustomException(NOT_FOUND_GROUP)")
    @Test
    void get_participants_no_group() {
        //given
        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.getGroupParticipants(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);
    }

    @DisplayName("모임 참가자 리스트 조회 / 총무가 없는 경우 CustomException(NOT_FOUND_PARTICIPANT)")
    @Test
    void get_participants_no_admin() {
        //given
        Group group = makeGroup();
        addParticipantInGroup(group, userId, false);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.getGroupParticipants(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);
    }


    @DisplayName("모임 탈퇴 / 성공")
    @Test
    void delete_participant() {
        //given
        Participant participant = makeParticipant(1L, userId, "닉네임");
        Group group = makeGroup();
        group.getParticipantList().add(participant);
        group.getParticipantList().add(makeParticipant(2L, userId + 1, "닉네임" + 1));

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant)).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        participantService.deleteParticipant(userId, groupId);

        //then
        assertThat(participant.isActive()).isFalse();
        assertThat(group.isActive()).isTrue();
    }

    @DisplayName("모임 탈퇴 / 참가자가 없는 경우 모임도 삭제")
    @Test
    void delete_participant_and_group() {
        //given
        Participant participant = makeParticipant(1L, userId, "닉네임");
        Group group = makeGroup();
        group.getParticipantList().add(participant);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant)).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        participantService.deleteParticipant(userId, groupId);

        //then
        assertThat(participant.isActive()).isFalse();
        assertThat(group.isActive()).isFalse();
    }


    @DisplayName("모임 탈퇴 / 모임이 없는 경우 CustomException(NOT_FOUND_GROUP)")
    @Test
    void delete_participant_no_group() {
        //given
        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.deleteParticipant(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);
    }

    @DisplayName("모임 탈퇴 / 참가자가 없는 경우 CustomException(NONE_PARTICIPANT)")
    @Test
    void delete_participant_no_participant() {
        //given
        Group group = makeGroup();

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.empty()).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.deleteParticipant(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);
    }

    @DisplayName("참가자 닉네임 변경 / 성공")
    @Test
    void modify_participant() {
        //given
        Participant participant = makeParticipant(1L, userId, "닉네임");
        Group group = makeGroup();
        group.getParticipantList().add(participant);

        String newNickname = "새닉네임";

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant)).when(participantRepository).findByUserIdAndGroupId(userId, groupId);
        doNothing().when(eventRepository).updateNicknameAll(any(), any());

        //when
        participantService.modifyNickname(userId, groupId, newNickname);

        //then
        assertThat(participant.getNickname()).isEqualTo(newNickname);
    }

    @DisplayName("참가자 닉네임 변경 / 그룹이 없는 경우 CustomException(NOT_FOUND_GROUP)")
    @Test
    void modify_participant_no_group() {
        //given
        String newNickname = "새닉네임";

        doReturn(Optional.empty()).when(groupRepository).findByIdWithParticipants(groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.modifyNickname(userId, groupId, newNickname));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_GROUP);
    }

    @DisplayName("참가자 닉네임 변경 / 참가자가 없는 경우 CustomException(NONE_PARTICIPANT)")
    @Test
    void modify_participant_no_participant() {
        //given
        String newNickname = "새닉네임";
        Group group = makeGroup();

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.empty()).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.modifyNickname(userId, groupId, newNickname));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);
    }

    @DisplayName("참가자 닉네임 변경 / 중복된 닉네임이 있는 경우 CustomException(ALREADY_USE_NICKNAME)")
    @Test
    void modify_participant_dup_nickname() {
        //given
        String newNickname = "새닉네임";
        Group group = makeGroup();
        Participant participant1 = makeParticipant(1L, userId, "닉네임");
        Participant participant2 = makeParticipant(2L, userId + 1, newNickname);
        group.getParticipantList().add(participant1);
        group.getParticipantList().add(participant2);

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(Optional.of(participant1)).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.modifyNickname(userId, groupId, newNickname));

        //then
        assertThat(e.getResponseCode()).isEqualTo(ALREADY_USE_NICKNAME);
    }

    @DisplayName("내 닉네임 조회 / 성공")
    @Test
    void get_my_nickname() {
        //given
        String nickname = "닉네임";
        Participant participant = makeParticipant(1L, userId, nickname);

        doReturn(Optional.of(participant)).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        GetNicknameResponse response = participantService.getMyNickname(userId, groupId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo(nickname);
    }

    @DisplayName("내 닉네임 조회 / 모임 or 참가자 없는 경우 CustomException(NONE_PARTICIPANT)")
    @Test
    void get_my_nickname_no_group_or_participant() {
        //given
        doReturn(Optional.empty()).when(participantRepository).findByUserIdAndGroupId(userId, groupId);

        //when
        CustomException e = assertThrows(CustomException.class, () ->
                participantService.getMyNickname(userId, groupId));

        //then
        assertThat(e.getResponseCode()).isEqualTo(NOT_FOUND_PARTICIPANT);
    }

    @DisplayName("참가자 검색 / 정상")
    @Test
    void search_participant() {
        //given
        Group group = makeGroup();
        String nicknamePrefix = "닉";
        NicknameSearchRequest request = new NicknameSearchRequest();
        request.setKeyword(nicknamePrefix);

        String nickname1 = "닉네임1";
        String nickname2 = "닉네임2";
        List<Participant> participants = List.of(
                makeParticipant(1L, userId, nickname1),
                makeParticipant(2L, userId + 1, nickname2));

        doReturn(Optional.of(group)).when(groupRepository)
                .findById(groupId);
        doReturn(participants).when(participantRepository)
                .findByGroupAndNicknameContainsIgnoreCase(group, nicknamePrefix);

        //when
        NicknameSearchResponse response = participantService.searchParticipants(groupId, request);

        //then
        List<NicknameDto> list = response.getNicknameList();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getNickname()).isEqualTo(nickname1);
        assertThat(list.get(1).getNickname()).isEqualTo(nickname2);
    }

    @DisplayName("참가자 검색 / 결과 없을 시 빈 배열 리턴")
    @Test
    void search_participant_no_result() {
        //given
        Group group = makeGroup();
        String nicknamePrefix = "닉";
        NicknameSearchRequest request = new NicknameSearchRequest();
        request.setKeyword(nicknamePrefix);

        List<Participant> participants = new ArrayList<>();
        doReturn(Optional.of(group)).when(groupRepository)
                .findById(groupId);
        doReturn(participants).when(participantRepository)
                .findByGroupAndNicknameContainsIgnoreCase(group, nicknamePrefix);

        //when
        NicknameSearchResponse response = participantService.searchParticipants(groupId, request);

        //then
        List<NicknameDto> list = response.getNicknameList();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(0);
    }

    private Group makeGroup() {
        Group group = Group.builder().build();
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private User makeUser() {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Participant makeParticipant(long id, long userId, String nickname) {
        Participant participant = Participant.builder().nickname(nickname).build();
        ReflectionTestUtils.setField(participant, "id", id);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(participant, "user", user);

        return participant;
    }

    private Participant addParticipantInGroup(Group group, long userId, boolean isAdmin) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        return group.createParticipant(user, "닉네임" + userId, isAdmin);
    }

}