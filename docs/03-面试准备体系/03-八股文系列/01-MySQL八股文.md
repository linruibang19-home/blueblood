# 01 · MySQL 八股文

> 索引 / 事务 / 锁 / SQL 优化

---

## 一、MySQL 索引（最重要！）

### 1.1 索引的数据结构：B+ 树

**为什么不用其他结构？**

| 结构 | 查找效率 | 范围查询 | 磁盘 IO |
|------|---------|---------|---------|
| 二叉树 | O(logN) | 不支持 | 多 |
| 红黑树 | O(logN) | 不支持 | 多 |
| **B 树** | O(logN) | 支持 | 中 |
| **B+ 树** | O(logN) | **高效支持** | **少** |

**MySQL 为什么选 B+ 树？**
1. **矮胖**：多叉树，高度低，磁盘 IO 少
2. **范围查询友好**：叶子节点用双向链表连接
3. **查询稳定**：所有数据都在叶子节点，非叶子节点只存索引

### 1.2 B+ 树结构

```
                        ┌─────────────────┐
                        │    非叶子节点    │  ← 只存索引（主键+指向）
                        │  (1) │ (50) │   │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
        ┌─────────┐        ┌─────────┐        ┌─────────┐
        │ 叶子节点 │        │ 叶子节点 │        │ 叶子节点 │
        │ (1~25)  │        │ (26~50) │        │ (51~75) │
        │         │◄──────►│         │◄──────►│         │  ← 双向链表
        │ 数据页  │        │ 数据页  │        │ 数据页  │
        └─────────┘        └─────────┘        └─────────┘
```

**特点**：
- 叶子节点存储完整数据（聚簇索引）或指向数据的指针（非聚簇索引）
- 非叶子节点只存索引，不存数据，所以更瘦高
- 叶子节点之间用双向链表连接，支持范围查询

### 1.3 聚簇索引 vs 非聚簇索引

| 类型 | 数据存储 | 特点 | 适用 |
|------|---------|------|------|
| **聚簇索引** | 叶子节点存储完整行数据 | 查询快，更新慢 | 主键 |
| **非聚簇索引** | 叶子节点存储主键值 | 查询需要回表 | 普通索引 |

**项目中的例子**：

```sql
-- task 表，主键是 id（聚簇索引）
PRIMARY KEY (id)

-- uk_order_pair 唯一索引（非聚簇索引）
UNIQUE INDEX uk_order_pair (task_id, user_id, deleted_at)

-- deleted_at 普通索引（查询时手动过滤）
INDEX idx_deleted (deleted_at)
```

### 1.4 最左前缀原则

**什么是最左前缀原则？**

对于联合索引 `(a, b, c)`，索引生效的条件是：

| 查询条件 | 索引是否生效 | 原因 |
|---------|-------------|------|
| `WHERE a = ?` | ✅ | 用了最左前缀 |
| `WHERE a = ? AND b = ?` | ✅ | 用了最左前缀 |
| `WHERE a = ? AND b = ? AND c = ?` | ✅ | 全部用了 |
| `WHERE b = ?` | ❌ | 没用最左前缀 |
| `WHERE c = ?` | ❌ | 没用最左前缀 |
| `WHERE b = ? AND c = ?` | ❌ | 没用最左前缀 |
| `WHERE a > ?` | ✅ | 范围查询左前缀生效 |
| `WHERE a = ? AND c = ?` | ✅ 部分生效 | a 生效，c 不生效（中断） |

**项目中的例子**：

```sql
-- 联合索引 (task_id, user_id, deleted_at)
-- 能命中的查询：
WHERE task_id = 1
WHERE task_id = 1 AND user_id = 1
WHERE task_id = 1 AND user_id = 1 AND deleted_at IS NULL

-- 不能命中的查询：
WHERE user_id = 1
WHERE deleted_at IS NULL
WHERE user_id = 1 AND deleted_at IS NULL
```

### 1.5 索引失效场景

| 场景 | 例子 | 是否失效 |
|------|------|---------|
| 以 % 开头的 LIKE | `LIKE '%abc'` | ❌ 失效 |
| 以 % 结尾的 LIKE | `LIKE 'abc%'` | ✅ 生效 |
| 两边都有 % | `LIKE '%abc%'` | ❌ 失效 |
| 使用 OR | `WHERE a = ? OR b = ?` | ⚠️ 可能失效 |
| 使用函数 | `WHERE YEAR(date) = 2024` | ❌ 失效 |
| 使用计算 | `WHERE price + 1 = 100` | ❌ 失效 |
| 类型不匹配 | `WHERE id = '123'` (id 是 int) | ❌ 可能失效 |
| NOT | `WHERE status != 'APPROVED'` | ❌ 失效 |

### 1.6 项目中的索引设计

```sql
-- 任务表索引
CREATE TABLE task (
    id BIGINT PRIMARY KEY,                        -- 聚簇索引
    employer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL,
    slots_left INT NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    
    UNIQUE INDEX uk_order_pair (task_id, user_id, deleted_at),  -- 防重复接单
    INDEX idx_status_deleted (status, deleted_at),               -- 任务大厅筛选
    INDEX idx_category_deleted (category, deleted_at),           -- 按分类筛选
    INDEX idx_created_deleted (created_at, deleted_at)           -- 按时间排序
);

-- 钱包流水表索引
CREATE TABLE wallet_record (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    biz_type VARCHAR(50),                        -- 业务类型（task）
    biz_id BIGINT,                               -- 业务ID（里程碑ID）
    type VARCHAR(20),                            -- income/withdraw
    
    INDEX idx_user_biz (user_id, biz_type, biz_id, type),  -- 幂等查重
    INDEX idx_created (created_at)                           -- 按时间查询
);
```

---

## 二、事务隔离级别

### 2.1 四种隔离级别

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 说明 |
|---------|------|----------|------|------|
| **READ UNCOMMITTED** | ❌ 可能 | ❌ 可能 | ❌ 可能 | 最低性能，最高隔离 |
| **READ COMMITTED** | ✅ 不可能 | ❌ 可能 | ❌ 可能 | Oracle 默认 |
| **REPEATABLE READ** | ✅ 不可能 | ✅ 不可能 | ❌ 可能（MySQL InnoDB 有间隙锁） | MySQL 默认 |
| **SERIALIZABLE** | ✅ 不可能 | ✅ 不可能 | ✅ 不可能 | 最高隔离，性能最差 |

**MySQL 默认是 REPEATABLE READ**。

### 2.2 MySQL 是怎么实现的？

**MVCC（Multi-Version Concurrency Control）**

```
┌─────────────────────────────────────────────────────────────────┐
│                         MVCC 工作原理                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  每个事务启动时生成一个"快照"                                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  undo log 链                                            │   │
│  │                                                          │   │
│  │  ┌─────┐    ┌─────┐    ┌─────┐                        │   │
│  │  │ v1  │───►│ v2  │───►│ v3  │  ← 当前版本           │   │
│  │  └─────┘    └─────┘    └─────┘                        │   │
│  │   历史      历史       当前                             │   │
│  │                                                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  读取时：根据事务ID，判断能看到哪个版本                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**ReadView 包含**：
- `m_ids`：活跃事务 ID 列表
- `min_trx_id`：最小活跃事务 ID
- `max_trx_id`：创建 ReadView 时最大事务 ID
- `creator_trx_id`：当前事务 ID

**判断规则**：
- 数据行的事务ID < min_trx_id → 可见（已提交）
- 数据行的事务ID > max_trx_id → 不可见（未开始）
- 数据行的事务ID 在 m_ids 中 → 不可见（未提交）
- 其他 → 可见

### 2.3 项目中的事务隔离级别

**MySQL 默认 REPEATABLE READ**，项目没有显式设置。

**项目中涉及并发的场景**：

```java
// 1. 任务接单（秒杀）
@Transactional  // 默认 REPEATABLE READ
public Map<String, Long> accept(Long taskId) {
    // Redis Lua 预扣名额
    // 创建订单
    // DB 原子扣名额（SELECT ... FOR UPDATE 或原子 UPDATE）
}

// 2. 钱包入账
@Transactional
public void credit(Long userId, BigDecimal amount) {
    // 幂等查重
    // 原子加余额
    // 写钱包流水
}

// 3. 提现
@Transactional
public void withdraw(Long userId, BigDecimal amount) {
    // 原子扣余额（WHERE balance >= amount）
    // 创建提现记录
}
```

---

## 三、MySQL 锁

### 3.1 锁的分类

```
锁
├── 按类型分
│   ├── 共享锁（S锁）SELECT ... LOCK IN SHARE MODE
│   └── 排他锁（X锁）SELECT ... FOR UPDATE / UPDATE / DELETE
│
├── 按粒度分
│   ├── 表锁   LOCK TABLE xxx READ/WRITE
│   ├── 行锁   （InnoDB）SELECT ... FOR UPDATE
│   └── 间隙锁 （InnoDB）锁区间，防止幻读
│
└── 按特性分
    ├── 悲观锁  假设冲突，先加锁再操作
    └── 乐观锁  假设无冲突，通过版本号控制
```

### 3.2 项目中的锁

**原子 UPDATE（隐式锁）**：

```java
// 钱包原子加余额（不需要显式加锁）
@Update("UPDATE wallet_account SET balance = balance + #{amount} WHERE user_id = #{userId}")
int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

// 任务名额原子扣减
@Update("UPDATE task SET slots_left = slots_left - 1 WHERE id = #{taskId} AND slots_left > 0")
int decrementSlotsLeft(@Param("taskId") Long taskId);
```

**为什么不用 SELECT FOR UPDATE？**

因为 UPDATE 语句本身就会加排他锁，而且是行锁 + WHERE 条件 = 范围更精准。

### 3.3 死锁

**死锁条件**：
1. 互斥：资源只能被一个事务持有
2. 持有并等待：持有资源的同时请求其他资源
3. 不可抢占：资源不能被强制释放
4. 循环等待：事务之间循环等待资源

**项目中的死锁风险**：

```java
// 场景：同时给同一个用户转账
// 事务1：A→B 转账
UPDATE wallet_account SET balance = balance - 100 WHERE user_id = A;  // 锁住 A
UPDATE wallet_account SET balance = balance + 100 WHERE user_id = B;  // 锁住 B

// 事务2：B→A 转账
UPDATE wallet_account SET balance = balance - 100 WHERE user_id = B;  // 锁住 B
UPDATE wallet_account SET balance = balance + 100 WHERE user_id = A;  // 等待 A...

// 可能死锁！事务1 持有 A 等待 B，事务2 持有 B 等待 A
```

**解决方案**：
1. 按固定顺序加锁（A→B 永远比 B→A 先）
2. 减少锁持有时间
3. 设置锁等待超时

---

## 四、SQL 优化

### 4.1 EXPLAIN 分析

```sql
EXPLAIN SELECT * FROM task WHERE status = 'APPROVED' AND deleted_at IS NULL;

-- 结果字段：
-- id: 查询序号
-- select_type: 查询类型（SIMPLE/PRIMARY/SUBQUERY）
-- type: 访问类型（ALL < index < range < ref < eq_ref < const）
-- key: 实际使用的索引
-- rows: 扫描行数（估算）
-- Extra: 额外信息（Using index/Using where/Using filesort）
```

### 4.2 慢查询优化步骤

```
1. 开启慢查询日志
   SET GLOBAL slow_query_log = 'ON';
   SET GLOBAL long_query_time = 1;  -- 超过1秒记录

2. 分析慢查询日志
   mysqldumpslow -s t /var/log/mysql/slow.log

3. 使用 EXPLAIN 分析
   EXPLAIN SQL;

4. 优化
   - 加索引
   - 改写 SQL
   - 分表
```

### 4.3 项目中的 SQL 优化

**分页查询优化**：

```java
// ❌ 低效：OFFSET 太大时性能差
SELECT * FROM task WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 100000, 10;

// ✅ 高效：基于 ID 分页
SELECT * FROM task WHERE id < #{lastId} AND deleted_at IS NULL 
ORDER BY created_at DESC LIMIT 10;
```

**索引覆盖**：

```java
// ❌ 需要回表
SELECT * FROM task WHERE id = ?;

// ✅ 覆盖索引，不需要回表
SELECT id, title, status FROM task WHERE id = ?;
```

---

## 五、面试必问问题

### Q1：索引的数据结构是什么？为什么用 B+ 树？

**标准回答**：
> MySQL 索引用的是 B+ 树。B+ 树是多叉平衡树，非叶子节点只存索引，叶子节点存数据且用双向链表连接。相比二叉树，B+ 树更矮胖，磁盘 IO 少；相比 B 树，B+ 树范围查询更高效，因为叶子节点链表连接。

### Q2：聚簇索引和非聚簇索引的区别？

**标准回答**：
> 聚簇索引叶子节点存完整行数据，非聚簇索引叶子节点存主键值。查询聚簇索引可以直接拿到数据，非聚簇索引需要回表（根据主键再查一次）。一个表只能有一个聚簇索引（主键），可以有多个非聚簇索引。

### Q3：事务隔离级别有哪些？MySQL 默认是哪个？

**标准回答**：
> 有四种：读未提交、读已提交、可重复读（MySQL 默认）、串行化。MySQL 用 MVCC 实现可重复读，通过 ReadView 判断能看到哪个版本的数据。读已提交每次读取都生成新 ReadView，可重复读只在事务开始时生成 ReadView。

### Q4：什么是幻读？怎么解决？

**标准回答**：
> 幻读是一个事务内，两次相同查询结果不同（因为其他事务插入了新行）。MySQL InnoDB 在可重复读下用间隙锁解决幻读：锁定索引区间，不允许其他事务插入新行。

### Q5：SQL 怎么优化？

**标准回答**：
> 1. 用 EXPLAIN 分析执行计划，看 type/rows/Extra。2. 确保查询命中索引，避免全表扫描（type=ALL）。3. 避免 SELECT *，用覆盖索引。4. 大分页用 ID 分页替代 OFFSET。5. 避免在索引列上使用函数或计算。

---

## 六、下一步

继续阅读：[02-Redis八股文.md](02-Redis八股文.md) — Redis 数据结构、持久化、缓存问题