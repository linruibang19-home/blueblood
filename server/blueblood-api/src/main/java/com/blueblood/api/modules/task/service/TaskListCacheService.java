package com.blueblood.api.modules.task.service;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.modules.task.dto.TaskListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务大厅列表的 Redis 缓存(防击穿/穿透/雪崩)。
 * <ul>
 *   <li>防穿透:空结果也缓存(NULL_MARK,短 TTL),避免恶意查询反复打 DB</li>
 *   <li>防雪崩:正常结果随机 TTL(30~60s),避免缓存同时失效</li>
 *   <li>防击穿:互斥锁(tryLock),热点 key 过期时只让一个线程重建</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TaskListCacheService {

    private final RedisTemplate<String, Object> redis;
    private final StringRedisTemplate stringRedis;

    private static final String KEY_PREFIX = "task:list:";
    private static final String LOCK_PREFIX = "lock:task:list:";
    private static final String NULL_MARK = "__NULL__";
    private static final int BASE_TTL = 30;
    private static final int RANDOM_TTL = 30;
    private static final int NULL_TTL = 10;

    /** 取缓存:命中(含空值缓存)返回 PageResult,未命中返回 null。 */
    @SuppressWarnings("unchecked")
    public PageResult<TaskListItemVO> get(String cacheKey) {
        Object v = redis.opsForValue().get(KEY_PREFIX + cacheKey);
        if (v == null) {
            return null;
        }
        if (NULL_MARK.equals(v)) {
            return emptyPage();
        }
        return (PageResult<TaskListItemVO>) v;
    }

    /** 写缓存:empty=true 写空值标记(短 TTL 防穿透),否则随机 TTL 防雪崩。 */
    public void set(String cacheKey, PageResult<TaskListItemVO> data, boolean empty) {
        if (empty) {
            redis.opsForValue().set(KEY_PREFIX + cacheKey, NULL_MARK, Duration.ofSeconds(NULL_TTL));
        } else {
            int ttl = BASE_TTL + ThreadLocalRandom.current().nextInt(RANDOM_TTL);
            redis.opsForValue().set(KEY_PREFIX + cacheKey, data, Duration.ofSeconds(ttl));
        }
    }

    public boolean tryLock(String cacheKey) {
        Boolean ok = stringRedis.opsForValue().setIfAbsent(LOCK_PREFIX + cacheKey, "1", Duration.ofSeconds(5));
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String cacheKey) {
        stringRedis.delete(LOCK_PREFIX + cacheKey);
    }

    /** 清所有任务列表缓存(发布/编辑/下架后)。 */
    public void evictAll() {
        Set<String> keys = redis.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    private PageResult<TaskListItemVO> emptyPage() {
        return new PageResult<>(Collections.emptyList(), 0L, 1L, 10L);
    }
}
