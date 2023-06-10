package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.request.UpdateGroupRequest;
import com.sosim.server.group.dto.response.GroupIdResponse;
import com.sosim.server.group.dto.response.GetGroupResponse;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
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
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal AuthUser authUser,
                                         @Validated @RequestBody CreateGroupRequest createGroupRequest,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

        GroupIdResponse groupIdResponse = groupService.createGroup(authUser.getId(), createGroupRequest);
        ResponseCode createGroup = ResponseCode.CREATE_GROUP;

        return new ResponseEntity<>(Response.create(createGroup, groupIdResponse), createGroup.getHttpStatus());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroup(@AuthenticationPrincipal AuthUser authUser,
                                      @PathVariable("groupId") Long groupId) {
        GetGroupResponse getGroupResponse = groupService.getGroup(
                authUser != null ? authUser.getId() : 0, groupId);
        ResponseCode getGroup = ResponseCode.GET_GROUP;

        return new ResponseEntity<>(Response.create(getGroup, getGroupResponse), getGroup.getHttpStatus());
    }

    @GetMapping("/group/{groupId}/participants")
    public ResponseEntity<?> getGroupParticipants(@AuthenticationPrincipal AuthUser authUser,
                                                  @PathVariable("groupId") Long groupId) {
        GetParticipantListResponse getGroupParticipants = groupService.getGroupParticipants(authUser.getId(), groupId);
        ResponseCode getParticipants = ResponseCode.GET_PARTICIPANTS;

        return new ResponseEntity<>(Response.create(getParticipants, getGroupParticipants), getParticipants.getHttpStatus());
    }

    @PatchMapping("/group/{groupId}")
    public ResponseEntity<?> modifyGroup(@AuthenticationPrincipal AuthUser authUser,
                                         @PathVariable("groupId") Long groupId,
                                         @Validated @RequestBody UpdateGroupRequest updateGroupRequest,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingError(bindingResult);
        }

        GroupIdResponse groupIdResponse = groupService.updateGroup(authUser.getId(), groupId, updateGroupRequest);
        ResponseCode modifyGroup = ResponseCode.MODIFY_GROUP;

        return new ResponseEntity<>(Response.create(modifyGroup, groupIdResponse), modifyGroup.getHttpStatus());
    }

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<?> deleteGroup(@AuthenticationPrincipal AuthUser authUser,
                                         @PathVariable("groupId") Long groupId) {
        groupService.deleteGroup(authUser.getId(), groupId);
        ResponseCode deleteGroup = ResponseCode.DELETE_GROUP;

        return new ResponseEntity<>(Response.create(deleteGroup, null), deleteGroup.getHttpStatus());
    }

    @PostMapping("/group/{groupId}/participant")
    public ResponseEntity<?> intoGroup(@AuthenticationPrincipal AuthUser authUser,
                                       @PathVariable("groupId") Long groupId,
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
                                         @PathVariable("groupId") Long groupId,
                                         @RequestBody ParticipantNicknameRequest participantNicknameRequest) {

        groupService.modifyAdmin(authUser.getId(), groupId, participantNicknameRequest);
        ResponseCode modifyGroupAdmin = ResponseCode.MODIFY_GROUP_ADMIN;

        return new ResponseEntity<>(Response.create(modifyGroupAdmin, null), modifyGroupAdmin.getHttpStatus());
    }

    private void bindingError(BindingResult bindingResult) {
        throw new CustomException(ResponseCode.BINDING_ERROR, bindingResult.getFieldError().getField(),
                bindingResult.getFieldError().getDefaultMessage());
    }
}
