package com.sosim.server.participant;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.participant.dto.request.CreateParticipantRequest;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sosim.server.common.response.ResponseCode.*;
import static com.sosim.server.common.response.ResponseCode.GET_PARTICIPANTS;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/group/{groupId}")
public class ParticipantController {
    private final ParticipantService participantService;

    @GetMapping("/participants")
    public ResponseEntity<?> getGroupParticipants(@AuthUserId long userId, @PathVariable("groupId") long groupId) {
        GetParticipantListResponse getGroupParticipants = participantService.getGroupParticipants(userId, groupId);

        return new ResponseEntity<>(Response.create(GET_PARTICIPANTS, getGroupParticipants), GET_PARTICIPANTS.getHttpStatus());
    }

    @PostMapping("/participant")
    public ResponseEntity<?> intoGroup(@AuthUserId long userId, @PathVariable("groupId") long groupId,
                                       @Validated @RequestBody CreateParticipantRequest participantRequest) {
        participantService.createParticipant(userId, groupId, participantRequest.getNickname());

        return new ResponseEntity<>(Response.create(INTO_GROUP, null), INTO_GROUP.getHttpStatus());
    }

//    @PatchMapping("/admin")
//    public ResponseEntity<?> modifyAdmin(@AuthUserId long userId,
//                                         @PathVariable("groupId") long groupId,
//                                         @RequestBody ParticipantNicknameRequest participantNicknameRequest) {
//
//        groupService.modifyAdmin(userId, groupId, participantNicknameRequest);
//        ResponseCode modifyGroupAdmin = ResponseCode.MODIFY_GROUP_ADMIN;
//
//        return new ResponseEntity<>(Response.create(modifyGroupAdmin, null), modifyGroupAdmin.getHttpStatus());
//    }
//
//    @DeleteMapping("/participant")
//    public ResponseEntity<?> withdrawGroup(@AuthUserId long userId,
//                                           @PathVariable("groupId") long groupId) {
//        groupService.withdrawGroup(userId, groupId);
//        ResponseCode withdrawGroup = ResponseCode.WITHDRAW_GROUP;
//
//        return new ResponseEntity<>(Response.create(withdrawGroup, null), withdrawGroup.getHttpStatus());
//    }
//
//    @PatchMapping("/participant")
//    public ResponseEntity<?> modifyNickname(@AuthUserId long userId,
//                                            @PathVariable ("groupId") long groupId,
//                                            @Validated @RequestBody ParticipantNicknameRequest participantNicknameRequest) {
//        groupService.modifyNickname(userId, groupId, participantNicknameRequest);
//        ResponseCode modifyNickname = ResponseCode.MODIFY_NICKNAME;
//
//        return new ResponseEntity<>(Response.create(modifyNickname, null), modifyNickname.getHttpStatus());
//    }
//
//    @GetMapping("/participant")
//    public ResponseEntity<?> getMyNickname(@AuthUserId long userId,
//                                           @PathVariable("groupId") long groupId) {
//        GetNicknameResponse getNicknameResponse = groupService.getMyNickname(userId, groupId);
//        ResponseCode getNickname = ResponseCode.GET_NICKNAME;
//
//        return new ResponseEntity<>(Response.create(getNickname, getNicknameResponse), getNickname.getHttpStatus());
//    }
}
