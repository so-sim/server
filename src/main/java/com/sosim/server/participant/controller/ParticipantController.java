package com.sosim.server.participant.controller;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.participant.service.ParticipantService;
import com.sosim.server.participant.dto.NicknameSearchRequest;
import com.sosim.server.participant.dto.NicknameSearchResponse;
import com.sosim.server.participant.dto.request.ParticipantNicknameRequest;
import com.sosim.server.participant.dto.response.GetNicknameResponse;
import com.sosim.server.participant.dto.response.GetParticipantListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sosim.server.common.response.ResponseCode.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/group/{groupId}")
public class ParticipantController {
    private final ParticipantService participantService;

    @GetMapping("/participant")
    public ResponseEntity<?> getMyNickname(@AuthUserId long userId, @PathVariable long groupId) {
        GetNicknameResponse response = participantService.getMyNickname(userId, groupId);

        return new ResponseEntity<>(Response.create(GET_NICKNAME, response), GET_NICKNAME.getHttpStatus());
    }

    @GetMapping("/participants")
    public ResponseEntity<?> getGroupParticipants(@AuthUserId long userId, @PathVariable long groupId) {
        GetParticipantListResponse getGroupParticipants = participantService.getGroupParticipants(userId, groupId);

        return new ResponseEntity<>(Response.create(GET_PARTICIPANTS, getGroupParticipants), GET_PARTICIPANTS.getHttpStatus());
    }

    @GetMapping("/participants-nickname")
    public ResponseEntity<?> searchParticipants(@PathVariable long groupId, NicknameSearchRequest searchRequest) {
        NicknameSearchResponse response = participantService.searchParticipants(groupId, searchRequest);

        return new ResponseEntity<>(Response.create(SEARCH_PARTICIPANTS, response), SEARCH_PARTICIPANTS.getHttpStatus());
    }

    @PostMapping("/participant")
    public ResponseEntity<?> intoGroup(@AuthUserId long userId, @PathVariable long groupId,
                                       @Validated @RequestBody ParticipantNicknameRequest participantRequest) {
        participantService.createParticipant(userId, groupId, participantRequest.getNickname());

        return new ResponseEntity<>(Response.create(INTO_GROUP, null), INTO_GROUP.getHttpStatus());
    }

    @PutMapping("/participant")
    public ResponseEntity<?> reActiveParticipant(@AuthUserId long userId, @PathVariable long groupId) {
        participantService.reActiveParticipant(userId, groupId);

        return new ResponseEntity<>(Response.create(INTO_GROUP, null), INTO_GROUP.getHttpStatus());
    }

    @DeleteMapping("/participant")
    public ResponseEntity<?> withdrawGroup(@AuthUserId long userId, @PathVariable long groupId) {
        participantService.deleteParticipant(userId, groupId);

        return new ResponseEntity<>(Response.create(WITHDRAW_GROUP, null), WITHDRAW_GROUP.getHttpStatus());
    }

    @PatchMapping("/participant")
    public ResponseEntity<?> modifyNickname(@AuthUserId long userId, @PathVariable long groupId,
                                            @Validated @RequestBody ParticipantNicknameRequest request) {
        participantService.modifyNickname(userId, groupId, request.getNickname());

        return new ResponseEntity<>(Response.create(MODIFY_NICKNAME, null), MODIFY_NICKNAME.getHttpStatus());
    }

}
