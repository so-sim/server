package com.sosim.server.participant;

import com.sosim.server.config.QueryDslConfig;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.user.User;
import com.sosim.server.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
class ParticipantRepositoryTest {

    @Autowired
    ParticipantRepository participantRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private long groupId;
    private long userId;
    private int nameNo;
    private long adminId;
    private String adminName;
    private Participant admin;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        userId = 1L;
        nameNo = 10000;
        admin = null;
        adminId = 0;
        adminName = "";
    }

    @Test
    @DisplayName("findGroupNormalParticipants")
    void findGroupNormalParticipants() throws Exception {
        //given
        int size = 4;
        saveParticipantsInGroup(groupId, size);

        //when
        List<Participant> normalParticipants = participantRepository.findGroupNormalParticipants(groupId, adminName);

        //then
        assertThat(admin).isNotIn(normalParticipants);
    }

    private void saveParticipantsInGroup(long groupId, int size) {
        Group group = makeGroup();
        groupId = group.getId();
        participantRepository.save(makeAdminParticipant(group, makeNickname()));
        for (int i = 1; i < size; i++) {
            participantRepository.save(makeParticipant(group, makeNickname()));
        }
    }

    private String makeNickname() {
        return String.format("닉네임%d", nameNo--);
    }

    private Group makeGroup() {
        Group group = Group.builder().build();
        return groupRepository.save(group);
    }

    private Participant makeAdminParticipant(Group group, String nickname) {
        admin = makeParticipant(group, nickname);
        group.modifyAdmin(admin);
        groupRepository.save(group);
        adminId = group.getAdminId();
        adminName = group.getAdminNickname();
        return admin;
    }

    private Participant makeParticipant(Group group, String nickname) {
        User user = makeUser();
        Participant participant = Participant.create(user, nickname);
        participant.addGroup(group);
        return participant;
    }

    private User makeUser() {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId++);
        return userRepository.save(user);
    }
}