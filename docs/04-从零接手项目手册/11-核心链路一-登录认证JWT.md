# 11 · 核心链路一：登录认证 JWT

> 目标：理解用户从登录到后续请求鉴权的完整链路。

---

## 一、这条链路解决什么问题？

系统需要知道：

```text
当前请求是谁发起的？
这个用户有没有登录？
这个用户有没有权限访问这个接口？
```

项目采用：

```text
Spring Security + JWT 无状态认证
```

---

## 二、登录链路总览

```text
前端登录页
→ POST /auth/login
→ AuthController.login
→ AuthService.login
→ 查询用户
→ BCrypt 校验密码
→ JwtUtil.generate 生成 token
→ 返回 LoginResponse
→ 前端保存 token
```

后续请求：

```text
前端请求时带 Authorization: Bearer token
→ JwtAuthenticationFilter 解析 token
→ JwtUtil.isValid 校验签名和过期
→ JwtUtil.toLoginUser 还原用户
→ 写入 SecurityContext
→ Controller/Service 中通过 SecurityUtils 获取当前用户
```

---

## 三、前端登录发生什么？

用户在前台或后台输入：

```text
username/password
```

前端发送：

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "lin",
  "password": "123456"
}
```

后端返回：

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOi...",
    "user": {
      "id": 1,
      "username": "lin",
      "role": "USER"
    }
  }
}
```

前端保存 token，后续请求自动带：

```http
Authorization: Bearer eyJhbGciOi...
```

---

## 四、后端核心文件

| 文件 | 作用 |
|---|---|
| `modules/auth/controller/AuthController.java` | 登录注册接口入口 |
| `modules/auth/service/AuthService.java` | 登录注册业务逻辑 |
| `security/JwtUtil.java` | 生成、解析、校验 JWT |
| `security/JwtAuthenticationFilter.java` | 每次请求解析 token |
| `security/SecurityUtils.java` | 获取当前登录用户 |
| `config/SecurityConfig.java` | 配置哪些接口放行、哪些接口要登录 |

---

## 五、JwtUtil 做什么？

### 生成 token

核心信息：

```text
userId
username
role
issuedAt
expiration
```

逻辑：

```text
JwtUtil.generate(userId, username, role)
→ 写入 claims
→ 设置过期时间
→ 使用密钥签名
→ 返回 token 字符串
```

### 校验 token

逻辑：

```text
JwtUtil.isValid(token)
→ parse token
→ 校验签名
→ 校验是否过期
→ 返回 true/false
```

### 还原用户

逻辑：

```text
JwtUtil.toLoginUser(token)
→ 从 claims 中取 userId/username/role
→ 组装 LoginUser
```

---

## 六、JwtAuthenticationFilter 做什么？

每次请求都会经过过滤器。

流程：

```text
1. 从请求头取 Authorization
2. 判断是否以 Bearer 开头
3. 截取 token
4. JwtUtil.isValid(token)
5. JwtUtil.toLoginUser(token)
6. 构造 UsernamePasswordAuthenticationToken
7. 加入角色 ROLE_USER / ROLE_ADMIN
8. 写入 SecurityContextHolder
9. 放行请求
```

核心意义：

```text
把“token 字符串”转换成 Spring Security 能识别的“已登录用户”。
```

---

## 七、SecurityConfig 做什么？

主要配置：

```text
1. 禁用 CSRF
2. 启用 CORS
3. Session 设置为 STATELESS
4. 放行 /auth/login、/auth/register、/health、/doc.html 等接口
5. 其他请求必须 authenticated
6. 配置 401/403 返回 JSON
7. 把 JwtAuthenticationFilter 放到过滤器链中
```

关键理解：

```text
项目不用服务端 Session，所有登录状态都依赖 JWT。
```

---

## 八、权限怎么判断？

### 第一层：是否登录

```text
.anyRequest().authenticated()
```

只要不是放行接口，都必须有合法 token。

### 第二层：角色权限

后台接口通常有：

```java
@PreAuthorize("hasRole('ADMIN')")
```

这要求当前用户有：

```text
ROLE_ADMIN
```

### 第三层：业务权限

有些权限不能只靠角色判断，比如：

```text
这个订单是不是你自己的？
这个任务是不是你发布的？
当前用户是不是企业用户？
```

这些在 Service 内部判断。

---

## 九、为什么用 JWT？

优点：

```text
1. 无状态，后端不需要保存 Session
2. 适合前台 H5、后台、小程序三端共用
3. 横向扩展简单
4. 请求只需要带 Authorization header
```

缺点：

```text
1. token 一旦签发，默认无法主动失效
2. 如果要真正登出，需要 Redis 黑名单
3. payload 不能放敏感信息，因为可以被解码
```

项目当前登出主要是前端清 token。

---

## 十、这条链路如何排错？

### 1. 登录返回 401

可能原因：

```text
账号密码错误
用户被禁用
请求路径写错
```

### 2. 后续请求 401

可能原因：

```text
token 没带
token 格式不是 Bearer xxx
token 过期
token 签名不正确
```

### 3. 后台请求 403

可能原因：

```text
用户登录了，但不是 ADMIN
@PreAuthorize 校验失败
```

---

## 十一、学完本篇你应该能回答

```text
1. 登录接口入口在哪里？
2. JWT 是在哪个类生成的？
3. 每次请求是在哪里解析 token 的？
4. SecurityContext 里存的是什么？
5. Service 怎么获取当前用户 ID？
6. 401 和 403 有什么区别？
7. 为什么项目不用 Session？
8. 为什么后台接口普通用户不能访问？
```
