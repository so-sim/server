package com.sosim.server.participant;

import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import com.sosim.server.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

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

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
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

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
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

        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(participants).when(participantRepository).findGroupNormalParticipants(groupId, adminNickname);

        //when
        GetParticipantListResponse response = participantService.getGroupParticipants(requestUserId, groupId);

        //then
        assertThat(response.getNicknameList())
                .containsExactly(requestUserName, "1", "2", "4", "5");
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