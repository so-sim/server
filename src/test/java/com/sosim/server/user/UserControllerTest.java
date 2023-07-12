package com.sosim.server.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.security.WithMockCustomUser;
import com.sosim.server.security.WithMockCustomUserSecurityContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.sosim.server.common.response.ResponseCode.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class})
class UserControllerTest {

    static final String URI_PREFIX = "/api/user/withdraw";
    private long userId = WithMockCustomUserSecurityContextFactory.USER_ID;

    @MockBean
    @Autowired
    UserService userService;

    MockMvc mvc;

    @Autowired
    WebApplicationContext was;

    ObjectMapper om;

    @BeforeEach
    public void init() {
        mvc = MockMvcBuilders.webAppContextSetup(was).build();
        om = new ObjectMapper();
    }

    @WithMockCustomUser
    @DisplayName("회원 탈퇴 체크 / 성공 -> CAN_WITHDRAW")
    @Test
    void user_check_withdraw() throws Exception {
        //given
        doNothing().when(userService).canWithdraw(userId);

        //when
        ResultActions actions = mvc.perform(get(URI_PREFIX));

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(CAN_WITHDRAW.getCode()))
                .andExpect(jsonPath("$.status.message").value(CAN_WITHDRAW.getMessage()));
    }

    @WithMockCustomUser
    @DisplayName("회원 탈퇴 체크 / 실패 -> CANNOT_WITHDRAWAL_BY_GROUP_ADMIN")
    @Test
    void user_check_withdraw_fail() throws Exception {
        //given
        CustomException e = new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
        doThrow(e).when(userService).canWithdraw(userId);

        //when
        ResultActions actions = mvc.perform(get(URI_PREFIX));

        //then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN.getMessage()));
    }

    @WithMockCustomUser
    @DisplayName("회원 탈퇴 / 성공 -> SUCCESS_WITHDRAW")
    @Test
    void user_withdraw() throws Exception {
        //given
        String withdrawReason = "탈퇴 사유";

        doNothing().when(userService).withdrawUser(userId, withdrawReason);

        //when
        ResultActions actions = withdrawAction(withdrawReason);

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(SUCCESS_WITHDRAW.getCode()))
                .andExpect(jsonPath("$.status.message").value(SUCCESS_WITHDRAW.getMessage()));
    }

    @WithMockCustomUser
    @DisplayName("회원 탈퇴 / Admin 참가자 기록이 있는 경우 -> CANNOT_WITHDRAWAL_BY_GROUP_ADMIN")
    @Test
    void user_withdraw_has_admin_data() throws Exception {
        //given
        String withdrawReason = "탈퇴 사유";

        CustomException e = new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
        doThrow(e).when(userService).withdrawUser(userId, withdrawReason);

        //when
        ResultActions actions = withdrawAction(withdrawReason);

        //then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN.getCode()))
                .andExpect(jsonPath("$.status.message").value(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN.getMessage()));
    }

    @WithMockCustomUser
    @DisplayName("회원 탈퇴 / User가 없는 경우 -> NOT_FOUND_USER")
    @Test
    void user_withdraw_no_user() throws Exception {
        //given
        String withdrawReason = "탈퇴 사유";

        CustomException e = new CustomException(NOT_FOUND_USER);
        doThrow(e).when(userService).withdrawUser(userId, withdrawReason);

        //when
        ResultActions actions = withdrawAction(withdrawReason);

        //then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(NOT_FOUND_USER.getCode()))
                .andExpect(jsonPath("$.status.message").value(NOT_FOUND_USER.getMessage()));
    }

    private ResultActions withdrawAction(String withdrawReason) throws Exception {
        ResultActions actions = mvc.perform(delete(URI_PREFIX)
                .param("reason", withdrawReason));
        return actions;
    }

}