# 02-技术架构与Docker运行方案

## 文档用途

本文件描述整体技术架构和 Docker 运行思路。具体开发任务以各 `phase-*` 文档为准。

## 总体技术路线

```text
用户 H5：Vue3 + Vite + TypeScript + Vant
后端 API：Spring Boot 3 + MyBatis-Plus + MySQL + Redis
后台管理：RuoYi-Vue3 / Element Plus
小程序：uni-app + Vue3
```

## 推荐最终目录

```text
bluebloodv2/
  prototype/
  docs/
  apps/
    user-web/
    admin-web/
    miniapp/
  server/
    blueblood-api/
  deploy/
    docker-compose.local.yml
    docker-compose.server.yml
    nginx/
    env/
  sql/
```

## 本地 Docker 运行目标

第一阶段只需要支持用户 H5 本地 Docker 运行：

```text
blueblood-user-web
```

第二阶段后支持：

```text
blueblood-user-web
blueblood-api
blueblood-mysql
blueblood-redis
blueblood-nginx
```

第三阶段后支持：

```text
blueblood-admin-web
```

## 服务器 Docker 运行目标

服务器端可使用：

```text
blueblood-nginx
blueblood-user-web
blueblood-admin-web
blueblood-api
blueblood-redis
mysql 或服务器已有 MySQL
phpmyadmin
blueblood-php 可保留过渡
```

Nginx 推荐路由：

```text
/            -> 用户 H5
/admin/      -> 后台管理
/api/        -> Spring Boot API
/uploads/    -> 文件资源
```

## 环境变量

前端：

```text
VITE_API_BASE_URL
VITE_APP_TITLE
VITE_UPLOAD_BASE_URL
```

后端：

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
JWT_SECRET
UPLOAD_BASE_PATH
UPLOAD_PUBLIC_URL
```

## 第一阶段 Docker 要求

前台 H5 项目需提供：

```text
apps/user-web/Dockerfile
deploy/docker-compose.local.yml
```

构建方式建议：

```text
Node 镜像构建 Vue dist
Nginx 镜像托管 dist
暴露本地端口
```

## 注意事项

```text
第一阶段不要引入数据库
第一阶段不要依赖后端服务启动
Mock 数据应在前端项目内可直接运行
Docker 配置应尽量简单
服务器部署配置后续阶段再完善
```

