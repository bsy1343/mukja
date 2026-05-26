# Dockerfile — multi-stage: JAR 빌드 → 실행 (CSS는 정적 산출물을 그대로 사용, Node 불필요)
# 1) Gradle 래퍼로 bootJar 빌드 (JDK 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --version --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# 2) 실행 (JRE 21)
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV MUKJA_DATA_DIR=/data
ENV TZ=Asia/Seoul
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
