package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.group.dto.response.GetGroupListResponse;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/group")
    public ResponseEntity<?> createGroup(@AuthUserId long userId, @Validated @RequestBody CreateGroupRequest createGroupRequest) {
        GroupIdResponse groupIdResponse = groupService.createGroup(userId, createGroupRequest);
        ResponseCode createGroup = ResponseCode.CREATE_GROUP;

        return new ResponseEntity<>(Response.create(createGroup, groupIdResponse), createGroup.getHttpStatus());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroup(@AuthUserId long userId, @PathVariable("groupId") long groupId) {
        GetGroupResponse getGroupResponse = groupService.getGroup(userId, groupId);
        ResponseCode getGroup = ResponseCode.GET_GROUP;

        return new ResponseEntity<>(Response.create(getGroup, getGroupResponse), getGroup.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}")
    public ResponseEntity<?> modifyGroup(@AuthUserId long userId,
                                         @PathVariable("groupId") long groupId,
                                         @Validated @RequestBody UpdateGroupRequest updateGroupRequest) {
        GroupIdResponse groupIdResponse = groupService.updateGroup(userId, groupId, updateGroupRequest);
        ResponseCode modifyGroup = ResponseCode.MODIFY_GROUP;

        return new ResponseEntity<>(Response.create(modifyGroup, groupIdResponse), modifyGroup.getHttpStatus());
    }

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<?> deleteGroup(@AuthUserId long userId,
                                         @PathVariable("groupId") long groupId) {
        groupService.deleteGroup(userId, groupId);
        ResponseCode deleteGroup = ResponseCode.DELETE_GROUP;

        return new ResponseEntity<>(Response.create(deleteGroup, null), deleteGroup.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}/admin")
    public ResponseEntity<?> modifyAdmin(@AuthenticationPrincipal AuthUser authUser,
                                         @PathVariable("groupId") long groupId,
                                         @RequestBody ParticipantNicknameRequest participantNicknameRequest) {

        groupService.modifyAdmin(authUser.getId(), groupId, participantNicknameRequest);
        ResponseCode modifyGroupAdmin = ResponseCode.MODIFY_GROUP_ADMIN;

        return new ResponseEntity<>(Response.create(modifyGroupAdmin, null), modifyGroupAdmin.getHttpStatus());
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getMyGroups(@AuthenticationPrincipal AuthUser authUser,
                                         @RequestParam("index") Long index) {
        if (index == null) {
            throw new CustomException(ResponseCode.BINDING_ERROR);
        }

        GetGroupListResponse groupList = groupService.getMyGroups(index, authUser.getId());
        ResponseCode getGroups = ResponseCode.GET_GROUPS;

        return new ResponseEntity<>(Response.create(getGroups, groupList), getGroups.getHttpStatus());
    }

}
