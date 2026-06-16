package com.blueblood.api.modules.auth.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.auth.dto.LoginRequest;
import com.blueblood.api.modules.auth.dto.LoginResponse;
import com.blueblood.api.modules.auth.dto.RegisterRequest;
import com.blueblood.api.modules.auth.service.AuthService;
import com.blueblood.api.security.JwtProperties;
import com.blueblood.api.security.LoginUser;
import com.blueblood.api.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口：登录、注册、登出、当前用户。
 */
@Tag(name = "认证", description = "登录、注册、登出、当前用户")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "登录（真实校验：user 表 + BCrypt）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return Result.success(Map.of("userId", userId));
    }

    @Operation(summary = "登出（无状态：丢弃令牌；后续接 Redis 黑名单）")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        String token = StringUtils.hasText(header) && header.startsWith(jwtProperties.getTokenPrefix())
                ? header.substring(jwtProperties.getTokenPrefix().length()).trim() : null;
        authService.logout(token);
        return Result.success();
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public Result<LoginUser> me() {
        return Result.success(SecurityUtils.currentLoginUser());
    }
}
