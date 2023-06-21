package com.sosim.server.user;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.util.CookieUtil;
import com.sosim.server.user.dto.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.sosim.server.common.response.ResponseCode.CAN_WITHDRAW;
import static com.sosim.server.common.response.ResponseCode.SUCCESS_WITHDRAW;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/withdraw")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> checkWithdraw(@AuthUserId long userId) {
        userService.canWithdraw(userId);

        return new ResponseEntity<>(Response.create(CAN_WITHDRAW, null), CAN_WITHDRAW.getHttpStatus());
    }

    @DeleteMapping
    public ResponseEntity<?> withdrawUser(@AuthUserId long userId, WithdrawRequest withdrawRequest,
                                          HttpServletRequest request, HttpServletResponse response) {
        userService.withdrawUser(userId, withdrawRequest);
        CookieUtil.deleteRefreshToken(response);

        return new ResponseEntity<>(Response.create(SUCCESS_WITHDRAW, null), SUCCESS_WITHDRAW.getHttpStatus());
    }
}
