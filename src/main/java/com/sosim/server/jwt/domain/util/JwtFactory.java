package com.sosim.server.jwt.domain.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtFactory {

    @Value("${jwt.access.key}")
    private String accessKey;

    @Value("${jwt.refresh.key}")
    private String refreshKey;

    @Value("${jwt.access.expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    private String encodeAccessKey;
    private String encodeRefreshKey;

    @PostConstruct
    protected void init(){
        encodeAccessKey = Base64.getEncoder().encodeToString(accessKey.getBytes());
        encodeRefreshKey = Base64.getEncoder().encodeToString(refreshKey.getBytes());
    }

    public String createAccessToken(Long userId){

        Claims claims = Jwts.claims().setSubject(userId.toString());

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessExpiration))
                .signWith(SignatureAlgorithm.HS256, encodeAccessKey)
                .compact();
    }

    public String createRefreshToken(){
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS256, encodeRefreshKey)
                .compact();
    }
}
