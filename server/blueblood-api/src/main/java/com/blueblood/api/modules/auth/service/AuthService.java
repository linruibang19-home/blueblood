package com.blueblood.api.modules.auth.service;

import com.blueblood.api.modules.auth.dto.LoginRequest;
import com.blueblood.api.modules.auth.dto.LoginResponse;
import com.blueblood.api.modules.auth.dto.RegisterRequest;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.service.UserService;
import com.blueblood.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 认证服务：登录、注册、登出。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest req) {
        User user = userService.loginCheck(req.getUsername(), req.getPassword());
        String role = userService.primaryRoleCode(user.getId());
        String token = jwtUtil.generate(user.getId(), user.getUsername(), role);

        Date expiration = new Date(System.currentTimeMillis() + jwtUtil.getExpireSeconds() * 1000L);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userId(user.getId())
                .username(user.getUsername())
                .role(role)
                .build();
    }

    public Long register(RegisterRequest req) {
        return userService.register(req);
    }

    /**
     * 登出：当前为无状态，客户端丢弃令牌即可。
     * TODO 模块07/后续：将 token 加入 Redis 黑名单直到过期。
     */
    public void logout(String token) {
        // 预留：redisTemplate.opsForValue().set("jwt:blacklist:" + token, "1", expire, SECONDS);
    }
}
