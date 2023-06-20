package com.sosim.server.user;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import com.sosim.server.user.dto.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sosim.server.common.response.ResponseCode.*;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public User save(OAuthUserRequest oAuthUserRequest) {
        checkAlreadyExistUser(oAuthUserRequest);
        return userRepository.save(User.create(oAuthUserRequest));
    }

    @Transactional
    public User update(OAuthUserRequest oAuthUserRequest) {
        User user = getUser(oAuthUserRequest);

        user.changeEmail(oAuthUserRequest.getEmail());
        return user;
    }

    @Transactional(readOnly = true)
    public void checkCanWithdraw(long id) {
        //TODO: 쿼리로 로직 개선
        List<Group> groupList = groupRepository.findFetchJoinGroupByAdminId(id);
        for (Group group : groupList) {
            if (group.getParticipantList().size() > 1) {
                throw new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
            }
        }
    }

    @Transactional
    public void withdrawUser(long id, WithdrawRequest withdrawRequest) {
        checkCanWithdraw(id);

        User user = getUser(id);
        user.delete(withdrawRequest.getWithdrawReason());
    }

    private void checkAlreadyExistUser(OAuthUserRequest oAuthUserRequest) {
        if (userRepository.findBySocialAndSocialId(
                oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.getOAuthId()).isPresent()) {
            throw new CustomException(USER_ALREADY_EXIST);
        }
    }

    private User getUser(OAuthUserRequest oAuthUserRequest) {
        return userRepository.findBySocialAndSocialId(
                        oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.getOAuthId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    private User getUser(long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }
}
