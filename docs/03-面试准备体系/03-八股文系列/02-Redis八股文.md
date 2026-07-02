# 02 · Redis 八股文

> 数据结构 / 持久化 / 缓存问题 / 集群

---

## 一、Redis 数据结构

### 1.1 5 种基本数据结构

| 数据结构 | 命令示例 | 底层实现 | 适用场景 |
|---------|---------|---------|---------|
| **String** | `SET k v` `GET k` | SDS（简单动态字符串） | 缓存、计数器、分布式锁 |
| **Hash** | `HSET k f v` `HGET k f` | 压缩列表 / 哈希表 | 对象存储、购物车 |
| **List** | `LPUSH k v` `RPOP k` | 压缩列表 / 链表 | 消息队列、排行榜 |
| **Set** | `SADD k v` `SMEMBERS k` | 哈希表 / 整数集合 | 标签、好友关系、去重 |
| **ZSet** | `ZADD k score v` `ZRANGE k 0 -1` | 压缩列表 / 跳表 | 排行榜、延时队列 |

### 1.2 项目的使用场景

| 数据类型 | 项目场景 | Redis Key 示例 |
|---------|---------|---------------|
| **String** | 验证码存储 | `verify:email:xxx@xx.com` → `123456` |
| **String** | 秒杀名额 | `task:slots:123` → `5` |
| **String** | 幂等 Token | `idem:publish:uuid` → `1` |
| **String** | 限流计数 | `rate:TaskController:sendCode:phone` → `3` |
| **String** | 缓存数据 | `task:list:hall:all::1:10` → `PageResult JSON` |
| **String** | 互斥锁 | `lock:task:list:hall:all::1:10` → `1` |

### 1.3 SDS 简单动态字符串

**为什么 Redis 不用 C 字符串？**

| C 字符串 | SDS |
|---------|-----|
| 长度 O(N)，需要遍历 | 长度 O(1)，有 len 字段 |
| 容易缓冲区溢出 | 自动扩容，防溢出 |
| 修改 N 次要重新分配 N 次 | 空间预分配，减少分配 |
| 二进制不安全（遇到 \0 截断） | 二进制安全 |

**SDS 结构**：
```c
struct sdshdr {
    int len;      // 已用长度
    int free;     // 剩余空间
    char buf[];   // 实际存储
};
```

### 1.4 ZSet 跳表

**为什么 ZSet 用跳表而不是红黑树？**

| 对比 | 跳表 | 红黑树 |
|------|------|--------|
| 查找 | O(logN) | O(logN) |
| 范围查询 | O(logN + M) 简单 | O(logN + M) 复杂 |
| 插入删除 | 只改局部节点 | 需要旋转平衡 |
| 实现 | 简单 | 复杂 |
| 内存 | 稍高（每层指针） | 较低 |

**跳表结构**：
```
Level 2: ──────────►[50]────────────────────────►
Level 1: ───►[20]──►[50]──►[70]─────────────────►
Level 0: ───►[10]──►[20]──►[30]──►[50]──►[70]──►[90]──►
```

---

## 二、Redis 持久化

### 2.1 两种持久化方式

| 方式 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| **RDB** | 定时快照，fork 子进程生成 dump.rdb | 恢复快，文件紧凑 | 可能丢失最后一次快照后的数据 |
| **AOF** | 记录每个写命令到 .aof 文件 | 数据安全，可配置 | 文件大，恢复慢 |

### 2.2 RDB

**触发方式**：
1. 定时触发（配置 `save 900 1` 等）
2. `BGSAVE` 命令手动触发
3. `SAVE` 命令（阻塞）
4. `FLUSHALL` + `BGSAVE`（数据为空时不生成）

**原理**：
```
1. Redis 主进程 fork 子进程（copy-on-write）
2. 子进程遍历内存，生成 RDB 文件
3. 主进程继续处理请求
```

### 2.3 AOF

**配置**：
```conf
appendonly yes
appendfsync everysec  # always/everysec/no
```

**AOF 重写**：
- 随着命令越来越多，AOF 文件越来越大
- `BGREWRITEAOF` 合并重复命令，压缩文件

**三种策略**：

| 策略 | 说明 | 安全性 | 性能 |
|------|------|--------|------|
| always | 每个写命令都同步 | 最高 | 最差 |
| everysec | 每秒同步一次 | 中 | 中 |
| no | 由 OS 决定何时同步 | 低 | 最好 |

### 2.4 混合持久化（Redis 4.0+）

```conf
aof-use-rdb-preamble yes
```

**原理**：
- AOF 重写时，先写 RDB，再追加增量 AOF
- 恢复时先加载 RDB，再应用 AOF

**项目中的配置**：
```yaml
redis:
  host: redis
  port: 6379
  # 未配置持久化，容器重启数据丢失（可接受，因为任务名额从 DB 懒加载）
```

---

## 三、缓存问题

### 3.1 缓存穿透

**问题**：查询一个不存在的数据，每次都打到 DB。

```
请求 → Redis GET key = null → DB 查询 = 空 → 返回空
     → 下一个请求同样打到 DB → DB 被打垮 ❌
```

**解决方案**：

| 方案 | 做法 | 适用场景 |
|------|------|---------|
| **空值缓存** | 空结果也缓存，短 TTL | 数据确实很少为空 |
| **布隆过滤器** | 用布隆过滤器判断是否存在 | 数据量大，命中率低 |
| **参数校验** | 提前拦截无效参数 | 恶意查询 |

**项目中的实现**：
```java
// TaskListCacheService.java
private static final String NULL_MARK = "__NULL__";
private static final int NULL_TTL = 10;

public void set(String cacheKey, PageResult<TaskListItemVO> data, boolean empty) {
    if (empty) {
        // 空结果写 NULL_MARK，TTL=10秒
        redis.opsForValue().set(KEY_PREFIX + cacheKey, NULL_MARK, Duration.ofSeconds(10));
    }
}

public PageResult<TaskListItemVO> get(String cacheKey) {
    Object v = redis.opsForValue().get(KEY_PREFIX + cacheKey);
    if (NULL_MARK.equals(v)) {
        return emptyPage();  // 直接返回空，不打 DB
    }
}
```

### 3.2 缓存击穿

**问题**：热点 key 过期瞬间，大量请求同时打到 DB。

```
热点 key 在 T 秒过期
T 时刻：大量请求同时到达
       Redis GET = null
       DB 查询同时执行 → DB 被压垮 ❌
```

**解决方案**：

| 方案 | 做法 | 适用场景 |
|------|------|---------|
| **互斥锁** | 只让一个线程查 DB | 所有场景 |
| **热点数据永不过期** | 定期更新 + 逻辑过期 | 热点数据 |
| **逻辑过期** | 不真过期，过期了也返回旧数据 | 高并发热点数据 |

**项目中的实现**：
```java
public PageResult<TaskListItemVO> pageTasks(...) {
    // 1. 查缓存
    PageResult cached = taskListCacheService.get(cacheKey);
    if (cached != null) return cached;
    
    // 2. 互斥锁
    boolean locked = taskListCacheService.tryLock(cacheKey);
    if (!locked) {
        // 没抢到锁，短暂等待后重试
        Thread.sleep(60);
        cached = taskListCacheService.get(cacheKey);
        if (cached != null) return cached;
    }
    
    try {
        // 3. 抢到锁的线程查 DB
        cached = queryTasksFromDb(...);
        taskListCacheService.set(cacheKey, cached, false);
        return cached;
    } finally {
        if (locked) taskListCacheService.unlock(cacheKey);
    }
}
```

### 3.3 缓存雪崩

**问题**：大量 key 同时过期，导致大量请求打到 DB。

```
同一时刻：大量 key TTL 同时到期
       Redis 大量 key 失效
       DB 同时收到大量请求 → DB 被压垮 ❌
```

**解决方案**：

| 方案 | 做法 |
|------|------|
| **随机 TTL** | 正常结果加随机 TTL（±10~30秒） |
| **热点数据永不过期** | 热点数据不设 TTL |
| **多级缓存** | 本地缓存 + Redis + DB |
| **限流熔断** | 请求过多时拒绝服务 |

**项目中的实现**：
```java
private static final int BASE_TTL = 30;
private static final int RANDOM_TTL = 30;

public void set(String cacheKey, PageResult data, boolean empty) {
    if (empty) {
        redis.opsForValue().set(KEY_PREFIX + cacheKey, NULL_MARK, Duration.ofSeconds(10));
    } else {
        // 随机 TTL：30~60秒
        int ttl = BASE_TTL + ThreadLocalRandom.current().nextInt(RANDOM_TTL);
        redis.opsForValue().set(KEY_PREFIX + cacheKey, data, Duration.ofSeconds(ttl));
    }
}
```

### 3.4 缓存三防总结

```
┌─────────────────────────────────────────────────────────────────┐
│                       缓存三防体系                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  穿透：查询不存在的数据                                          │
│  ├─ 原因：恶意查询 / 数据确实为空                                │
│  ├─ 后果：每次都打 DB                                           │
│  └─ 解决：空值缓存（NULL_MARK，短 TTL）                         │
│                                                                  │
│  击穿：热点 key 过期瞬间                                         │
│  ├─ 原因：热点数据突然过期                                       │
│  ├─ 后果：大量请求同时打 DB                                      │
│  └─ 解决：互斥锁（tryLock，只一个线程查 DB）                    │
│                                                                  │
│  雪崩：大量 key 同时过期                                         │
│  ├─ 原因：TTL 设置相同，同时到期                                 │
│  ├─ 后果：大量请求同时打 DB                                      │
│  └─ 解决：随机 TTL ±30秒                                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 四、Redis 过期策略

### 4.1 过期 key 删除策略

| 策略 | 说明 | 优点 | 缺点 |
|------|------|------|------|
| **定时删除** | 定时器扫描，过期就删 | 内存友好 | CPU 压力大 |
| **惰性删除** | 访问时检查，过期就删 | CPU 友好 | 内存可能泄漏 |
| **定期删除** | 定期扫描，删除过期 key | 折中 | 实现复杂 |

**Redis 用的是惰性删除 + 定期删除**。

### 4.2 内存淘汰策略

当内存达到上限（`maxmemory`）时：

| 策略 | 说明 |
|------|------|
| `noeviction` | 不淘汰，返回错误（默认） |
| `volatile-lru` | LRU 删除过期 key |
| `volatile-ttl` | 删除 TTL 最短的 |
| `volatile-random` | 随机删除过期 key |
| `allkeys-lru` | LRU 删除所有 key |
| `allkeys-random` | 随机删除所有 key |

**项目中的配置**：
```yaml
redis:
  host: redis
  port: 6379
  # 未设置 maxmemory，用默认（不限制）
```

---

## 五、Redis 集群

### 5.1 主从复制

```
┌──────────┐
│  Master  │ ← 写
└────┬─────┘
     │ 同步
     ▼
┌──────────┐
│  Slave1  │ ← 读
└──────────┘
     │
     ▼
┌──────────┐
│  Slave2  │ ← 读
└──────────┘
```

**原理**：
1. Slave 向 Master 发送 `SYNC` 命令
2. Master 执行 `BGSAVE`，生成 RDB
3. Master 发送 RDB 给 Slave
4. Master 发送缓冲区的写命令给 Slave
5. 之后 Master 每条写命令都发送给 Slave

### 5.2 哨兵（Sentinel）

**作用**：
1. 监控主从节点是否存活
2. 自动故障转移（主节点挂了，选一个从节点当主节点）
3. 通知客户端新主节点地址

### 5.3 Cluster

**数据分片**：
- 16384 个 slot
- 每个节点负责一部分 slot
- `key` 的 slot = `CRC16(key) % 16384`

```
Slot 0~5460      → Node1
Slot 5461~10922  → Node2
Slot 10923~16383 → Node3
```

### 5.4 项目中的选择

项目用的是**单机 Redis**：

```yaml
redis:
  host: redis
  port: 6379
  # 单机，未配置集群
```

**为什么单机够用？**
- 项目是实习项目，并发量不大
- 单机 Redis 足够支撑秒杀、限流、缓存等场景
- 任务名额从 DB 懒加载，不怕 Redis 重启丢失

**如果要升级到集群？**
1. 换用 Redis Cluster
2. key 的 slot 分配到不同节点
3. 某些 Lua 脚本需要改（涉及多个 key）

---

## 六、面试必问问题

### Q1：Redis 的持久化机制是什么？

**标准回答**：
> Redis 有两种持久化：RDB 和 AOF。RDB 是定时快照，fork 子进程生成 dump.rdb 文件，恢复快但可能丢数据。AOF 是记录每个写命令到 .aof 文件，数据安全但文件大。Redis 4.0 支持混合持久化，先写 RDB 再追加 AOF。

### Q2：缓存穿透、击穿、雪崩是什么？怎么解决？

**标准回答**：
> 穿透是查不存在的数据每次都打 DB，用空值缓存解决。击穿是热点 key 过期瞬间大量请求打 DB，用互斥锁解决。雪崩是大量 key 同时过期，用随机 TTL 解决。

### Q3：Redis 的过期策略是什么？

**标准回答**：
> Redis 用惰性删除 + 定期删除。惰性删除是访问 key 时检查是否过期，过期就删，CPU 友好但内存可能泄漏。定期删除是每 100ms 随机检查一部分 key，删除过期的。内存满了用淘汰策略，默认不淘汰返回错误。

### Q4：Redis 和 MySQL 怎么保持一致？

**标准回答**：
> 有几种方案：1. 先删缓存再更新数据库，可能有短暂不一致。2. 先更新数据库再删缓存，更常用。3. 延迟双删，先删缓存→更新DB→再删缓存，适合分布式。4. 订阅 Canal binlog，异步同步。我项目里用的是先更新DB再删缓存，配合 try-catch 回补保证一致性。

### Q5：Redis 分布式锁怎么实现？

**标准回答**：
> 用 `SET key value NX PX timeout` 原子设置，value 要用唯一标识（如 UUID），解锁用 Lua 脚本判断 value 是否相同再删除。NX 保证原子性，PX 设置过期时间防死锁。

---

## 七、下一步

继续阅读：[03-Spring八股文.md](03-Spring八股文.md) — IoC/AOP/事务/安全