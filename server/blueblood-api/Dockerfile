# ---- 构建阶段：Maven 编译打包 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 先拷 pom 利用层缓存下载依赖
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
# 拷源码并打包(跳过测试)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- 运行阶段：精简 JRE ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# 上传文件落地目录
RUN mkdir -p /app/uploads
EXPOSE 8080
# 通过环境变量连接 compose 内的 mysql/redis
ENTRYPOINT ["java", "-jar", "app.jar"]
