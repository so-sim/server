package com.sosim.server.user;

import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.GroupService;
import com.sosim.server.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/withdraw")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> withdrawInfo(@AuthenticationPrincipal AuthUser authUser) {
        userService.withdrawInfo(authUser.getId());
        ResponseCode canWithdraw = ResponseCode.CAN_WITHDRAW;

        return new ResponseEntity<>(Response.create(canWithdraw, null), canWithdraw.getHttpStatus());
    }
}
