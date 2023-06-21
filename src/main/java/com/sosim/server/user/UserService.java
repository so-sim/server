package com.sosim.server.user;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import com.sosim.server.participant.Participant;
import com.sosim.server.participant.ParticipantRepository;
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
    private final ParticipantRepository participantRepository;

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
    public void canWithdraw(long userId) {
        checkCanWithdraw(userId);
    }

    @Transactional
    public void withdrawUser(long userId, WithdrawRequest withdrawRequest) {
        checkCanWithdraw(userId);

        User user = getUser(userId);
        List<Participant> myParticipants = participantRepository.findByUserIdWithGroup(userId);

        user.delete(withdrawRequest.getWithdrawReason());
        myParticipants.forEach(Participant::withdrawGroup);
    }

    private void checkAlreadyExistUser(OAuthUserRequest oAuthUserRequest) {
        if (userRepository.findBySocialAndSocialId(
                oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.getOAuthId()).isPresent()) {
            throw new CustomException(USER_ALREADY_EXIST);
        }
    }

    private void checkCanWithdraw(long userId) {
        List<Participant> myParticipants = participantRepository.findByUserIdAndIsAdminIsTrue(userId);
        if (myParticipants.stream().anyMatch(Participant::isAdmin)) {
            throw new CustomException(CANNOT_WITHDRAWAL_BY_GROUP_ADMIN);
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
