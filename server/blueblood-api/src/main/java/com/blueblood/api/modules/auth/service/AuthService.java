package com.blueblood.api.modules.auth.service;

import com.blueblood.api.modules.auth.dto.LoginRequest;
import com.blueblood.api.modules.auth.dto.LoginResponse;
import com.blueblood.api.modules.auth.dto.RegisterRequest;
import com.blueblood.api.modules.auth.dto.WxLoginRequest;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.service.UserService;
import com.blueblood.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 认证服务：登录、注册、登出、微信小程序登录(桩)。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /** 微信小程序 AppID / AppSecret；未配置时 wxLogin 走演示桩(发 lin 的 token) */
    @Value("${wx.appid:}")
    private String wxAppId;

    @Value("${wx.secret:}")
    private String wxSecret;

    public LoginResponse login(LoginRequest req) {
        User user = userService.loginCheck(req.getUsername(), req.getPassword());
        return buildResponse(user.getId(), user.getUsername(), userService.primaryRoleCode(user.getId()));
    }

    public Long register(RegisterRequest req) {
        return userService.register(req);
    }

    /**
     * 微信小程序登录：wx.login 拿到 code → 后端换 token。
     * 桩逻辑：未配置 wx.appid/wx.secret 时，直接签发演示用户(lin)的 token，便于无 AppID 时联调；
     * 配置后应调用 https://api.weixin.qq.com/sns/jscode2session 用 code 换 openid/session_key，
     * 再按 openid 查/建 user 并签发其 token（此处留 TODO，按需接入）。
     */
    public LoginResponse wxLogin(WxLoginRequest req) {
        if (!StringUtils.hasText(wxAppId) || !StringUtils.hasText(wxSecret)) {
            // 未配置 → 演示桩：签发演示用户 token
            return buildResponse(1L, "lin", "USER");
        }
        // TODO 配置真实 AppID/Secret 后：用 req.getCode() 调 jscode2session 换 openid，
        //      再 userService.findOrCreateByOpenid(openid, req.getNickname(), req.getAvatar()) → 签发其 token
        return buildResponse(1L, "lin", "USER");
    }

    private LoginResponse buildResponse(Long userId, String username, String role) {
        String token = jwtUtil.generate(userId, username, role);
        Date expiration = new Date(System.currentTimeMillis() + jwtUtil.getExpireSeconds() * 1000L);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userId(userId)
                .username(username)
                .role(role)
                .build();
    }

    /**
     * 登出：当前为无状态，客户端丢弃令牌即可。
     * TODO 模块07/后续：将 token 加入 Redis 黑名单直到过期。
     */
    public void logout(String token) {
        // 预留：redisTemplate.opsForValue().set("jwt:blacklist:" + token, "1", expire, SECONDS);
    }
}
