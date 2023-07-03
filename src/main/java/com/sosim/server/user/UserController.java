package com.sosim.server.user;

import com.sosim.server.common.resolver.AuthUserId;
import com.sosim.server.common.response.Response;
import com.sosim.server.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> withdrawUser(@AuthUserId long userId, @RequestParam("reason") String withdrawReason,
                                          HttpServletResponse response) {
        System.out.println(withdrawReason);
        userService.withdrawUser(userId, withdrawReason);
        CookieUtil.deleteRefreshToken(response);

        return new ResponseEntity<>(Response.create(SUCCESS_WITHDRAW, null), SUCCESS_WITHDRAW.getHttpStatus());
    }
}
