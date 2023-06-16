package com.sosim.server.participant;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
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

import static com.sosim.server.common.auditing.Status.ACTIVE;
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

    @DisplayName("모임 참가자 리스트 조회 / 응답 테스트")
    @Test
    void get_participants() {
        //given
        String adminNickname = "총무닉네임";
        Group group = new Group();
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "adminId", userId);
        ReflectionTestUtils.setField(group, "adminNickname", adminNickname);
        List<Participant> participants = new ArrayList<>(List.of(
                makeParticipant(userId + 1, "유저1"),
                makeParticipant(userId + 2, "유저2")));

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(participants).when(participantRepository).findGroupNormalParticipants(groupId, adminNickname);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(userId, groupId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getAdminNickname()).isEqualTo(adminNickname);
        assertThat(response.getNicknameList()).containsExactly("유저1", "유저2");
    }

    @DisplayName("모임 참가자 리스트 조회 / 총무가 아닌 유저가 nicknameList의 첫 번째 원소")
    @Test
    void get_participants_not_admin_request() {
        //given
        String adminNickname = "총무닉네임";
        long requestUserId = 9;
        String requestUserName = "요청유저";

        Group group = new Group();
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "adminId", userId);
        ReflectionTestUtils.setField(group, "adminNickname", adminNickname);
        List<Participant> participants = new ArrayList<>(List.of(
                makeParticipant(1, userId + 1, "유저1"),
                makeParticipant(2, userId + 2, "유저2"),
                makeParticipant(3, requestUserId, requestUserName)));

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(participants).when(participantRepository).findGroupNormalParticipants(groupId, adminNickname);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(requestUserId, groupId);

        //then
        assertThat(response.getNicknameList().get(0)).isEqualTo(requestUserName);
    }

    @DisplayName("모임 참가자 리스트 조회 / 요청 유저 제외하고 nickname 오름차순 정렬")
    @Test
    void get_participants_asc_nickname() {
        //given
        String adminNickname = "총무닉네임";
        long requestUserId = 9;
        String requestUserName = "3";

        Group group = new Group();
        ReflectionTestUtils.setField(group, "id", groupId);
        ReflectionTestUtils.setField(group, "adminId", userId);
        ReflectionTestUtils.setField(group, "adminNickname", adminNickname);
        List<Participant> participants = new ArrayList<>(List.of(
                makeParticipant(3, userId + 1, "1"),
                makeParticipant(2, userId + 2, "2"),
                makeParticipant(5, requestUserId, requestUserName),
                makeParticipant(4, userId + 3, "4"),
                makeParticipant(1, userId + 4, "5")));

        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(participants).when(participantRepository).findGroupNormalParticipants(groupId, adminNickname);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(requestUserId, groupId);

        //then
        assertThat(response.getNicknameList())
                .containsExactly(requestUserName, "1", "2", "4", "5");
    }

    @DisplayName("참가자 가입 / 정상")
    @Test
    void create_participant() {
        //given
        User user = makeUser();
        Group group = makeGroup();
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(false).when(participantRepository)
                .existsByUserIdAndGroupIdAndStatus(userId, groupId, ACTIVE);
        doReturn(false).when(participantRepository)
                .existsByGroupIdAndNicknameAndStatus(groupId, nickname, ACTIVE);

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
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(true).when(participantRepository)
                .existsByUserIdAndGroupIdAndStatus(userId, groupId, ACTIVE);

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
        String nickname = "닉네임";

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(Optional.of(group)).when(groupRepository).findByIdWithParticipants(groupId);
        doReturn(false).when(participantRepository)
                .existsByUserIdAndGroupIdAndStatus(userId, groupId, ACTIVE);
        doReturn(true).when(participantRepository)
                .existsByGroupIdAndNicknameAndStatus(groupId, nickname, ACTIVE);

        //when
        CustomException exception = assertThrows(CustomException.class, () ->
                participantService.createParticipant(userId, groupId, nickname));

        //then
        assertThat(exception.getResponseCode()).isEqualTo(ALREADY_USE_NICKNAME);
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
        assertThat(e.getResponseCode()).isEqualTo(NONE_PARTICIPANT);
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
        assertThat(e.getResponseCode()).isEqualTo(NONE_PARTICIPANT);
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

    private Participant makeParticipant(long id, String nickname) {
        return makeParticipant(id, 0L, nickname);
    }

    private Participant makeParticipant(long id, long userId, String nickname) {
        Participant participant = Participant.builder().nickname(nickname).build();
        ReflectionTestUtils.setField(participant, "id", id);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(participant, "user", user);
        return participant;
    }

}