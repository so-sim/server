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
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/group")
    public ResponseEntity<?> createGroup(@AuthUserId long userId, @Validated @RequestBody CreateGroupRequest createGroupRequest,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

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
                                         @Validated @RequestBody UpdateGroupRequest updateGroupRequest,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

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

    @PostMapping("/group/{groupId}/participant")
    public ResponseEntity<?> intoGroup(@AuthenticationPrincipal AuthUser authUser,
                                       @PathVariable("groupId") long groupId,
                                       @Validated @RequestBody ParticipantNicknameRequest participantNicknameRequest,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

        groupService.intoGroup(authUser.getId(), groupId, participantNicknameRequest);
        ResponseCode intoGroup = ResponseCode.INTO_GROUP;

        return new ResponseEntity<>(Response.create(intoGroup, null), intoGroup.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}/admin")
    public ResponseEntity<?> modifyAdmin(@AuthenticationPrincipal AuthUser authUser,
                                         @PathVariable("groupId") long groupId,
                                         @RequestBody ParticipantNicknameRequest participantNicknameRequest) {

        groupService.modifyAdmin(authUser.getId(), groupId, participantNicknameRequest);
        ResponseCode modifyGroupAdmin = ResponseCode.MODIFY_GROUP_ADMIN;

        return new ResponseEntity<>(Response.create(modifyGroupAdmin, null), modifyGroupAdmin.getHttpStatus());
    }

    @DeleteMapping("/group/{groupId}/participant")
    public ResponseEntity<?> withdrawGroup(@AuthenticationPrincipal AuthUser authUser,
                                           @PathVariable("groupId") long groupId) {
        groupService.withdrawGroup(authUser.getId(), groupId);
        ResponseCode withdrawGroup = ResponseCode.WITHDRAW_GROUP;

        return new ResponseEntity<>(Response.create(withdrawGroup, null), withdrawGroup.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}/participant")
    public ResponseEntity<?> modifyNickname(@AuthenticationPrincipal AuthUser authUser,
                                            @PathVariable ("groupId") long groupId,
                                            @Validated @RequestBody ParticipantNicknameRequest participantNicknameRequest) {
        groupService.modifyNickname(authUser.getId(), groupId, participantNicknameRequest);
        ResponseCode modifyNickname = ResponseCode.MODIFY_NICKNAME;

        return new ResponseEntity<>(Response.create(modifyNickname, null), modifyNickname.getHttpStatus());
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

    @GetMapping("/group/{groupId}/participant")
    public ResponseEntity<?> getMyNickname(@AuthenticationPrincipal AuthUser authUser,
                                           @PathVariable("groupId") long groupId) {
        GetNicknameResponse getNicknameResponse = groupService.getMyNickname(authUser.getId(), groupId);
        ResponseCode getNickname = ResponseCode.GET_NICKNAME;

        return new ResponseEntity<>(Response.create(getNickname, getNicknameResponse), getNickname.getHttpStatus());
    }

    private void bindingError(BindingResult bindingResult) {
        throw new CustomException(ResponseCode.BINDING_ERROR, bindingResult.getFieldError().getField(),
                bindingResult.getFieldError().getDefaultMessage());
    }
}
