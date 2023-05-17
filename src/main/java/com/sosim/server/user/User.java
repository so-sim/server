package com.sosim.server.user;

import com.sosim.server.oauth.Social;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "USER")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private Social social;

    private Long socialId;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, Social social, Long socialId) {
        this.email = email;
        this.social = social;
        this.socialId = socialId;
    }

    public static User create(OAuthUserRequest oAuthUserRequest) {
        return User.builder()
                .email(oAuthUserRequest.getEmail())
                .social(oAuthUserRequest.getOAuthSocial())
                .socialId(oAuthUserRequest.getOAuthId())
                .build();
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
