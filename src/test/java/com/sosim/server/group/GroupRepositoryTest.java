package com.sosim.server.group;

import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled // TODO 수정하기
@DataJpaTest
class GroupRepositoryTest {
    private static final Logger log = LoggerFactory.getLogger(GroupRepositoryTest.class);


    private long groupId = 1L;

    private long userId = 1L;


    private int nicknameNo;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    ParticipantRepository participantRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
        nicknameNo = 1;
        groupId = 1L;
        userId = 1L;
    }

    @DisplayName("findByIdWithParticipants / 정상 작동 확인")
    @Test
    void findByIdWithParticipants() {
        int activeNumber = saveOneGroupAndParticipants();

        Group group = groupRepository.findByIdWithParticipants(groupId).get();

        assertThat(group.getParticipantList().size()).isEqualTo(activeNumber);
    }

    @Disabled
    @DisplayName("findByIdWithParticipants / n + 1 문제 확인")
    @Test
    void findByIdWithParticipants_n_plus_1() {
        int activeNumber = saveOneGroupAndParticipants();

        log.info("\n=============== Query =================");
        Group group = groupRepository.findByIdWithParticipants(groupId).get();

        group.isAdminUser(userId);
        group.hasParticipant(userId);
        group.getNumberOfParticipants();
    }

    @Disabled // TODO : 페이지네이션 쿼리 수정 후 테스트 재작성
    @DisplayName("findMyGroups / 내 그룹 조회 페이징 성공")
    @Test
    void findMyGroups() {
        //given
        int size = saveFindMyGroupsData();

        int preSize = 3;
        int nextSize = 5;
        int nextPageNo = 0 + (3 / nextSize);
        PageRequest pageable1 = PageRequest.of(0, preSize);
        PageRequest pageable2 = PageRequest.of(nextPageNo, nextSize);
//        PageRequest pageable3 = PageRequest.of(2, pageSize);

        //when
//        Slice<Group> myGroups1 = groupRepository.findMyGroups(userId, pageable1);
//        Slice<Group> myGroups2 = groupRepository.findMyGroups(userId, pageable2);
//        Slice<Group> myGroups3 = groupRepository.findMyGroups(userId, pageable3);

        System.out.println("\n\n====================");
//        System.out.println("=== 1 ===");
//        List<Group> content1 = myGroups1.getContent();
//        System.out.println("size = " + content1.size());
//        for (Group group : content1) {
//            System.out.println("group.getId() = " + group.getId());
//        }
//        System.out.println("\n=== 2 ===");
//        List<Group> content2 = myGroups2.getContent();
//        System.out.println("size = " + content2.size());
//        for (Group group : content2) {
//            System.out.println("group.getId() = " + group.getId());
//        }
//        System.out.println();

        //then
//        assertThat(myGroups1.hasNext()).isTrue();
//        assertThat(myGroups1.getNumberOfElements()).isEqualTo(pageSize);
//
//        assertThat(myGroups2.hasNext()).isFalse();
//        assertThat(myGroups2.getNumberOfElements()).isEqualTo(size - pageSize);

//        assertThat(myGroups3.hasNext()).isFalse();
//        assertThat(myGroups3.hasContent()).isFalse();
    }

    @Disabled
    @DisplayName("findMyGroups / n + 1확인")
    @Test
    void findMyGroups_check_n_plus_1() {
//        saveFindMyGroupsData();
//
//        PageRequest pageable = PageRequest.of(0, 2);
//
//        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageable);
//
//        Group group = myGroups.get().filter(g -> g.getId().equals(1L))
//                .findFirst().get();
//        System.out.println(group.isAdminUser(1L));
//        System.out.println(group.getAdminParticipant().getNickname());
//        System.out.println(group.getNumberOfParticipants());
    }

    private int saveOneGroupAndParticipants() {
        Group group = groupRepository.save(makeGroup());
        groupId = group.getId();
        User user = userRepository.save(makeUser());
        userId = user.getId();

        int n = 10;
        int deletedN = 2;
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            participants.add(Participant.create(user, group, "닉네임" + nicknameNo, false));
        }
        participants.get(0).signOn();
        for (int i = 1; i <= deletedN; i++) {
            Participant delete = participants.get(n % (deletedN + 1) + i);
            delete.delete();
            group.getParticipantList().remove(delete);
        }
        for (Participant participant : participants) {
            participantRepository.save(participant);
        }
        groupRepository.save(group);
        return n - deletedN;
    }

    private int saveFindMyGroupsData() {
        int n = 10;
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
        groups.get(n / 2).delete();
        for (Participant participant : participants) {
            participantRepository.save(participant);
        }
        return n - 1;
    }

    private User makeUser() {
        return User.builder().build();
    }

    private static Group makeGroup() {
        return Group.builder().build();
    }
}