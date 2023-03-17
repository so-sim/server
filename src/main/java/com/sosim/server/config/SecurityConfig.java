package com.sosim.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // csrf
                .headers().frameOptions().disable();

        // 세션 STATELESS 설정
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 요청에 대한 권한 체크 파트
        http
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .antMatchers("/api/**").authenticated();

        return http.build();
    }
}
