package com.blueblood.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务：生成、存储(Redis)、校验。
 * 发送为桩：当前将验证码写入日志并返回(dev 便于联调)；
 * 生产环境应接 SMTP(邮件) / SMS 网关(短信)，不返回 code。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private static final long TTL_SECONDS = 300; // 5 分钟
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 生成并发送验证码（桩：返回 code 便于 dev） */
    public String sendCode(String target, String type) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(key(target, type), code, TTL_SECONDS, TimeUnit.SECONDS);
        log.info("[验证码桩][{}|{}] code={}", type, target, code);
        // TODO 生产：接 SMTP/SMS 真实发送，移除此 return
        return code;
    }

    /** 校验验证码（成功删除，防重放） */
    public boolean verify(String target, String type, String code) {
        String stored = redisTemplate.opsForValue().get(key(target, type));
        if (code != null && code.equals(stored)) {
            redisTemplate.delete(key(target, type));
            return true;
        }
        return false;
    }

    private String key(String target, String type) {
        return "verify:" + type + ":" + target;
    }
}
