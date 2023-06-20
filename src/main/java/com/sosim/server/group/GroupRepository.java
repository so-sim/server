package com.sosim.server.group;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryDsl {
    @Query("select g from Group g where g.id = :groupId and g.status = 'ACTIVE'")
    Optional<Group> findById(@Param("groupId") long groupId);

    @Query("SELECT g FROM Group g " +
            "JOIN FETCH g.participantList p " +
            "WHERE g.id = :groupId AND g.status = 'ACTIVE'")
    @EntityGraph(attributePaths = {"participantList"})
    Optional<Group> findByIdWithParticipants(@Param("groupId") long groupId);

    @Query("select g from Group g " +
            "join fetch g.participantList p where p.user.id in (:adminId) and g.status = 'ACTIVE' " +
            "and p.status = 'ACTIVE'")
    List<Group> findFetchJoinGroupByAdminId(@Param("adminId") long groupId);

//    @Query("SELECT g FROM Group g " +
//            "WHERE g.id IN (SELECT p.group.id FROM Participant p " +
//            "               WHERE p.user.id = :userId AND p.status = 'ACTIVE') " +
//            "ORDER BY g.id DESC")
//    @EntityGraph(attributePaths = "participantList")
//    Slice<Group> findMyGroups(@Param("userId") long userId, Pageable pageable);
}
