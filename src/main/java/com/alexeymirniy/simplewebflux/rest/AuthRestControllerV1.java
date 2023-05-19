package com.alexeymirniy.simplewebflux.rest;

import com.alexeymirniy.simplewebflux.dto.AuthRequestDto;
import com.alexeymirniy.simplewebflux.dto.AuthResponseDto;
import com.alexeymirniy.simplewebflux.dto.UserDto;
import com.alexeymirniy.simplewebflux.entity.UserEntity;
import com.alexeymirniy.simplewebflux.mapper.UserMapper;
import com.alexeymirniy.simplewebflux.security.CustomPrincipal;
import com.alexeymirniy.simplewebflux.security.SecurityService;
import com.alexeymirniy.simplewebflux.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final SecurityService securityService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody UserDto userDto) {

        UserEntity user = userMapper.map(userDto);

        return userService.registerUser(user)
                .map(userMapper::map);
    }

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto dto) {

        return securityService.authenticate(dto.getUsername(), dto.getPassword())
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDto.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiredAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserDto> getUserInfo(Authentication authentication) {

        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();

        return userService.getUserById(customPrincipal.getId())
                .map(userMapper::map);
    }
}