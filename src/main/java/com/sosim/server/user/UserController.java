package com.sosim.server.user;

import com.sosim.server.common.response.Response;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.common.util.CookieUtil;
import com.sosim.server.security.AuthUser;
import com.sosim.server.user.dto.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @DeleteMapping
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal AuthUser authUser, WithdrawRequest withdrawRequest,
                                          HttpServletRequest request, HttpServletResponse response) {
        userService.withdrawUser(authUser.getId(), withdrawRequest);
        CookieUtil.deleteRefreshToken(response);
        ResponseCode successWithdraw = ResponseCode.SUCCESS_WITHDRAW;

        return new ResponseEntity<>(Response.create(successWithdraw, null), successWithdraw.getHttpStatus());
    }
}
