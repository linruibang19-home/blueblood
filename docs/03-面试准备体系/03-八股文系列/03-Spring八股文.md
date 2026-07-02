# 03 · Spring 八股文

> IoC / AOP / 事务 / Spring Security

---

## 一、IoC 控制反转

### 1.1 什么是 IoC？

**传统写法**：
```java
public class UserService {
    private UserMapper userMapper = new UserMapperImpl();  // 主动创建
}
```

**IoC 写法**：
```java
public class UserService {
    private UserMapper userMapper;  // 不主动创建，等待注入
    
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;  // 被动接收
    }
}
```

**IoC 的好处**：
1. **解耦**：对象不用自己创建，由容器创建
2. **易测试**：可以注入 Mock 对象
3. **易扩展**：换实现类只需要改配置

### 1.2 DI 依赖注入

| 注入方式 | 说明 | 优点 | 缺点 |
|---------|------|------|------|
| **构造器注入** | 通过构造函数注入 | 不可变，强制依赖 | 参数多时构造器很长 |
| **Setter 注入** | 通过 setter 方法注入 | 可选依赖灵活 | 可变，可能为 null |
| **字段注入** | 直接在字段上 @Autowired | 简洁 | 不易测试，违背单一职责 |

**项目中的选择**：构造器注入（@RequiredArgsConstructor）

```java
@Service
@RequiredArgsConstructor  // Lombok 自动生成构造器
public class TaskService {
    
    private final TaskMapper taskMapper;
    private final TaskStockService taskStockService;
    private final TaskListCacheService taskListCacheService;
    // ...
}
```

### 1.3 Bean 作用域

| 作用域 | 说明 | 适用场景 |
|--------|------|---------|
| **singleton** | 单例，整个应用只有一个 | 默认，绝大部分场景 |
| **prototype** | 原型，每次获取创建新实例 | 有状态 Bean |
| **request** | 每个请求创建一个 | Web 请求 |
| **session** | 每个会话创建一个 | Web 会话 |

**项目中的使用**：全部用默认的 singleton

```java
@Service  // 默认 singleton
public class TaskService { ... }

@Service
public class RateLimitAspect { ... }  // 单例 AOP 切面
```

### 1.4 Bean 生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                        Bean 生命周期                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 实例化 Bean                                                   │
│       ↓                                                          │
│  2. 属性填充（依赖注入）                                          │
│       ↓                                                          │
│  3. 初始化前回调（@PostConstruct）                               │
│       ↓                                                          │
│  4. 初始化（InitializingBean / @PostConstruct）                  │
│       ↓                                                          │
│  5. Bean 就绪                                                    │
│       ↓                                                          │
│  6. 销毁（@PreDestroy / DisposableBean）                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、AOP 面向切面编程

### 2.1 AOP 术语

| 术语 | 说明 | 项目中的例子 |
|------|------|------------|
| **Join Point** | 可被拦截的点（方法调用） | 所有 Controller 方法 |
| **Pointcut** | 切入点，匹配 Join Point | `@Around("@annotation(rateLimit)")` |
| **Advice** | 通知，增强逻辑 | before/after/around |
| **Aspect** | 切面，切入点 + 通知 | RateLimitAspect |
| **Weaving** | 织入，把切面织入目标对象 | 编译时/加载时/运行时 |

### 2.2 5 种通知类型

| 通知 | 说明 | 项目中的例子 |
|------|------|------------|
| **@Before** | 前置通知，执行前调用 | — |
| **@After** | 后置通知，执行后调用 | — |
| **@AfterReturning** | 返回后通知，正常返回时调用 | — |
| **@AfterThrowing** | 异常通知，抛异常时调用 | — |
| **@Around** | 环绕通知，包围目标方法 | RateLimitAspect |

### 2.3 项目中的 AOP：限流

```java
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final StringRedisTemplate redis;
    
    @Around("@annotation(rateLimit)")  // 切入点：标注了 @RateLimit 的方法
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        // 1. 解析限流 key
        String key = "rate:" + resolveKey(rateLimit, pjp);
        
        // 2. 执行限流检查
        Long allowed = redis.execute(SCRIPT, Collections.singletonList(key),
                String.valueOf(rateLimit.seconds()), String.valueOf(rateLimit.count()));
        
        // 3. 超限则抛异常
        if (allowed == null || allowed == 0L) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, rateLimit.message());
        }
        
        // 4. 执行目标方法
        return pjp.proceed();
    }
}
```

**使用方式**：
```java
@RateLimit(seconds = 60, count = 1, key = "#phone")
public Result<?> sendCode(@RequestParam String phone) {
    // 超过 60s/1次 会被限流拦截
}
```

### 2.4 代理方式

| 代理方式 | 原理 | 适用场景 |
|---------|------|---------|
| **JDK 动态代理** | 面向接口，代理接口 | 被代理类实现了接口 |
| **CGLIB 代理** | 继承子类，重写方法 | 被代理类没实现接口 |

**Spring 默认策略**：
- 有接口 → JDK 动态代理
- 无接口 → CGLIB 代理
- 可配置 `proxy-target-class="true"` 强制用 CGLIB

**项目中的配置**：
```java
@Configuration
@EnableAspectJAutoProxy  // 默认 proxy-target-class=false
public class WebMvcConfig { ... }
```

---

## 三、Spring 事务

### 3.1 @Transactional 属性

```java
@Transactional(
    propagation = Propagation.REQUIRED,    // 传播行为
    isolation = Isolation.DEFAULT,         // 隔离级别（默认 MySQL 的 REPEATABLE_READ）
    timeout = -1,                          // 超时时间（-1 不超时）
    readOnly = false,                      // 是否只读
    rollbackFor = Exception.class          // 哪些异常回滚
)
```

### 3.2 传播行为

| 传播行为 | 说明 | 项目中的例子 |
|---------|------|------------|
| **REQUIRED** | 有就用，没有就创建 | 默认，accept() 用这个 |
| **REQUIRES_NEW** | 每次都创建新事务 | 需要独立的地方 |
| **SUPPORTS** | 有就用，没有就不用 | — |
| **MANDATORY** | 必须有事务，没有抛异常 | — |
| **NOT_SUPPORTED** | 不在事务中执行 | 发送通知等 |
| **NEVER** | 不能在事务中，否则抛异常 | — |
| **NESTED** | 嵌套事务（SAVEPOINT） | — |

**项目中的例子**：
```java
@Transactional  // 默认 REQUIRED
public Map<String, Long> accept(Long taskId) {
    // 这个方法在一个事务里
    taskStockService.tryDeduct(taskId);  // Redis 操作，不归事务管
    orderMapper.insert(order);           // DB 操作，归事务管
    taskMapper.decrementSlotsLeft(taskId);
    // 任何异常都会回滚
}
```

### 3.3 事务失效场景

| 场景 | 原因 | 解决方案 |
|------|------|---------|
| 非 public 方法 | Spring AOP 限制 | 改成 public |
| 自调用 | 走的是 this，不是代理对象 | 注入自身或用 AopContext |
| 异常被 catch | 没有异常抛出，不回滚 | 重新抛出或配置 rollbackFor |
| 传播行为不对 | REQUIRED 内抛异常 | 改成 REQUIRES_NEW |

**项目中的关键问题：@Transactional 不回滚 Redis**

```java
@Transactional  // 只管 DB
public Map<String, Long> accept(Long taskId) {
    taskStockService.tryDeduct(taskId);  // Redis 操作，不会回滚！
    
    try {
        orderMapper.insert(order);
        taskMapper.decrementSlotsLeft(taskId);
    } catch (RuntimeException e) {
        taskStockService.rollback(taskId);  // ❗ 必须手动回补
        throw e;
    }
}
```

### 3.4 声明式 vs 编程式事务

| 方式 | 做法 | 适用场景 |
|------|------|---------|
| **声明式**（@Transactional） | 加注解，Spring 自动管理 | 绝大部分场景 |
| **编程式**（TransactionTemplate） | 手动 begin/commit/rollback | 复杂事务逻辑 |

**项目用声明式**：
```java
@Transactional  // 简洁
public Map<String, Long> accept(Long taskId) {
    // ...
}
```

---

## 四、Spring Security

### 4.1 认证流程

```
请求 → JwtAuthenticationFilter
           ↓
      解析 token
           ↓
      校验 token
           ↓
      存 SecurityContext
           ↓
      Controller 处理
```

**项目代码**（已在认证授权体系讲过）：
```java
// JwtAuthenticationFilter.java
if (StringUtils.hasText(token) && jwtUtil.isValid(token)) {
    LoginUser loginUser = jwtUtil.toLoginUser(token);
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

### 4.2 授权流程

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()  // 放行
    .requestMatchers("/admin/**").hasRole("ADMIN")  // ADMIN 才能访问
    .anyRequest().authenticated()  // 其余需要登录
)
```

### 4.3 密码加密

**项目代码**：
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // BCrypt 加密
}

// 使用
String encodedPassword = passwordEncoder.encode(rawPassword);  // 加密
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);  // 校验
```

---

## 五、面试必问问题

### Q1：Spring 的 IoC 是什么？

**标准回答**：
> IoC 是控制反转，把对象的创建和依赖管理交给 Spring 容器，而不是在代码里主动 new。好处是解耦、易测试、易扩展。我项目里用 @Service/@Component 标注 Bean，用构造器注入（@RequiredArgsConstructor）接收依赖。

### Q2：@Transactional 失效场景有哪些？

**标准回答**：
> 有几种：1. 非 public 方法，Spring AOP 限制。2. 自调用，走了 this 而不是代理对象。3. 异常被 catch 了，没有抛出来。4. 传播行为不对，比如 REQUIRED 内抛异常。另外 @Transactional 只管 DB，不管 Redis，我项目里处理秒杀时需要手动回补 Redis。

### Q3：AOP 的原理是什么？

**标准回答**：
> AOP 靠代理实现。Spring 默认有接口用 JDK 动态代理，没有接口用 CGLIB 代理。代理对象在调用目标方法前后加上增强逻辑。我的项目里 RateLimitAspect 用 @Around 拦截带 @RateLimit 注解的方法，用 Redis + Lua 实现限流。

### Q4：Spring Bean 生命周期是什么？

**标准回答**：
> 实例化 → 属性填充（依赖注入）→ 初始化前回调（@PostConstruct）→ 初始化（InitializingBean/@PostConstruct）→ Bean 就绪 → 销毁（@PreDestroy）。容器负责管理整个生命周期。

---

## 六、下一步

继续阅读：[04-JVM八股文.md](04-JVM八股文.md) — 内存模型/GC/调优