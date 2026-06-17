package com.blueblood.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具：生成、解析、校验令牌。基于 jjwt 0.12.x。
 */
@Slf4j
@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret 长度不足，需 >= 32 字节（当前 " + keyBytes.length + "）");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成令牌。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（如 USER/ADMIN）
     */
    public String generate(Long userId, String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getExpireSeconds());
        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /** 解析令牌，无效则抛 JwtException */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验令牌是否合法且未过期 */
    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return claims.getExpiration().after(Date.from(Instant.now()));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /** 从令牌还原登录用户 */
    public LoginUser toLoginUser(String token) {
        Claims claims = parse(token);
        Long userId = claims.get("userId", Number.class).longValue();
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        return new LoginUser(userId, username, role);
    }

    public long getExpireSeconds() {
        return props.getExpireSeconds();
    }
}
