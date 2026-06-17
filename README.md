# 蓝血菁英平台（Blue Blood Elite Platform）

> 面向 AI 技术人才、学生、自由职业者和企业需求方的**成长型任务撮合平台**：超级个体成长 + 任务接单交付 + 社区协作 + 收益结算，四端齐备（用户端 H5 / 后端 API / 后台管理 / 微信小程序）。

本仓库含**四个阶段**的完整交付：前台 H5、后端 API、后台管理端、微信小程序，全部容器化、可一键起栈。

---

## 一、产品业务架构

平台围绕「**超级个体成长 → 实战接单 → 收益闭环**」构建，四大业务域：

| 业务域 | 能力 |
|--------|------|
| **社区协作** | 兴趣小组、帖子、评论、点赞/收藏、活动报名、私信 |
| **成长学习** | 课程、章节、学习进度、作业提交与批改、黑客松、AI 岗位 |
| **任务撮合**（核心）| 任务大厅、分类筛选、确认接单（生成订单+里程碑）、里程碑提交→审核→进度流转→验收 |
| **收益结算** | 钱包、收益流水、待结算、提现申请→审核→打款、通知 |

### 核心业务流（任务交付闭环）
```
企业发布任务 → 用户接单(生成 task_order + 里程碑)
   → 提交里程碑(SUBMITTED) → 管理员审核(APPROVED/REJECTED)
   → 进度自动重算 → 全部通过 → 订单待验收 → 结算收益(入钱包)
   → 用户提现申请 → 管理员审核 → 打款
```

### 角色
- **普通用户（USER）**：浏览社区、学习课程、接单交付、收益提现。
- **管理员（ADMIN）**：后台运营——用户/认证审核、内容管理、任务/里程碑审核、收益结算、系统配置。

---

## 二、技术栈架构

### 四端技术栈

| 端 | 目录 | 技术栈 |
|----|------|--------|
| **前台 H5（用户端）** | `apps/user-web` | Vue3 + Vite + TypeScript + Vant4 + Pinia + Vue Router + Axios + Sass |
| **后台管理端** | `apps/admin-web` | Vue3 + Vite + TypeScript + Element Plus + Pinia + Vue Router |
| **后端 API** | `server/blueblood-api` | Spring Boot 3 + MyBatis-Plus + Spring Security(JWT) + MySQL8 + Redis + Knife4j + MapStruct + Lombok |
| **微信小程序** | `apps/miniapp` | uni-app + Vue3 + TypeScript + Pinia（mp-weixin） |

### 架构特点
- **单后端多前端**：`blueblood-api` 同时服务前台 H5、后台管理、小程序三端，统一 JWT 认证 + RBAC（`@PreAuthorize`）。
- **统一响应**：`{ code, message, data }`，code=0 成功；全局异常处理；分页 `{ list, total, page, pageSize, totalPages }`。
- **认证**：Spring Security + JWT（jjwt），角色多对多（`sys_role` + `sys_user_role`）。
- **数据库**：MySQL 8（utf8mb4/InnoDB），44+ 核心表，软删（`deleted_at`），金额 `DECIMAL(10,2)`，审计字段自动填充。
- **接口文档**：Knife4j / OpenAPI3，`/api/doc.html` 在线可测。
- **小程序**：复用同一后端，微信登录（`wx.login` → `/auth/wx-login`）、上传、订阅消息、分享。

### 16 个后端业务模块
`auth · user · profile · group · post · activity · chat · course · assignment · task · order · milestone · wallet · notification · file · system`（+ `admin` 后台管理接口层）

---

## 三、目录结构

```
bluebloodv2/
├── apps/
│   ├── user-web/        # 前台 H5（Vue3+Vant）
│   ├── admin-web/       # 后台管理端（Vue3+Element Plus）
│   └── miniapp/         # 微信小程序（uni-app）
├── server/
│   └── blueblood-api/   # 后端 API（Spring Boot 3）
├── sql/
│   ├── init.sql         # 建库 + 44+表 + 种子数据（库名 blueblood_v2）
│   └── v2_001_auth_user.sql  # 角色/认证扩展迁移
├── deploy/
│   ├── docker-compose.local.yml   # 阶段一：仅前台
│   └── docker-compose.full.yml    # 阶段二/三：api+mysql+redis+user-web+admin-web 五容器
├── docs/                # 各阶段开发任务文档（4 个 phase）
└── prototype/           # 客户原型 HTML
```

---

## 四、部署方案

### 方式 A：Docker 一键起栈（推荐，五容器）

**前置**：本机装 Docker Desktop（拉镜像需网络通畅，必要时配镜像加速器）。

```bash
# 1. 本地打后端 jar（精简运行时镜像，避免容器内 Maven 构建）
cd server/blueblood-api && mvn -q clean package -DskipTests && cd ../..

# 2. 起五容器全栈
docker compose -f deploy/docker-compose.full.yml up -d --build
```

启动后：

| 服务 | 地址 | 凭证 |
|------|------|------|
| 前台 H5 | http://localhost:3000 | lin / 123456 |
| 后台管理 | http://localhost:3001 | admin / 123456 |
| 后端 API | http://localhost:8090/api | - |
| 接口文档 | http://localhost:8090/api/doc.html | - |
| MySQL | localhost:3307 | root / blueblood |
| Redis | localhost:6380 | - |

> 容器名用 `blueblood-v2-*` 前缀，与遗留 `blueblood-php` 老栈共存不冲突。停止：`docker compose -f deploy/docker-compose.full.yml down`（加 `-v` 清数据）。

### 方式 B：本地原生开发（改代码即生效，调试用）

需要本机已装：JDK17、Maven、MySQL8、Redis、Node。

```bash
# 后端（连本机 MySQL/Redis）
cd server/blueblood-api
SERVER_PORT=8090 MYSQL_PASSWORD=<你的MySQL密码> mvn spring-boot:run

# 前台 H5（Mock 模式）
cd apps/user-web && npm install && npm run dev          # :3000，默认 Mock

# 前台 H5（接真实后端）
cd apps/user-web && npm run dev:api                     # VITE_USE_API=true，代理 /api→:8090

# 后台管理端
cd apps/admin-web && npm install && npm run dev         # :3001，代理 /api→:8090

# 小程序
cd apps/miniapp && npm install && npm run dev:mp-weixin # 产物 dist/dev/mp-weixin，用微信开发者工具打开
```

### 方式 C：小程序运行

```bash
cd apps/miniapp && npm run build:mp-weixin   # 产物 dist/build/mp-weixin
```
用**微信开发者工具**导入 `apps/miniapp/dist/build/mp-weixin` 运行。
- 未配置 AppID 时，微信登录走**演示桩**（发 lin 的 token）。
- 在 `application.yml` 配 `wx.appid` / `wx.secret` 后接真实 `jscode2session`（后端已留接入点）。

---

## 五、数据库

- 库名 **`blueblood_v2`**（与遗留 Laravel `blueblood` 库共存，互不影响）。
- `sql/init.sql`：建库 + 44+ 表 + 种子数据（含完整的 接单→里程碑→提交→审核 演示链）。
- `sql/v2_001_auth_user.sql`：角色多对多（`sys_role`/`sys_user_role`）+ 认证申请（`user_verification`）。
- 导入：`mysql -u root -p < sql/init.sql`（Docker 方式自动导入）。
- **演示账号**（密码统一 `123456`）：`lin / zhangming / lina / wangqiang`（用户）、`admin`（管理员）。

---

## 六、开发阶段路线（已完成 4/4）

| 阶段 | 内容 | 状态 |
|------|------|------|
| 一 | 前台 H5（21 页，原型复原） | ✅ |
| 二 | 后端 API（16 模块 + 前台联调 + Docker） | ✅ |
| 三 | 后台管理端（6 模块 + admin-web） | ✅ |
| 四 | 微信小程序（uni-app + 微信能力） | ✅ |

各阶段任务文档见 `docs/phase-{1..4}-*/`。

---

## 七、关键说明

- **前台 Mock/API 可切换**：`apps/user-web` 用 `VITE_USE_API` 开关，`npm run dev`（Mock）/ `npm run dev:api`（真实后端）。
- **RBAC**：所有 `/admin/*` 接口 `@PreAuthorize("hasRole('ADMIN')")`，普通用户越权返回 403。
- **软删**：全表 `deleted_at`，查询带 `deleted_at IS NULL` 过滤。
- **文件上传**：存本地 `uploads/`，URL 用 `ServletUriComponentsBuilder` 动态推导（适配任意端口/域名）。
- **操作日志**：`sys_operation_log` 表已就绪，切面接入留后续。
