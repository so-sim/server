package com.sosim.server.group;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.dto.request.CreateGroupRequest;
import com.sosim.server.group.dto.response.CreateGroupResponse;
import com.sosim.server.group.dto.response.GetGroupResponse;
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
            throw new CustomException(ResponseCode.BINDING_ERROR, bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage());
        }

        CreateGroupResponse createGroupResponse = groupService.createGroup(authUser.getId(), createGroupRequest);
        ResponseCode createGroup = ResponseCode.CREATE_GROUP;

        return new ResponseEntity<>(Response.create(createGroup, createGroupResponse), createGroup.getHttpStatus());
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
}
