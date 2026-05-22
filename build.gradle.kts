// build.gradle.kts — 빌드 설정 (Spring Boot 3.5, Java 21)
plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}
group = "dev.sybaek"
version = "0.1.0"
java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}
repositories { mavenCentral() }
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.webjars.npm:htmx.org:2.0.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.withType<Test> { useJUnitPlatform() }
