# 蓝血菁英平台 · 前台 H5（user-web）

第一阶段交付物：基于 Vue3 + Vite + TypeScript + Vant 4 复原移动端 H5 原型，可在浏览器中点击演示。数据全部由本地 Mock 驱动，API 层封装好，后续可无缝替换为真实后端接口。

> 主题：浅色高级感设计，最大容器宽度 430px，针对 375 / 390 / 430 三档移动端宽度适配。

## 技术栈

- **Vue 3** `<script setup>` + **TypeScript**
- **Vite** 构建 / 开发服务器
- **Vue Router 4**（history 模式）
- **Pinia** 状态管理
- **Vant 4** 移动端组件库（按需自动引入）
- **Axios**（请求封装，第一阶段走 Mock）
- **Sass** + CSS 变量主题

## 本地开发

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:3000）
npm run dev
```

浏览器建议开启移动端调试模式（宽度 375 / 390 / 430）查看效果。

## 构建与预览

```bash
# 类型检查 + 生产构建（产物输出到 dist/）
npm run build

# 本地预览构建产物
npm run preview
```

## Docker 运行

项目提供生产级 Dockerfile（多阶段构建 + nginx 托管），在**项目根目录**执行：

```bash
docker compose -f deploy/docker-compose.local.yml up --build
```

启动后访问：<http://localhost:8080>

## 项目目录说明

```text
src/
├── api/              # 接口封装层：每个模块一组异步函数（内部走 Mock，含 delay 模拟网络）
├── components/       # 全局通用组件（SearchBar、UserAvatar、StatusBadge、ProgressBar 等）
├── layouts/          # 布局：MobileTabLayout（带底部 Tab 主框架）、SubPageLayout（子页带返回栏）
├── mock/             # 集中管理的 Mock 数据（users/groups/posts/courses/tasks/jobs/hackathons...）
├── pages/            # 业务页面，按四大模块分目录
│   ├── discover/     # 发现：首页、小组、帖子、超级个体、私信
│   ├── grow/         # 成长：课程、视频学习、作业、黑客松、岗位
│   ├── task/         # 任务：大厅、详情、执行、里程碑提交
│   └── mine/         # 我的：首页、钱包、通知、设置、编辑资料
├── router/           # 路由（21 条路由，含懒加载）
├── stores/           # Pinia store（user、ui）
├── styles/           # 全局样式：变量、基础、主题
└── types/            # TypeScript 类型定义
```

## Mock 数据说明

- 所有演示数据集中在 `src/mock/` 下，按模块拆分（如 `mock/users.ts`、`mock/tasks.ts`）。
- `src/api/` 中的每个函数都返回 `Promise`，内部用 `setTimeout` 模拟网络延迟，读取对应 Mock 文件。
- 页面统一在 `onMounted` 中调用 `api/*` 函数获取数据，**不直接 import mock**（仅个别列表页直接用 mock 常量做演示）。
- 因此数据流是：`页面 → api 函数 → mock 数据`，与真实请求结构一致。

## 后续接入后端说明

1. **切换数据源**：将 `src/api/*` 中每个函数的实现从「读 mock + delay」改为「调用 `request`（src/api/request.ts 封装的 axios 实例）」即可，函数签名与返回类型保持不变，页面无需改动。
2. **接口前缀**：`request.ts` 的 `baseURL` 默认 `/api`，可用环境变量 `VITE_API_BASE_URL` 覆盖。在项目根目录新建 `.env` / `.env.development`：

   ```text
   VITE_API_BASE_URL=https://your-api-host/api
   ```

3. **响应结构**：`request.ts` 已按 `{ code, message, data }` 统一响应体处理（`code` 为 0 或 200 视为成功，直接返回 `data`）。后端需遵守此约定。
4. **登录态**：请求拦截器自动携带 `localStorage` 中的 `token`（`Bearer` 头），后端接入认证后前端只需在登录成功时写入 `token`。
5. **Docker 联调**：nginx 已预留 `/api/` 反向代理段（见 `nginx.conf` 注释），后端服务就绪后取消注释并指向后端容器即可。
