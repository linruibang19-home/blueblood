# 15 · Redis 在项目里到底做了什么

> 目标：一次性搞清楚 Redis 在项目里的全部用途，不再只记得“用了 Redis”。

---

## 一、Redis 在项目里的定位

Redis 不是只用来“缓存”的。

在本项目中，Redis 承担了 5 类职责：

```text
1. 验证码临时存储
2. 任务抢单库存
3. 任务列表缓存
4. 接口限流计数
5. 幂等 token
```

也就是说：

```text
Redis = 临时数据 + 高并发控制 + 缓存 + 限流 + 幂等
```

---

## 二、Redis key 总览

| key 前缀 | 用途 | 示例 |
|---|---|---|
| `verify:*` | 验证码 | `verify:email:test@qq.com` |
| `task:slots:*` | 任务接单库存 | `task:slots:12` |
| `task:list:*` | 任务大厅列表缓存 | `task:list:hall:all::1:10` |
| `lock:task:list:*` | 任务列表缓存重建互斥锁 | `lock:task:list:hall:all::1:10` |
| `rate:*` | 接口限流计数 | `rate:AuthController:sendCode:phone` |
| `idem:*` | 幂等 token | `idem:publish:xxxx` |

---

## 三、验证码：verify:*

用途：

```text
邮箱/手机号注册时，保存验证码。
```

流程：

```text
用户请求发送验证码
→ 后端生成 6 位验证码
→ Redis 保存 verify:{type}:{target}
→ TTL 300 秒
→ 用户注册时校验验证码
```

为什么用 Redis？

```text
1. 验证码是临时数据
2. 天然需要过期时间
3. 不适合长期存 MySQL
4. Redis TTL 很方便
```

---

## 四、任务库存：task:slots:*

用途：

```text
任务接单时，做 Redis Lua 原子预扣库存。
```

key 示例：

```text
task:slots:12 = 3
```

表示：

```text
任务 ID=12 还有 3 个可接名额。
```

流程：

```text
发布任务时 initStock(taskId, slots)
接单时 tryDeduct(taskId)
失败时 rollback(taskId)
```

为什么用 Redis？

```text
任务接单是秒杀场景；
Redis 单线程 + Lua 可以保证判断和扣减原子；
比所有请求都打 MySQL 更能抗并发。
```

---

## 五、任务列表缓存：task:list:*

用途：

```text
缓存任务大厅列表，减少 DB 查询。
```

key 示例：

```text
task:list:hall:all::1:10
```

可能表示：

```text
任务大厅 hall 状态，全部分类，无关键词，第 1 页，每页 10 条。
```

缓存策略：

```text
查列表 → 先查 Redis
命中 → 直接返回
未命中 → 查 DB → 写 Redis
```

---

## 六、缓存重建锁：lock:task:list:*

用途：

```text
防缓存击穿。
```

场景：

```text
热点任务列表缓存刚好过期，100 个请求同时来了。
```

如果没有锁：

```text
100 个请求都打 DB。
```

有锁后：

```text
只有一个请求拿到锁去查 DB；
其他请求短暂等待，再读缓存。
```

实现：

```text
SETNX lock:task:list:{cacheKey} 1 EX 5
```

---

## 七、限流：rate:*

用途：

```text
限制验证码、登录等接口的访问频率。
```

key 示例：

```text
rate:AuthController:sendCode:13800138000
```

Lua 逻辑：

```text
INCR key
如果第一次访问，设置 EXPIRE
如果计数超过阈值，返回 0
否则返回 1
```

为什么用 Lua？

```text
INCR 和 EXPIRE 必须原子；
否则可能出现计数器没有过期时间的问题。
```

---

## 八、幂等 token：idem:*

用途：

```text
防发布任务重复提交。
```

key 示例：

```text
idem:publish:ea7f7a2dxxx = 1
```

流程：

```text
进入发布页 → 后端 issue token → Redis 存 10 分钟
提交任务 → 前端带 X-Idempotent-Token
后端 consume token → Redis DEL
DEL 返回 1：首次提交
DEL 返回 0：重复提交
```

为什么用 DEL？

```text
DEL 是原子操作；
第一次删除成功，后续再删返回 0；
天然适合一次性 token。
```

---

## 九、Redis 和 MySQL 的关系

在这个项目中：

```text
MySQL 是真实数据源；
Redis 是高性能辅助层。
```

比如任务库存：

```text
Redis 负责预扣，挡高并发；
MySQL 负责真实扣减，防止最终超卖。
```

如果 Redis 出问题：

```text
项目仍然可以用 DB 兜底，只是性能下降。
```

---

## 十、Redis 相关代码位置

| 功能 | 代码位置 |
|---|---|
| 验证码 | `VerificationCodeService` |
| 任务库存 | `TaskStockService` |
| 任务列表缓存 | `TaskListCacheService` |
| 限流 | `RateLimitAspect` |
| 幂等 token | `IdempotentService` |
| Redis 配置 | `RedisConfig` |

---

## 十一、学完本篇你应该能回答

```text
1. Redis 在项目里有哪些用途？
2. verify:* 是干什么的？
3. task:slots:* 是干什么的？
4. task:list:* 是干什么的？
5. lock:task:list:* 是干什么的？
6. rate:* 是干什么的？
7. idem:* 是干什么的？
8. Redis 和 MySQL 谁是真实数据源？
9. 为什么 Redis 失败时还需要 DB 兜底？
```
