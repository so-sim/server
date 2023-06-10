package com.sosim.server.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("select g from Group g where g.id = :groupId and status = 'ACTIVE'")
    Optional<Group> findById(Long groupId);
}
