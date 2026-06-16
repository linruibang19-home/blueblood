# 蓝血菁英平台项目资料

## 当前资料结构

```text
prototype/
  蓝血精英平台-原型-v6.1.html

docs/
  00-项目总览与开发规则.md
  01-原型业务理解.md
  02-技术架构与Docker运行方案.md
  蓝血菁英平台技术开发方案V1.0.md

  phase-1-user-web/
    01-前台H5开发总说明.md
    02-工程初始化任务.md
    03-全局布局与基础组件任务.md
    04-Mock数据与API封装任务.md
    05-发现模块开发任务.md
    06-成长模块开发任务.md
    07-任务模块开发任务.md
    08-我的模块开发任务.md
    09-前台H5验收清单.md

  phase-2-springboot-api/
    01-后端开发总说明.md
    02-数据库模型设计.md
    03-认证与用户模块任务.md
    04-社区模块任务.md
    05-课程与作业模块任务.md
    06-任务与里程碑模块任务.md
    07-钱包收益与通知模块任务.md
    08-文件上传与系统配置任务.md
    09-后端接口验收清单.md

  phase-3-admin/
    01-后台管理端总说明.md
    02-RuoYi基础改造任务.md
    03-用户与认证审核管理任务.md
    04-社区内容管理任务.md
    05-课程作业管理任务.md
    06-任务里程碑审核任务.md
    07-收益结算管理任务.md
    08-系统配置与权限任务.md
    09-后台验收清单.md

  phase-4-miniapp/
    01-小程序适配总说明.md
    02-uni-app工程初始化任务.md
    03-H5页面迁移策略.md
    04-微信登录上传订阅消息任务.md
    05-小程序验收清单.md
```

## 已确认技术路线

```text
第一阶段：Vue3 + Vite + TypeScript + Vant 复原前台 H5 原型
第二阶段：Spring Boot 3 + MyBatis-Plus + MySQL + Redis 建真实后端
第三阶段：RuoYi-Vue3 / Element Plus 建后台管理端
第四阶段：uni-app + Vue3 适配小程序端
```

## 给 AI IDE 的使用方式

不要一次性把所有文档都交给 AI IDE 开发。

建议每次只投喂：

```text
1. docs/00-项目总览与开发规则.md
2. docs/01-原型业务理解.md
3. docs/02-技术架构与Docker运行方案.md
4. 当前要执行的任务文档
5. prototype/蓝血精英平台-原型-v6.1.html
```

## 第一阶段推荐执行顺序

```text
docs/phase-1-user-web/01-前台H5开发总说明.md
docs/phase-1-user-web/02-工程初始化任务.md
docs/phase-1-user-web/03-全局布局与基础组件任务.md
docs/phase-1-user-web/04-Mock数据与API封装任务.md
docs/phase-1-user-web/05-发现模块开发任务.md
docs/phase-1-user-web/06-成长模块开发任务.md
docs/phase-1-user-web/07-任务模块开发任务.md
docs/phase-1-user-web/08-我的模块开发任务.md
docs/phase-1-user-web/09-前台H5验收清单.md
```

## 执行约束

```text
每次只执行一个任务文档
不要跨阶段开发
不要提前开发后端、后台或小程序
不要删除 prototype 原型文件
不要把所有页面写在一个文件中
不要把业务数据硬编码在页面模板中
```

## 原型文件

客户原型已归档：

```text
prototype/蓝血精英平台-原型-v6.1.html
```

