package com.alexeymirniy.simplewebflux.security;

import com.alexeymirniy.simplewebflux.entity.UserEntity;
import com.alexeymirniy.simplewebflux.exception.AuthException;
import com.alexeymirniy.simplewebflux.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SecurityService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public Mono<TokenDetails> authenticate(String username, String password) {

        return userService.getUserByUserName(username)
                .flatMap(user -> {
                    if(!user.isEnabled()) {
                        return Mono.error(new AuthException("Account disabled!", "USER_ACCOUNT_DISABLED"));
                    }
                    if(!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Invalid password!", "INVALID_PASSWORD"));
                    }

                    return Mono.just(generateToken(user).toBuilder()
                                    .userId(user.getId())
                                    .build());
                })
                .switchIfEmpty(Mono.error(new AuthException("User not found!", "USER_NOT_FOUND")));

    }

    private TokenDetails generateToken(UserEntity user) {

        Map<String, Object> claims = new HashMap<>() {{
            put("role", user.getRole());
            put("username", user.getUsername());
        }};
        return generateToken(claims, user.getId().toString());
    }

    private TokenDetails generateToken(Map<String, Object> claims, String subject) {

        long expirationTimeInMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);

        return generateToken(expirationDate, claims, subject);
    }

    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {

        Date createdDate = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
                .setExpiration(expirationDate)
                .signWith(
                        SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiredAt(expirationDate)
                .build();
    }
}