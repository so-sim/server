package com.sosim.server.user;

import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdate(OAuthUserRequest oAuthUserRequest) {
        User user = userRepository.findBySocialAndSocialId(
                oAuthUserRequest.getOAuthSocial(), oAuthUserRequest.OAuthId())
                .orElse(User.create(oAuthUserRequest));

        user.setEmail(oAuthUserRequest.getEmail());
        return user;
    }
}
