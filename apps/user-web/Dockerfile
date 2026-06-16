# ---- 构建阶段 ----
FROM node:20-alpine AS build
WORKDIR /app

# 先拷依赖清单，利用 Docker 层缓存
COPY package*.json ./
RUN npm install

# 拷源码并构建
COPY . .
RUN npm run build

# ---- 运行阶段 ----
FROM nginx:stable-alpine AS runtime

# 替换默认站点配置，支持 SPA history 回退
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷构建产物到 nginx 静态目录
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
