package com.sosim.server.group;

import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class GroupRepositoryTest {

    private long groupId1 = 1L;
    private long groupId2 = 2L;

    private long userId1 = 1L;
    private long userId2 = 2L;
    private long userId3 = 3L;
    private long userId4 = 4L;
    private long userId5 = 4L;
    private long userId6 = 4L;

    private String nickname1 = "닉네임1";
    private String nickname2 = "닉네임2";
    private String nickname3 = "닉네임3";
    private String nickname4 = "닉네임4";
    private String nickname5 = "닉네임4";
    private String nickname6 = "닉네임4";

    private int nicknameNo;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    ParticipantRepository participantRepository;

    @Autowired
    UserRepository userRepository;

    private int activeParticipantSize;
    private int deleteParticipantSize;

    @Test
    void findByIdWithParticipants() {
        Group group = groupRepository.findByIdWithParticipants(1L).get();

        assertThat(group.getParticipantList().size()).isEqualTo(activeParticipantSize);
    }

    @DisplayName("findMyGroups / 내 그룹 조회 페이징 성공")
    @Test
    void findMyGroups() {
        saveFindMyGroupsData();

        PageRequest pageable1 = PageRequest.of(0, 2);
        PageRequest pageable2 = PageRequest.of(1, 2);
        PageRequest pageable3 = PageRequest.of(2, 2);

        Slice<Group> myGroups1 = groupRepository.findMyGroups(userId1, pageable1);
        Slice<Group> myGroups2 = groupRepository.findMyGroups(userId1, pageable2);
        Slice<Group> myGroups3 = groupRepository.findMyGroups(userId1, pageable3);

        assertThat(myGroups1.hasNext()).isTrue();
        assertThat(myGroups1.getSize()).isEqualTo(2);

        assertThat(myGroups2.hasNext()).isFalse();
        assertThat(myGroups2.getSize()).isEqualTo(2);

        assertThat(myGroups3.hasNext()).isFalse();
        assertThat(myGroups3.hasContent()).isFalse();
    }

    @Disabled
    @DisplayName("findMyGroups / n + 1확인")
    @Test
    void findMyGroups_check_n_plus_1() {
        saveFindMyGroupsData();

        PageRequest pageable = PageRequest.of(0, 2);

        Slice<Group> myGroups = groupRepository.findMyGroups(userId1, pageable);

        Group group = myGroups.get().filter(g -> g.getId().equals(1L))
                .findFirst().get();
        System.out.println(group.isAdminUser(1L));
        System.out.println(group.getAdminParticipant().getNickname());
        System.out.println(group.getNumberOfParticipants());
    }

    private void saveFindMyGroupsData() {
        int n = 5;
        User user = userRepository.save(makeUser());

        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            groups.add(groupRepository.save(makeGroup()));
        }

        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            participants.add(Participant.create(user, groups.get(i), "닉네임" + nicknameNo++, false));
        }
        participants.get(0).signOn();
        participants.get(n / 2).delete();
        for (Participant participant : participants) {
            participantRepository.save(participant);
        }
    }

//    user1, 2, 3, 4
    //group1, 2
    //participant

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();

//
//
//        groupId = group.getId();
//        activeParticipantSize = 5;
//        for (int i = 0; i < activeParticipantSize; i++) {
//            Participant participant = saveParticipant(group);
//        }
//        deleteParticipantSize = 3;
//        for (int i = 0; i < deleteParticipantSize; i++) {
//            Participant participant = saveDeleteParticipant(group);
//        }
    }

    private User makeUser() {
        return User.builder().build();
    }

    private Participant saveDeleteParticipant(Group group) {
        Participant participant = Participant.create(null, group, "닉네임" + nicknameNo++, false);
        participant.addGroup(group);
        participant.withdrawGroup(group);
        return participantRepository.save(participant);
    }

    private Participant saveParticipant(Group group) {
        Participant participant = Participant.create(null, group,"닉네임" + nicknameNo++, false);
        participant.addGroup(group);
        return participantRepository.save(participant);
    }

    private static Group makeGroup() {
        return Group.builder().build();
    }
}