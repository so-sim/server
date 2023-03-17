package com.sosim.server.user;

import com.sosim.server.oauth.Social;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private Social social;

    private Long socialId;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, Social social, Long socialId) {
        this.email = email;
        this.social = social;
        this.socialId = socialId;
    }
}
