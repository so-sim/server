package com.sosim.server.user;

import com.sosim.server.common.auditing.BaseTimeEntity;
import com.sosim.server.common.auditing.Status;
import com.sosim.server.oauth.Social;
import com.sosim.server.oauth.dto.request.OAuthUserRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "USER")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOCIAL")
    private Social social;

    @Column(name = "SOCIAL_ID")
    private Long socialId;

    @Column(name = "WITHDRAW_REASON")
    private String withdrawReason;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, Social social, Long socialId) {
        this.email = email;
        this.social = social;
        this.socialId = socialId;
        status = Status.ACTIVE;
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
