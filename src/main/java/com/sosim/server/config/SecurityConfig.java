package com.sosim.server.config;

import com.sosim.server.jwt.util.JwtProvider;
import com.sosim.server.security.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // csrf
                .httpBasic().disable()
                .formLogin().disable()
                .headers().frameOptions().disable();

        http
                .cors();

        // 세션 STATELESS 설정
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 요청에 대한 권한 체크 파트
        http
                .authorizeRequests()
                .antMatchers("/**", "/api/group/{groupId}").permitAll()
                .antMatchers("/api/**").authenticated();

        http
                .addFilterBefore(new AuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}