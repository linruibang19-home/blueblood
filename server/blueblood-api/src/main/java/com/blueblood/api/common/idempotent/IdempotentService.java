package com.blueblood.api.common.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 一次性幂等 token 服务(防重复提交)。
 * issue 签发 token(Redis 标记存在,10min);consume 原子 DEL 消费(1=首次,0=重复)。
 * 适用于"发布任务"等无天然唯一约束的写操作,防止前端重复点击/网络重放。
 */
@Service
@RequiredArgsConstructor
public class IdempotentService {

    private final StringRedisTemplate redis;
    private static final String PREFIX = "idem:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private static final DefaultRedisScript<Long> DEL_SCRIPT;
    static {
        DEL_SCRIPT = new DefaultRedisScript<>();
        // DEL 返回删除数量:1=首次(存在并删除) 0=不存在(已消费/过期 → 重复)
        DEL_SCRIPT.setScriptText("return redis.call('DEL', KEYS[1])");
        DEL_SCRIPT.setResultType(Long.class);
    }

    /** 签发一次性 token。 */
    public String issue(String biz) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(PREFIX + biz + ":" + token, "1", TTL);
        return token;
    }

    /** 消费 token(原子 DEL):true=首次有效,false=重复/过期。未带 token 放行(向后兼容)。 */
    public boolean consume(String biz, String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }
        Long r = redis.execute(DEL_SCRIPT, Collections.singletonList(PREFIX + biz + ":" + token));
        return r != null && r == 1L;
    }
}
