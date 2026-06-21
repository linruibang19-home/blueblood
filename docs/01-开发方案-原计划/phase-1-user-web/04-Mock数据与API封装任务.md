# 04-Mock数据与API封装任务

## 任务目标

将原型中的硬编码业务内容抽象为 Mock 数据，并封装前端 API 层，后续可替换为 Spring Boot 接口。

## 开发范围

允许修改：

```text
apps/user-web/src/mock/
apps/user-web/src/api/
apps/user-web/src/types/
apps/user-web/src/stores/
```

## 前置依赖

必须完成：

```text
phase-1-user-web/02-工程初始化任务.md
phase-1-user-web/03-全局布局与基础组件任务.md
```

## Mock 文件

创建：

```text
users.ts
groups.ts
posts.ts
courses.ts
assignments.ts
tasks.ts
milestones.ts
wallets.ts
notifications.ts
hackathons.ts
jobs.ts
```

## 类型定义

创建：

```text
src/types/user.ts
src/types/group.ts
src/types/post.ts
src/types/course.ts
src/types/task.ts
src/types/wallet.ts
src/types/notification.ts
src/types/common.ts
```

## API 封装

创建：

```text
api/user.ts
api/group.ts
api/post.ts
api/course.ts
api/task.ts
api/wallet.ts
api/notification.ts
```

第一阶段 API 可直接返回 Mock 数据，但函数命名应模拟真实后端：

```text
getCurrentUser()
getGroups()
getGroupDetail(id)
getPostsByGroup(groupId)
getCourseList()
getCourseDetail(id)
getTaskList(params)
getTaskDetail(id)
getWalletSummary()
getNotifications()
```

## 数据对象最低字段

用户：

```text
id, name, avatarText, school, major, level, points, creditScore, skills, verified
```

任务：

```text
id, title, category, description, reward, levelRequired, slotsLeft, deadline, skills, status
```

课程：

```text
id, title, subtitle, chapters, progress, rewardPoints, status
```

帖子：

```text
id, groupId, author, title, content, likes, comments, views, tag, createdAt
```

## 禁止事项

```text
不要请求真实后端
不要引入数据库
不要把 Mock 数据写在页面组件内部
```

## 验收标准

```text
所有 Mock 数据集中在 src/mock
所有业务页面可通过 api 函数获取数据
TypeScript 类型清晰
后续替换真实 Axios 请求时页面无需大改
```

## 交给 AI IDE 的执行提示

```text
请只完成 Mock 数据和 API 封装任务。把原型中的用户、小组、帖子、课程、任务、里程碑、收益、通知、黑客松、岗位数据抽成 src/mock 文件，并创建对应 api 函数。不要开发页面。
```

