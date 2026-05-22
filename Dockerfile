# Dockerfile — multi-stage: CSS 빌드 → JAR 빌드 → 실행
# 1) Tailwind/DaisyUI CSS 빌드 (Node)
FROM node:22-alpine AS css
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY src/main/css ./src/main/css
COPY src/main/resources/templates ./src/main/resources/templates
COPY src/main/resources/static ./src/main/resources/static
RUN npm run build:css

# 2) Gradle 래퍼로 bootJar 빌드 (JDK 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --version --no-daemon
COPY src ./src
# CSS 빌드 산출물을 덮어써서 최신 상태로 패키징
COPY --from=css /app/src/main/resources/static/css/app.css ./src/main/resources/static/css/app.css
RUN ./gradlew bootJar --no-daemon

# 3) 실행 (JRE 21)
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV MUKJA_DATA_DIR=/data
ENV TZ=Asia/Seoul
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
