package com.sosim.server.group.controller;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.group.service.GroupService;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.ModifyGroupRequest;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.group.dto.response.MyGroupsResponse;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sosim.server.common.response.ResponseCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/group")
    public ResponseEntity<?> createGroup(@AuthUserId long userId, @Validated @RequestBody CreateGroupRequest createGroupRequest) {
        GroupIdResponse groupIdResponse = groupService.createGroup(userId, createGroupRequest);

        return new ResponseEntity<>(Response.create(CREATE_GROUP, groupIdResponse), CREATE_GROUP.getHttpStatus());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroup(@AuthUserId(required = false) long userId, @PathVariable long groupId) {
        GetGroupResponse getGroupResponse = groupService.getGroup(userId, groupId);

        return new ResponseEntity<>(Response.create(GET_GROUP, getGroupResponse), GET_GROUP.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}")
    public ResponseEntity<?> modifyGroup(@AuthUserId long userId, @PathVariable long groupId,
                                         @Validated @RequestBody ModifyGroupRequest modifyGroupRequest) {
        GroupIdResponse groupIdResponse = groupService.updateGroup(userId, groupId, modifyGroupRequest);

        return new ResponseEntity<>(Response.create(MODIFY_GROUP, groupIdResponse), MODIFY_GROUP.getHttpStatus());
    }

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<?> deleteGroup(@AuthUserId long userId, @PathVariable long groupId) {
        groupService.deleteGroup(userId, groupId);

        return new ResponseEntity<>(Response.create(DELETE_GROUP, null), DELETE_GROUP.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}/admin")
    public ResponseEntity<?> modifyAdmin(@AuthUserId long userId, @PathVariable long groupId,
                                         @RequestBody ParticipantNicknameRequest participantNicknameRequest) {

        groupService.modifyAdmin(userId, groupId, participantNicknameRequest);

        return new ResponseEntity<>(Response.create(MODIFY_GROUP_ADMIN, null), MODIFY_GROUP_ADMIN.getHttpStatus());
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getMyGroups(@AuthUserId long userId, @RequestParam int page) {
        MyGroupsResponse myGroups = groupService.getMyGroups(userId, page);

        return new ResponseEntity<>(Response.create(GET_MY_GROUPS, myGroups), GET_MY_GROUPS.getHttpStatus());
    }

}
