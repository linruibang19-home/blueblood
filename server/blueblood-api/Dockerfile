# 精简运行时镜像：本地用 Maven 预先打好 jar，容器只需 JRE 运行
# 构建前先在宿主执行： mvn -q clean package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
