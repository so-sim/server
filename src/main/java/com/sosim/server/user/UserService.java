package com.sosim.server.user;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.group.Group;
import com.sosim.server.group.GroupRepository;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import com.sosim.server.user.dto.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public User save(OAuthUserRequest oAuthUserRequest) {
        if (userRepository.findBySocialAndSocialId(
                oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.getOAuthId()).isPresent()) {
            throw new CustomException(ResponseCode.USER_ALREADY_EXIST);
        }

        return userRepository.save(User.create(oAuthUserRequest));
    }

    @Transactional
    public User update(OAuthUserRequest oAuthUserRequest) {
        User user = userRepository.findBySocialAndSocialId(
                oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.getOAuthId())
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_USER));

        user.setEmail(oAuthUserRequest.getEmail());
        return user;
    }

    public void checkCanWithdraw(Long id) {
        List<Group> groupList = groupRepository.findListByAdminId(id);
        for (Group group : groupList) {
            if (group.getParticipantList().stream().filter(p -> p.getStatus().equals(Status.ACTIVE)).count() > 1) {
                throw new CustomException(ResponseCode.CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
            }
        }
    }

    @Transactional
    public void withdrawUser(Long id, WithdrawRequest withdrawRequest) {
        checkCanWithdraw(id);
        User userEntity = getUserEntity(id);
        userEntity.delete(withdrawRequest.getWithdrawReason());
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_USER));
    }
}
