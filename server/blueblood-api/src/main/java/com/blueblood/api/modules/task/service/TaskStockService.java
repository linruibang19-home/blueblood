package com.blueblood.api.modules.task.service;

import com.blueblood.api.modules.task.entity.Task;
import com.blueblood.api.modules.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 任务名额的 Redis 库存服务(秒杀架构核心)。
 *
 * <p>接单流程:Redis Lua 原子预扣(内存层挡瞬时高并发,隔离 DB)→ DB 兜底扣(真实数据源)。
 * Redis 与 DB 最终一致;预扣成功后若 DB 扣失败/重复接单,调用方须 rollback 回补 Redis
 * (因为 @Transactional 只回滚 DB,不回滚 Redis)。</p>
 */
@Service
@RequiredArgsConstructor
public class TaskStockService {

    private final StringRedisTemplate redis;
    private final TaskMapper taskMapper;

    private static final String KEY_PREFIX = "task:slots:";

    /** Lua:原子"判断名额>0 + 扣减",返回 1=成功 0=满。Redis 单线程 + Lua 不可被打断 → 原子。 */
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT;
    static {
        DEDUCT_SCRIPT = new DefaultRedisScript<>();
        DEDUCT_SCRIPT.setScriptText(
                "local c = tonumber(redis.call('GET', KEYS[1]) or '0') " +
                "if c > 0 then redis.call('DECR', KEYS[1]); return 1 else return 0 end");
        DEDUCT_SCRIPT.setResultType(Long.class);
    }

    private String key(Long taskId) {
        return KEY_PREFIX + taskId;
    }

    /** 发布任务时初始化 Redis 名额。 */
    public void initStock(Long taskId, int slots) {
        redis.opsForValue().set(key(taskId), String.valueOf(slots));
    }

    /**
     * 原子预扣一个名额(Lua)。
     * Redis 无 key 时(重启/未初始化)从 DB slots_left 懒加载(setIfAbsent 防并发重复设值)。
     *
     * @return true=预扣成功,false=名额已满
     */
    public boolean tryDeduct(Long taskId) {
        String k = key(taskId);
        Boolean exists = redis.hasKey(k);
        if (exists == null || !exists) {
            Task t = taskMapper.selectById(taskId);
            int slots = t == null ? 0 : (t.getSlotsLeft() == null ? 0 : t.getSlotsLeft());
            // setIfAbsent:仅当 key 不存在时设置,防并发首加载互相覆盖
            redis.opsForValue().setIfAbsent(k, String.valueOf(slots));
        }
        Long r = redis.execute(DEDUCT_SCRIPT, Collections.singletonList(k));
        return r != null && r == 1L;
    }

    /** 回补一个名额(DB 扣失败/重复接单时,把 Redis 预扣的还回去)。 */
    public void rollback(Long taskId) {
        redis.opsForValue().increment(key(taskId));
    }
}
