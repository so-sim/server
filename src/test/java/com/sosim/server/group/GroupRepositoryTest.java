package com.sosim.server.group;

import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class GroupRepositoryTest {

    private long groupId = 1L;
    private long userId = 1L;
    private int nicknameNo;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    ParticipantRepository participantRepository;
    private int activeParticipantSize;
    private int deleteParticipantSize;

    @Test
    void findByIdWithParticipants() {
        Group group = groupRepository.findByIdWithParticipants(groupId).get();

        assertThat(group.getParticipantList().size()).isEqualTo(activeParticipantSize);
    }

    @BeforeEach
    void setUp() {
        Group group = groupRepository.save(makeGroup());
        groupId = group.getId();
        activeParticipantSize = 5;
        for (int i = 0; i < activeParticipantSize; i++) {
            Participant participant = saveParticipant(group);
        }
        deleteParticipantSize = 3;
        for (int i = 0; i < deleteParticipantSize; i++) {
            Participant participant = saveDeleteParticipant(group);
        }
    }

    private Participant saveDeleteParticipant(Group group) {
        Participant participant = Participant.create(null, "닉네임" + nicknameNo++);
        participant.addGroup(group);
        participant.withdrawGroup(group);
        return participantRepository.save(participant);
    }

    private Participant saveParticipant(Group group) {
        Participant participant = Participant.create(null, "닉네임" + nicknameNo++);
        participant.addGroup(group);
        return participantRepository.save(participant);
    }

    private static Group makeGroup() {
        return Group.builder().build();
    }
}