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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
}