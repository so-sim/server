package com.sosim.server.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("select g from Group g where g.id = :groupId and g.status = 'ACTIVE'")
    Optional<Group> findById(@Param("groupId") Long groupId);

    @Query("select g from Group g where g.adminId in (:adminId) and g.status = 'ACTIVE'")
    List<Group> findListByAdminId(@Param("adminId") Long id);

    @Query("select g from Group g " +
            "join fetch g.participantList where g.adminId in (:adminId) and g.status = 'ACTIVE'")
    List<Group> findFetchJoinGroupByAdminId(@Param("adminId") Long groupId);
}
