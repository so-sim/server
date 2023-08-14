package com.sosim.server.event;

import com.sosim.server.config.QueryDslConfig;
import com.sosim.server.event.domain.entity.Event;
import com.sosim.server.event.domain.entity.Situation;
import com.sosim.server.event.domain.repository.EventRepository;
import com.sosim.server.event.dto.request.FilterEventRequest;
import com.sosim.server.group.domain.entity.Group;
import com.sosim.server.group.domain.repository.GroupRepository;
import com.sosim.server.user.domain.entity.User;
import com.sosim.server.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
public class EventRepositoryTest {

    private long eventId;

    private List<Long> eventIdList;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        eventIdList = new ArrayList<>();
        em.flush();
        em.clear();
    }

    @DisplayName("납부 여부 상태 일괄 변경")
    @Test
    void update_situation_all() {
        // given
        saveEventEntity();
        Situation situation = Situation.CHECK;

        // when
        System.out.println("=============================");
        eventRepository.updateSituationAll(eventIdList, situation);
        List<Event> events = eventRepository.findAllById(eventIdList);

        // then
        for (Event event : events) {
            assertThat(event.getSituation()).isEqualTo(situation);
        }
    }

    @DisplayName("닉네임 일괄 변경")
    @Test
    void update_nickname_all() {
        // given
        saveEventEntity();
        String preNickname = "닉네임1";
        String newNickname = "새 닉네임";

        // when
        System.out.println("=============================");
        eventRepository.updateNicknameAll(newNickname, preNickname);
        List<Event> events = eventRepository.findAllById(eventIdList);

        // then
        for (Event event : events) {
            assertThat(event.getNickname()).isEqualTo(newNickname);
        }
    }

    @DisplayName("Event 조회 / Group n + 1")
    @Test
    void find_by_id_with_group() {
        // given
        saveEventEntityOne();

        // when
        System.out.println("=============================");
        Event event = eventRepository.findByIdWithGroup(eventId).get();

        // then
        assertThat(event.getGroup()).isNotNull();
    }

    @DisplayName("Event 캘린더 조회용 Query DSL")
    @Test
    void get_event_calendar() {
        // given
        FilterEventRequest request = makeFilterEventRequest(null, null);
        saveEventEntity();

        // when
        System.out.println("=============================");
        List<Event> events = eventRepository.searchAll(request);

        // then
        assertThat(events).isNotNull();
    }

    @DisplayName("Event Filter Query DSL")
    @Test
    void get_event_list() {
        // given
        FilterEventRequest nicknameRequest = makeFilterEventRequest("닉네임1", null);
        FilterEventRequest situationRequest = makeFilterEventRequest(null, Situation.FULL);
        PageRequest pageRequest = PageRequest.of(0, 10);
        saveEventEntity();

        // when
        List<Event> nicknameList = eventRepository.searchAll(nicknameRequest, pageRequest).getContent();
        List<Event> situationList = eventRepository.searchAll(situationRequest, pageRequest).getContent();

        // then
        for (Event event : nicknameList) {
            assertThat(event.getNickname()).isEqualTo("닉네임1");
        }
        for (Event event : situationList) {
            assertThat(event.getSituation()).isEqualTo(Situation.FULL);
        }
    }

    private void saveEventEntity() {
        List<Event> events = new ArrayList<>();

        int size = 3;
        while (size-- > 0) {
            events.add(makeEvent(Situation.NONE, "닉네임1"));
        }

        List<Event> eventList = eventRepository.saveAll(events);

        for (Event event : eventList) {
            eventIdList.add(event.getId());
        }
    }

    private void saveEventEntityOne() {
        Event event = eventRepository.save(makeEvent(null, null));
        eventId = event.getId();
    }

    private Event makeEvent(Situation situation, String nickname) {
        User user = User.builder().build();
        userRepository.save(user);
        Group group = Group.builder().build();
        groupRepository.save(group);
        return Event.builder()
                .situation(situation)
                .date(LocalDate.of(2023, 8, 1))
                .group(group)
                .nickname(nickname)
                .user(user)
                .build();
    }

    private FilterEventRequest makeFilterEventRequest(String nickname, Situation situation) {
        return FilterEventRequest.builder()
                .startDate(LocalDate.of(2023, 8, 1))
                .endDate(LocalDate.of(2023, 8, 31))
                .nickname(nickname)
                .situation(situation)
                .build();
    }
}
