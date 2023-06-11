package com.sosim.server.user;

import com.sosim.server.common.advice.exception.CustomException;
import com.sosim.server.common.response.ResponseCode;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

    public User getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_USER));
    }
}
