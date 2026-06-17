package com.blueblood.api.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置（对应 application.yml 中 jwt.*）。
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥（按 UTF-8 字节直接作为 HMAC key，需 >= 32 字节）。生产务必覆盖。 */
    private String secret;

    /** 有效期（秒） */
    private long expireSeconds = 604800L;

    /** 签发者 */
    private String issuer = "blueblood";

    /** 请求头名称 */
    private String header = "Authorization";

    /** 令牌前缀 */
    private String tokenPrefix = "Bearer ";
}
