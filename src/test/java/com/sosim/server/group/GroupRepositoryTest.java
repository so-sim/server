package com.sosim.server.group;

import com.sosim.server.config.TestConfig;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.group.domain.util.MyGroupPaginationUtil;
import com.sosim.server.group.dto.MyGroupPageDto;
import com.sosim.server.participant.domain.entity.Participant;
import com.sosim.server.participant.domain.repository.ParticipantRepository;
import com.sosim.server.user.domain.entity.User;
import com.sosim.server.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(TestConfig.class)
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

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
        participantRepository.deleteAll();
        userRepository.deleteAll();
        nicknameNo = 1;
        groupId = 1L;
        userId = 1L;
        em.flush();
        em.clear();
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

    @DisplayName("findMyGroups / 내 그룹 조회 첫 페이지는 17개")
    @Test
    void findMyGroups_first_page() {
        //given
        int size = 100;
        saveMyGroupsData(size);

        int page = 0;
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);
        em.flush();
        em.clear();

        //when
        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageDto.getOffset(), pageDto.getLimit());

        assertThat(myGroups.getNumberOfElements()).isEqualTo(17);
        assertThat(myGroups.hasNext()).isTrue();
    }

    @DisplayName("findMyGroups / 내 그룹 조회 첫 페이지 아니면 18개")
    @Test
    void findMyGroups_other_page() {
        //given
        int size = 100;
        saveMyGroupsData(size);

        int page1 = 1;
        int page2 = 3;
        MyGroupPageDto pageDto1 = MyGroupPaginationUtil.calculateOffsetAndSize(page1);
        MyGroupPageDto pageDto2 = MyGroupPaginationUtil.calculateOffsetAndSize(page2);

        //when
        Slice<Group> myGroups1 = groupRepository.findMyGroups(userId, pageDto1.getOffset(), pageDto1.getLimit());
        Slice<Group> myGroups2 = groupRepository.findMyGroups(userId, pageDto2.getOffset(), pageDto2.getLimit());

        assertThat(myGroups1.getNumberOfElements()).isEqualTo(18);
        assertThat(myGroups1.hasNext()).isTrue();

        assertThat(myGroups2.getNumberOfElements()).isEqualTo(18);
        assertThat(myGroups2.hasNext()).isTrue();
    }

    @Disabled
    @DisplayName("findMyGroups / n + 1확인")
    @Test
    void findMyGroups_check_n_plus_1() {
        //given
        int size = 100;
        saveMyGroupsData(size);

        int page = 0;
        MyGroupPageDto pageDto = MyGroupPaginationUtil.calculateOffsetAndSize(page);

        //when
        Slice<Group> myGroups = groupRepository.findMyGroups(userId, pageDto.getOffset(), pageDto.getLimit());

        em.flush();
        em.clear();
        System.out.println("\n\n=========================\n");

        Group group = myGroups.stream().filter(g -> g.getId().equals(1L))
                .findFirst().get();
        System.out.println(group.isAdminUser(1L));
        System.out.println(group.getAdminParticipant().getNickname());
        System.out.println(group.getNumberOfParticipants());
    }

    @Disabled
    @DisplayName("findMyGroups / 속도 확인")
    @Test
    void findMyGroups_speed() {
        //given
        int size = 100;
        saveMyGroupsData(size);

        List<MyGroupPageDto> pageDtos = new ArrayList<>();
        for (int page = 0; page < 5; page++) {
            pageDtos.add(MyGroupPaginationUtil.calculateOffsetAndSize(page));
        }

        em.flush();
        em.clear();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 5; i++) {
            groupRepository.findMyGroups(userId, pageDtos.get(i).getOffset(), pageDtos.get(i).getLimit());
        }
        stopWatch.stop();

        System.out.println("\n\n======= [Milli Time] = " + stopWatch.getTotalTimeMillis() + "ms =========\n\n");
    }

    @DisplayName("findMyGroups / Participant Fetch join 확인")
    @Test
    void findMyGroups_check_fetch_join_participantList() {
        //given
        User user1 = userRepository.save(makeUser());
        User user2 = userRepository.save(makeUser());

        Group group1 = groupRepository.save(makeGroup());
        Group group2 = groupRepository.save(makeGroup());

        List<Participant> participants = new ArrayList<>();
        participants.add(group1.createParticipant(user1, makeNickname(), true));
        participants.add(group2.createParticipant(user1, makeNickname(), false));
        participants.add(group1.createParticipant(user2, makeNickname(), false));
        participants.add(group2.createParticipant(user2, makeNickname(), true));

        for (Participant participant : participants) {
            participantRepository.save(participant);
        }

        em.flush();
        em.clear();

        Slice<Group> myGroups = groupRepository.findMyGroups(user1.getId(), 0, 2);

        assertThat(myGroups.hasNext()).isFalse();
        assertThat(myGroups.getNumberOfElements()).isEqualTo(2);
        assertThat(myGroups.getContent().get(0).getId()).isEqualTo(2L);
        assertThat(myGroups.getContent().get(0).getParticipantList().size()).isEqualTo(2);
    }

    private int saveOneGroupAndParticipants() {
        Group group = groupRepository.save(makeGroup());
        groupId = group.getId();

        int n = 10;
        int deletedN = 2;
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            User user = userRepository.save(makeUser());
            participants.add(group.createParticipant(user, makeNickname(), false));
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

    private String makeNickname() {
        return "닉네임" + nicknameNo++;
    }

    private void saveMyGroupsData(int size) {
        User user = userRepository.save(makeUser());
        userId = user.getId();

        List<Group> groups = new ArrayList<>();
        for (int i = 0; i <= size; i++) {
            groups.add(groupRepository.save(makeGroup()));
        }

        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i <= size; i++) {
            participants.add(groups.get(i).createParticipant(user, makeNickname(), false));
        }
        participants.get(0).signOn();
        participants.get(size / 2).delete();
        groups.get(size / 2).delete();
        for (Participant participant : participants) {
            participantRepository.save(participant);
        }
    }

    private User makeUser() {
        return User.builder().build();
    }

    private static Group makeGroup() {
        return Group.builder().build();
    }
}