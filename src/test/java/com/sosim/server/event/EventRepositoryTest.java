package com.sosim.server.event;

import com.sosim.server.config.QueryDslConfig;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
public class EventRepositoryTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        em.flush();
        em.clear();
    }

    @DisplayName("납부 여부 상태 일괄 변경")
    @Test
    void update_situation_all() {
        // given
        saveEventEntity();
        List<Long> eventIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        Situation situation = Situation.CHECK;

        // when
        System.out.println("=============================");
        eventRepository.updateSituationAll(eventIdList, situation);
        List<Event> events = eventRepository.findAllById(eventIdList);

        // then
        assertThat(events.get(0).getSituation()).isEqualTo(situation);
        assertThat(events.get(1).getSituation()).isEqualTo(situation);
        assertThat(events.get(2).getSituation()).isEqualTo(situation);
    }

    @DisplayName("닉네임 일괄 변경")
    @Test
    void update_nickname_all() {
        // given
        saveEventEntity();
        String preNickname = "닉네임2";
        String newNickname = "새 닉네임";
        List<Long> eventIdList = new ArrayList<>(Arrays.asList(4L, 5L, 6L));

        // when
        System.out.println("=============================");
        eventRepository.updateNicknameAll(newNickname, preNickname);
        List<Event> events = eventRepository.findAllById(eventIdList);

        // then
        assertThat(events.get(0).getNickname()).isEqualTo(newNickname);
        assertThat(events.get(1).getNickname()).isEqualTo(newNickname);
        assertThat(events.get(2).getNickname()).isEqualTo(newNickname);
    }

    @DisplayName("Event 조회 / Group n + 1")
    @Test
    void find_by_id_with_group() {
        // given
        saveEventEntity();
        long eventId = 1L;

        // when
        System.out.println("=============================");
        Event event = eventRepository.findByIdWithGroup(eventId).get();

        // then
        assertThat(event.getGroup()).isNotNull();
    }

    private void saveEventEntity() {
        List<Event> events = new ArrayList<>();

        int size = 3;
        while (size-- > 0) {
            events.add(makeEvent(Situation.NONE, "닉네임1"));
        }

        size = 3;
        while (size-- > 0) {
            events.add(makeEvent(Situation.FULL, "닉네임2"));
        }

        eventRepository.saveAll(events);
    }

    private Event makeEvent(Situation situation, String nickname) {
        Group group = Group.builder().build();
        groupRepository.save(group);
        return Event.builder()
                .situation(situation)
                .group(group)
                .nickname(nickname)
                .build();
    }
}
