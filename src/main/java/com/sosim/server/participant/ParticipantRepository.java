package com.sosim.server.participant;

import com.sosim.server.user.User;
import com.sosim.server.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByUserAndGroup(User user, Group group);
    boolean existsByGroupAndNickname(Group group, String nickname);
}
