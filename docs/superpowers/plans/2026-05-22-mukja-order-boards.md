# mukja — 카테고리×팀 주문판 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사내 커피 주문을 카테고리×팀 단위 상시 주문판으로 취합하는 모바일 우선 웹앱을 만든다.

**Architecture:** Spring Boot 3.5 + Thymeleaf SSR + HTMX 부분 갱신, 데이터는 보드별 JSON 파일(`data/orders/{category}-{team}.json`)에 `JsonStore<T>`(RWLock, atomic move)로 저장. 세션 개념 없이 보드는 상시 존재하고, 당번이 마감 시각 설정·수동 초기화로 재사용. 실시간은 보드별 SSE + 폴링 폴백.

**Tech Stack:** Java 21, Spring Boot 3.5.x (web, thymeleaf, test), Gradle Kotlin DSL, HTMX 2.x(webjar), Tailwind CSS 4 + DaisyUI 5(standalone/npm CLI 빌드 산출물), Jackson, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md`

---

## File Structure

```
mukja/
├── build.gradle.kts                 # Gradle Kotlin DSL, Spring Boot 3.5, Java 21
├── settings.gradle.kts              # rootProject.name = "mukja"
├── gradle/ gradlew gradlew.bat      # Gradle wrapper
├── package.json                     # Tailwind4 + DaisyUI5 CSS 빌드 (Node)
├── src/main/css/app.css             # Tailwind 입력 (@import/@plugin/@source)
├── src/main/java/dev/sybaek/mukja/
│   ├── MukjaApplication.java
│   ├── config/
│   │   ├── MukjaProperties.java     # @ConfigurationProperties("mukja"): adminPin, teams
│   │   ├── JacksonConfig.java       # KST, JavaTimeModule
│   │   ├── WebConfig.java           # 인터셉터 등록
│   │   └── AdminPinInterceptor.java # /admin/** PIN 보호
│   ├── common/store/
│   │   ├── JsonStore.java           # 제네릭 JSON read/write/update + RWLock
│   │   └── StoreException.java
│   ├── menu/
│   │   ├── MenuRepository.java      # menus.json 로드/저장 + seed 복사
│   │   ├── MenuService.java
│   │   ├── PriceCalculator.java     # 데이터 주도 가격 계산
│   │   ├── OptionTextBuilder.java   # 옵션 요약 텍스트
│   │   └── domain/{MenuData,Place,Category,MenuItem,OptionDef,OptionChoice}.java
│   ├── order/
│   │   ├── OrderController.java     # 네비 + 주문 + 상태 + 마감/초기화
│   │   ├── OrderService.java
│   │   ├── OrderRepository.java     # 보드별 JsonStore 캐시
│   │   ├── OrderAggregator.java     # 집계 + 요약 텍스트
│   │   ├── BoardClosedException.java
│   │   ├── sse/OrderSseService.java # 보드별 SseEmitter
│   │   └── domain/{BoardData,OrderEntry,OrderLine,Aggregation,MenuAgg,Stats}.java
│   └── admin/AdminController.java   # 메뉴 CRUD
├── src/main/resources/
│   ├── application.yml
│   ├── data/menus.seed.json
│   ├── templates/{layout.html, order/*, admin/*}
│   └── static/{css/app.css, js/order.js}
├── src/test/java/dev/sybaek/mukja/  # 단위 테스트
├── Dockerfile                       # multi-stage (node css → gradle jar → jre run)
└── docker-compose.yml
```

---

## M1 — 프로젝트 뼈대 & 저장소

### Task 1: Gradle 프로젝트 생성 및 부팅 확인

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`
- Create: `src/main/java/dev/sybaek/mukja/MukjaApplication.java`
- Create: `src/main/resources/application.yml`

- [ ] **Step 1: Gradle wrapper 생성**

Run: `gradle wrapper --gradle-version 8.10` (또는 시스템 gradle 사용). 설치된 gradle이 없으면 `brew install gradle` 후 실행.
Expected: `gradlew`, `gradle/wrapper/` 생성.

- [ ] **Step 2: `settings.gradle.kts` 작성**

```kotlin
// settings.gradle.kts — Gradle 루트 프로젝트 설정
rootProject.name = "mukja"
```

- [ ] **Step 3: `build.gradle.kts` 작성**

```kotlin
// build.gradle.kts — 빌드 설정 (Spring Boot 3.5, Java 21)
plugins {
    java
    id("org.springframework.boot") version "3.5.9"
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
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.webjars.npm:htmx.org:2.0.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> { useJUnitPlatform() }
```

- [ ] **Step 4: `MukjaApplication.java` 작성**

```java
// MukjaApplication.java — Spring Boot 애플리케이션 진입점
package dev.sybaek.mukja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MukjaApplication {
    // 애플리케이션 부팅
    public static void main(String[] args) {
        SpringApplication.run(MukjaApplication.class, args);
    }
}
```

- [ ] **Step 5: `application.yml` 작성**

```yaml
# application.yml — 애플리케이션 설정
spring:
  application:
    name: mukja
  jackson:
    time-zone: Asia/Seoul
mukja:
  data-dir: ${MUKJA_DATA_DIR:./data}
  admin-pin: ${MUKJA_ADMIN_PIN:1234}
  teams:
    - { id: all,  name: 전체 }
    - { id: sa,   name: SA팀 }
    - { id: imdg, name: IMDG팀 }
```

- [ ] **Step 6: 부팅 검증**

Run: `./gradlew bootRun` (몇 초 후 Ctrl-C)
Expected: `Started MukjaApplication` 로그, 8080 포트 기동, 에러 없음.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradlew gradlew.bat gradle src/main/java/dev/sybaek/mukja/MukjaApplication.java src/main/resources/application.yml
git commit -m "feat: Spring Boot 3.5 프로젝트 뼈대 (mukja)"
```

---

### Task 2: JsonStore<T> 제네릭 저장소 (TDD)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/common/store/StoreException.java`
- Create: `src/main/java/dev/sybaek/mukja/common/store/JsonStore.java`
- Test: `src/test/java/dev/sybaek/mukja/common/store/JsonStoreTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// JsonStoreTest.java — JsonStore 동작 및 동시성 검증
package dev.sybaek.mukja.common.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class JsonStoreTest {
    record Box(int value) {}

    @Test
    void readReturnsDefaultWhenFileMissing(@TempDir Path dir) {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        assertEquals(0, store.read().value());
    }

    @Test
    void writeThenReadRoundTrips(@TempDir Path dir) {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        store.write(new Box(42));
        assertEquals(42, store.read().value());
    }

    @Test
    void concurrentUpdatesDoNotLoseData(@TempDir Path dir) throws Exception {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        int n = 200;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = IntStream.range(0, n)
                .mapToObj(i -> pool.submit(() ->
                        store.update(cur -> { store.writeUnlocked(new Box(cur.value() + 1)); return null; })))
                .toList();
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        assertEquals(n, store.read().value());
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests JsonStoreTest`
Expected: 컴파일 실패 (JsonStore/StoreException 없음).

- [ ] **Step 3: StoreException 작성**

```java
// StoreException.java — JSON 저장소 I/O 예외
package dev.sybaek.mukja.common.store;

public class StoreException extends RuntimeException {
    // 저장소 읽기/쓰기 실패 시 던진다
    public StoreException(String message, Throwable cause) { super(message, cause); }
}
```

- [ ] **Step 4: JsonStore 최소 구현**

```java
// JsonStore.java — 제네릭 JSON 파일 저장소 (RWLock + atomic move)
package dev.sybaek.mukja.common.store;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonStore<T> {
    private final Path file;
    private final Class<T> type;
    private final ObjectMapper mapper;
    private final Supplier<T> defaultValue;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // file 경로의 JSON을 type으로 직렬화/역직렬화한다. 파일이 없으면 defaultValue를 반환한다
    public JsonStore(Path file, Class<T> type, ObjectMapper mapper, Supplier<T> defaultValue) {
        this.file = file;
        this.type = type;
        this.mapper = mapper;
        this.defaultValue = defaultValue;
    }

    // 읽기 락으로 현재 값을 읽는다 (없으면 기본값)
    public T read() {
        lock.readLock().lock();
        try { return readUnlocked(); }
        finally { lock.readLock().unlock(); }
    }

    // 쓰기 락으로 값을 저장한다 (tmp 파일 작성 후 atomic move)
    public void write(T data) {
        lock.writeLock().lock();
        try { writeUnlocked(data); }
        finally { lock.writeLock().unlock(); }
    }

    // 쓰기 락으로 read-modify-write를 원자적으로 수행한다
    public <R> R update(Function<T, R> fn) {
        lock.writeLock().lock();
        try { return fn.apply(readUnlocked()); }
        finally { lock.writeLock().unlock(); }
    }

    // 락 없이 읽는다 (update 콜백 내부 전용)
    public T readUnlocked() {
        if (Files.notExists(file)) return defaultValue.get();
        try { return mapper.readValue(file.toFile(), type); }
        catch (IOException e) { throw new StoreException("read failed: " + file, e); }
    }

    // 락 없이 쓴다 (update 콜백 내부 전용): tmp → ATOMIC_MOVE
    public void writeUnlocked(T data) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = Files.createTempFile(file.getParent(), "tmp-", ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), data);
            try { Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new StoreException("write failed: " + file, e); }
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests JsonStoreTest`
Expected: 3 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/common/store src/test/java/dev/sybaek/mukja/common/store
git commit -m "feat: JsonStore 제네릭 파일 저장소 (RWLock, atomic move)"
```

---

### Task 3: 메뉴 도메인 + JacksonConfig + 시드

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/menu/domain/{Place,OptionChoice,OptionDef,MenuItem,Category,MenuData}.java`
- Create: `src/main/java/dev/sybaek/mukja/config/JacksonConfig.java`
- Create: `src/main/java/dev/sybaek/mukja/config/MukjaProperties.java`
- Create: `src/main/resources/data/menus.seed.json`
- Test: `src/test/java/dev/sybaek/mukja/menu/MenuDataJsonTest.java`

- [ ] **Step 1: 도메인 record 작성**

```java
// Place.java — 주문 장소
package dev.sybaek.mukja.menu.domain;
public record Place(String id, String name, String floor) {}
```
```java
// OptionChoice.java — 단일선택 옵션의 보기 (가산금액 extra)
package dev.sybaek.mukja.menu.domain;
public record OptionChoice(String id, String name, int extra) {}
```
```java
// OptionDef.java — 옵션 정의. type: single|toggle|counter
package dev.sybaek.mukja.menu.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OptionDef(String label, String type, boolean required,
                        List<OptionChoice> choices, Integer extra, Integer max) {}
```
```java
// MenuItem.java — 메뉴 항목. fixedTemp가 있으면 temp 옵션을 해당 값으로 고정
package dev.sybaek.mukja.menu.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuItem(int id, String name, int price, List<String> options, String fixedTemp) {}
```
```java
// Category.java — 서브카테고리. group: coffee|food
package dev.sybaek.mukja.menu.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(String id, String name, String group, List<MenuItem> items) {}
```
```java
// MenuData.java — 메뉴 루트 (장소, 옵션정의 맵, 카테고리 목록)
package dev.sybaek.mukja.menu.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuData(Place place, Map<String, OptionDef> optionDefs, List<Category> categories) {}
```

- [ ] **Step 2: JacksonConfig 작성**

```java
// JacksonConfig.java — ObjectMapper 커스터마이즈 (JavaTime, KST)
package dev.sybaek.mukja.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    // 시간 타입(OffsetDateTime 등)을 ISO 문자열로 직렬화한다
    @Bean
    Jackson2ObjectMapperBuilderCustomizer timeModule() {
        return builder -> builder.modulesToInstall(new JavaTimeModule())
                .featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

- [ ] **Step 3: MukjaProperties 작성**

```java
// MukjaProperties.java — 앱 설정 바인딩 (mukja.*)
package dev.sybaek.mukja.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties("mukja")
public record MukjaProperties(String dataDir, String adminPin, List<Team> teams) {
    // 팀 정의 (id는 URL 경로에 사용)
    public record Team(String id, String name) {}
}
```

- [ ] **Step 4: `menus.seed.json` 작성 (커피만, group 포함)**

```json
{
  "place": { "id": "kt-bundang-cafe", "name": "KT그룹희망나눔재단 분당 카페", "floor": "21층" },
  "optionDefs": {
    "temp":  { "label": "HOT / ICE", "type": "single", "required": true,
               "choices": [ {"id":"hot","name":"HOT","extra":0}, {"id":"ice","name":"ICE","extra":500} ] },
    "ice":   { "label": "얼음", "type": "single", "required": false,
               "choices": [ {"id":"ice_normal","name":"기본","extra":0}, {"id":"ice_none","name":"얼음 없음","extra":0},
                            {"id":"ice_less","name":"얼음 적게","extra":0}, {"id":"ice_more","name":"얼음 많이","extra":0} ] },
    "light": { "label": "연하게", "type": "toggle", "required": false, "extra": 0 },
    "shot":  { "label": "샷 추가", "type": "counter", "required": false, "extra": 500, "max": 3 }
  },
  "categories": [
    { "id": "coffee", "name": "커피", "group": "coffee", "items": [
      { "id": 101, "name": "에스프레소", "price": 1500, "options": ["shot"], "fixedTemp": "hot" },
      { "id": 102, "name": "아메리카노", "price": 1600, "options": ["temp","ice","light","shot"] },
      { "id": 103, "name": "카페라떼", "price": 2100, "options": ["temp","ice","light","shot"] },
      { "id": 104, "name": "바닐라라떼", "price": 2600, "options": ["temp","ice","light","shot"] }
    ] },
    { "id": "ade", "name": "에이드", "group": "coffee", "items": [
      { "id": 201, "name": "자몽에이드", "price": 2800, "options": ["ice"], "fixedTemp": "ice" }
    ] }
  ]
}
```

- [ ] **Step 5: 직렬화 라운드트립 테스트 작성**

```java
// MenuDataJsonTest.java — 시드 JSON이 도메인으로 정상 매핑되는지 검증
package dev.sybaek.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.menu.domain.MenuData;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class MenuDataJsonTest {
    @Test
    void seedJsonDeserializes() throws Exception {
        MenuData data = new ObjectMapper().readValue(
                new ClassPathResource("data/menus.seed.json").getInputStream(), MenuData.class);
        assertEquals("kt-bundang-cafe", data.place().id());
        assertEquals(2, data.categories().size());
        assertEquals("coffee", data.categories().get(0).group());
        assertEquals("ice", data.categories().get(1).items().get(0).fixedTemp());
        assertTrue(data.optionDefs().containsKey("temp"));
        assertEquals(500, data.optionDefs().get("shot").extra());
    }
}
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests MenuDataJsonTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/menu/domain src/main/java/dev/sybaek/mukja/config src/main/resources/data/menus.seed.json src/test/java/dev/sybaek/mukja/menu/MenuDataJsonTest.java
git commit -m "feat: 메뉴 도메인 + 시드 JSON + Jackson/Properties 설정"
```

---

### Task 4: MenuRepository (seed 복사 + 로드)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/menu/MenuRepository.java`
- Test: `src/test/java/dev/sybaek/mukja/menu/MenuRepositoryTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// MenuRepositoryTest.java — seed 복사 및 로드 검증
package dev.sybaek.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.menu.domain.MenuData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuRepositoryTest {
    @Test
    void copiesSeedWhenMissingThenLoads(@TempDir Path dir) {
        MenuRepository repo = new MenuRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertTrue(Files.exists(dir.resolve("menus.json")));
        MenuData data = repo.load();
        assertEquals(2, data.categories().size());
    }

    @Test
    void keepsExistingMenusJson(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("menus.json"),
            "{\"place\":{\"id\":\"x\",\"name\":\"X\",\"floor\":\"1\"},\"optionDefs\":{},\"categories\":[]}");
        MenuRepository repo = new MenuRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertEquals("x", repo.load().place().id());
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests MenuRepositoryTest`
Expected: 컴파일 실패.

- [ ] **Step 3: MenuRepository 구현**

```java
// MenuRepository.java — menus.json 로드/저장 + 최초 seed 복사
package dev.sybaek.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.common.store.JsonStore;
import dev.sybaek.mukja.config.MukjaProperties;
import dev.sybaek.mukja.menu.domain.MenuData;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Repository
public class MenuRepository {
    private final Path file;
    private final JsonStore<MenuData> store;
    private final ObjectMapper mapper;

    // 설정의 data-dir 아래 menus.json을 다룬다
    public MenuRepository(MukjaProperties props, ObjectMapper mapper) {
        this(props.dataDir(), mapper);
    }

    // 테스트용 생성자
    MenuRepository(String dataDir, ObjectMapper mapper) {
        this.file = Path.of(dataDir, "menus.json");
        this.mapper = mapper;
        this.store = new JsonStore<>(file, MenuData.class, mapper,
                () -> new MenuData(null, Map.of(), List.of()));
    }

    // 기동 시 menus.json이 없으면 클래스패스 seed를 복사한다
    @PostConstruct
    public void init() {
        if (Files.exists(file)) return;
        try {
            Files.createDirectories(file.getParent());
            try (var in = new ClassPathResource("data/menus.seed.json").getInputStream()) {
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) { throw new RuntimeException("seed copy failed", e); }
    }

    // 현재 메뉴 데이터를 읽는다
    public MenuData load() { return store.read(); }

    // 메뉴 데이터를 저장한다 (관리자 CRUD)
    public void save(MenuData data) { store.write(data); }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests MenuRepositoryTest`
Expected: 2 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/menu/MenuRepository.java src/test/java/dev/sybaek/mukja/menu/MenuRepositoryTest.java
git commit -m "feat: MenuRepository (seed 복사 + 로드/저장)"
```

---

## M2 — 가격 계산 · 주문 도메인 · 주문 저장

### Task 5: PriceCalculator + OptionTextBuilder (TDD)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/menu/PriceCalculator.java`
- Create: `src/main/java/dev/sybaek/mukja/menu/OptionTextBuilder.java`
- Test: `src/test/java/dev/sybaek/mukja/menu/PriceCalculatorTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// PriceCalculatorTest.java — 데이터 주도 가격 계산 + 옵션 텍스트 검증
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {
    private final Map<String, OptionDef> defs = Map.of(
        "temp", new OptionDef("HOT / ICE", "single", true,
            List.of(new OptionChoice("hot","HOT",0), new OptionChoice("ice","ICE",500)), null, null),
        "shot", new OptionDef("샷 추가", "counter", false, null, 500, 3),
        "light", new OptionDef("연하게", "toggle", false, null, 0, null));

    private final MenuItem americano = new MenuItem(102, "아메리카노", 1600,
        List.of("temp","light","shot"), null);

    @Test
    void iceShotAddsExtras() {
        Map<String,Object> sel = Map.of("temp","ice", "shot", 1, "light", true);
        assertEquals(1600 + 500 + 500, new PriceCalculator().calc(americano, defs, sel));
    }

    @Test
    void hotNoExtra() {
        assertEquals(1600, new PriceCalculator().calc(americano, defs, Map.of("temp","hot")));
    }

    @Test
    void fixedTempIgnoresIceExtra() {
        MenuItem espresso = new MenuItem(101,"에스프레소",1500, List.of("shot"), "hot");
        assertEquals(1500, new PriceCalculator().calc(espresso, defs, Map.of("shot", 0)));
    }

    @Test
    void optionTextIsHumanReadable() {
        Map<String,Object> sel = new LinkedHashMap<>();
        sel.put("temp","ice"); sel.put("light", true); sel.put("shot", 1);
        assertEquals("ICE·연하게·샷+1", new OptionTextBuilder().build(americano, defs, sel));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests PriceCalculatorTest`
Expected: 컴파일 실패.

- [ ] **Step 3: PriceCalculator 구현**

```java
// PriceCalculator.java — 메뉴 가격 + 선택 옵션의 가산금액 합산 (데이터 주도)
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.menu.domain.OptionChoice;
import dev.sybaek.mukja.menu.domain.OptionDef;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PriceCalculator {
    // item 기본가 + 선택 옵션 가산금액을 합산한다
    public int calc(MenuItem item, Map<String, OptionDef> defs, Map<String, Object> selected) {
        int total = item.price();
        for (var entry : selected.entrySet()) {
            OptionDef def = defs.get(entry.getKey());
            if (def == null) continue;
            total += extraFor(item, entry.getKey(), def, entry.getValue());
        }
        return total;
    }

    // 옵션 한 개의 가산금액을 계산한다
    private int extraFor(MenuItem item, String key, OptionDef def, Object value) {
        return switch (def.type()) {
            case "single" -> {
                // 온도가 고정된 메뉴는 temp 가산금액을 무시한다
                if ("temp".equals(key) && item.fixedTemp() != null) yield 0;
                yield def.choices().stream()
                        .filter(c -> c.id().equals(String.valueOf(value)))
                        .mapToInt(OptionChoice::extra).findFirst().orElse(0);
            }
            case "toggle" -> Boolean.TRUE.equals(value) ? def.extra() : 0;
            case "counter" -> ((Number) value).intValue() * def.extra();
            default -> 0;
        };
    }
}
```

- [ ] **Step 4: OptionTextBuilder 구현**

```java
// OptionTextBuilder.java — 선택 옵션을 "ICE·연하게·샷+1" 형태 텍스트로 만든다
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.menu.domain.OptionDef;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OptionTextBuilder {
    // 메뉴의 options 순서대로 선택값을 사람이 읽는 텍스트로 변환한다
    public String build(MenuItem item, Map<String, OptionDef> defs, Map<String, Object> selected) {
        List<String> parts = new ArrayList<>();
        for (String key : item.options()) {
            OptionDef def = defs.get(key);
            Object value = selected.get(key);
            if (def == null || value == null) continue;
            switch (def.type()) {
                case "single" -> def.choices().stream()
                        .filter(c -> c.id().equals(String.valueOf(value)))
                        .findFirst().ifPresent(c -> { if (c.extra() >= 0) parts.add(c.name()); });
                case "toggle" -> { if (Boolean.TRUE.equals(value)) parts.add(def.label()); }
                case "counter" -> {
                    int n = ((Number) value).intValue();
                    if (n > 0) parts.add("샷+" + n);
                }
            }
        }
        return String.join("·", parts);
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests PriceCalculatorTest`
Expected: 4 tests PASS. (single에서 "기본"같은 extra 0 보기도 텍스트에 포함됨 — 의도된 동작. 테스트의 ice/light/shot만 검증하므로 통과.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/menu/PriceCalculator.java src/main/java/dev/sybaek/mukja/menu/OptionTextBuilder.java src/test/java/dev/sybaek/mukja/menu/PriceCalculatorTest.java
git commit -m "feat: 데이터 주도 가격 계산 + 옵션 텍스트"
```

---

### Task 6: 주문 도메인 + OrderRepository (보드/마감/초기화) (TDD)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/order/domain/{OrderLine,OrderEntry,BoardData}.java`
- Create: `src/main/java/dev/sybaek/mukja/order/BoardClosedException.java`
- Create: `src/main/java/dev/sybaek/mukja/order/OrderRepository.java`
- Test: `src/test/java/dev/sybaek/mukja/order/OrderRepositoryTest.java`

- [ ] **Step 1: 주문 도메인 record 작성**

```java
// OrderLine.java — 주문 1줄 (메뉴 + 선택옵션 + 금액)
package dev.sybaek.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLine(int itemId, String name, int unitPrice,
                        Map<String, Object> options, String optionText, int lineTotal) {}
```
```java
// OrderEntry.java — 한 사람의 주문
package dev.sybaek.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEntry(String person, OffsetDateTime submittedAt, List<OrderLine> lines) {}
```
```java
// BoardData.java — 주문판 저장 단위 (마감시각 nullable + 주문 목록)
package dev.sybaek.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record BoardData(OffsetDateTime closeAt, List<OrderEntry> orders) {
    // 빈 주문판
    public static BoardData empty() { return new BoardData(null, List.of()); }
}
```

- [ ] **Step 2: BoardClosedException 작성**

```java
// BoardClosedException.java — 마감된 주문판에 제출 시도 시
package dev.sybaek.mukja.order;
public class BoardClosedException extends RuntimeException {
    public BoardClosedException(String message) { super(message); }
}
```

- [ ] **Step 3: 실패하는 테스트 작성**

```java
// OrderRepositoryTest.java — 보드 제출/덮어쓰기/마감차단/초기화 검증
package dev.sybaek.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.sybaek.mukja.order.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {
    private OrderRepository repo(Path dir) {
        return new OrderRepository(dir.toString(), new ObjectMapper().registerModule(new JavaTimeModule()));
    }
    private OrderEntry entry(String person, int total) {
        return new OrderEntry(person, OffsetDateTime.now(),
            List.of(new OrderLine(102,"아메리카노",1600, java.util.Map.of("temp","hot"), "HOT", total)));
    }

    @Test
    void submitAndRead(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        assertEquals(1, repo.read("coffee","sa").orders().size());
    }

    @Test
    void resubmitOverwritesSamePerson(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        repo.submit("coffee","sa", entry("백상열", 2100));
        BoardData b = repo.read("coffee","sa");
        assertEquals(1, b.orders().size());
        assertEquals(2100, b.orders().get(0).lines().get(0).lineTotal());
    }

    @Test
    void boardsAreIsolated(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        assertEquals(0, repo.read("coffee","imdg").orders().size());
    }

    @Test
    void submitAfterDeadlineThrows(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.setDeadline("coffee","sa", OffsetDateTime.now().minusMinutes(1));
        assertThrows(BoardClosedException.class, () -> repo.submit("coffee","sa", entry("백상열", 1600)));
    }

    @Test
    void resetClearsOrdersAndDeadline(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        repo.setDeadline("coffee","sa", OffsetDateTime.now().plusMinutes(30));
        repo.reset("coffee","sa");
        BoardData b = repo.read("coffee","sa");
        assertTrue(b.orders().isEmpty());
        assertNull(b.closeAt());
    }
}
```

- [ ] **Step 4: 테스트 실패 확인**

Run: `./gradlew test --tests OrderRepositoryTest`
Expected: 컴파일 실패.

- [ ] **Step 5: OrderRepository 구현**

```java
// OrderRepository.java — 보드별(카테고리×팀) 주문 저장. 보드마다 독립 JsonStore
package dev.sybaek.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.common.store.JsonStore;
import dev.sybaek.mukja.config.MukjaProperties;
import dev.sybaek.mukja.order.domain.BoardData;
import dev.sybaek.mukja.order.domain.OrderEntry;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {
    private final String dataDir;
    private final ObjectMapper mapper;
    private final Map<String, JsonStore<BoardData>> stores = new ConcurrentHashMap<>();

    // 설정의 data-dir 아래 orders/ 디렉토리를 사용한다
    public OrderRepository(MukjaProperties props, ObjectMapper mapper) {
        this(props.dataDir(), mapper);
    }

    // 테스트용 생성자
    OrderRepository(String dataDir, ObjectMapper mapper) {
        this.dataDir = dataDir;
        this.mapper = mapper;
    }

    // 보드별 JsonStore를 lazy 생성/캐시한다
    private JsonStore<BoardData> store(String category, String team) {
        String key = category + "-" + team;
        return stores.computeIfAbsent(key, k -> new JsonStore<>(
                Path.of(dataDir, "orders", k + ".json"), BoardData.class, mapper, BoardData::empty));
    }

    // 보드 데이터를 읽는다
    public BoardData read(String category, String team) { return store(category, team).read(); }

    // 주문을 제출/수정한다. 동일 person은 덮어쓴다. 마감 경과 시 예외
    public void submit(String category, String team, OrderEntry entry) {
        store(category, team).update(cur -> {
            if (cur.closeAt() != null && OffsetDateTime.now().isAfter(cur.closeAt()))
                throw new BoardClosedException("마감된 주문판입니다");
            List<OrderEntry> next = new ArrayList<>(cur.orders());
            next.removeIf(e -> e.person().equals(entry.person()));
            next.add(entry);
            store(category, team).writeUnlocked(new BoardData(cur.closeAt(), next));
            return null;
        });
    }

    // 마감 시각을 설정한다
    public void setDeadline(String category, String team, OffsetDateTime closeAt) {
        store(category, team).update(cur -> {
            store(category, team).writeUnlocked(new BoardData(closeAt, new ArrayList<>(cur.orders())));
            return null;
        });
    }

    // 마감 시각을 해제한다
    public void clearDeadline(String category, String team) {
        store(category, team).update(cur -> {
            store(category, team).writeUnlocked(new BoardData(null, new ArrayList<>(cur.orders())));
            return null;
        });
    }

    // 주문 내역과 마감을 모두 비운다 (재사용)
    public void reset(String category, String team) {
        store(category, team).write(BoardData.empty());
    }
}
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests OrderRepositoryTest`
Expected: 5 tests PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/domain src/main/java/dev/sybaek/mukja/order/BoardClosedException.java src/main/java/dev/sybaek/mukja/order/OrderRepository.java src/test/java/dev/sybaek/mukja/order/OrderRepositoryTest.java
git commit -m "feat: 주문 도메인 + 보드별 OrderRepository (마감/초기화)"
```

---

### Task 7: Tailwind 4 + DaisyUI 5 CSS 빌드 셋업

**Files:**
- Create: `package.json`
- Create: `src/main/css/app.css`
- Create: `src/main/resources/static/css/app.css` (빌드 산출물)
- Create: `.gitignore` 항목 추가 (`node_modules/`)

- [ ] **Step 1: `package.json` 작성**

```json
{
  "name": "mukja-css",
  "private": true,
  "scripts": {
    "build:css": "tailwindcss -i ./src/main/css/app.css -o ./src/main/resources/static/css/app.css --minify",
    "watch:css": "tailwindcss -i ./src/main/css/app.css -o ./src/main/resources/static/css/app.css --watch"
  },
  "devDependencies": {
    "@tailwindcss/cli": "^4.0.0",
    "tailwindcss": "^4.0.0",
    "daisyui": "^5.0.0"
  }
}
```

- [ ] **Step 2: Tailwind 입력 CSS 작성 (v4 CSS-first)**

```css
/* src/main/css/app.css — Tailwind 4 입력. DaisyUI 5 플러그인 + Thymeleaf 템플릿 스캔 */
@import "tailwindcss";
@plugin "daisyui";
@source "../../resources/templates";
@source "../../resources/static/js";
```

- [ ] **Step 3: 의존성 설치 및 CSS 빌드**

Run: `npm install && npm run build:css`
Expected: `src/main/resources/static/css/app.css` 생성(minified, DaisyUI 클래스 포함).

- [ ] **Step 4: node_modules gitignore**

`.gitignore`에 다음 줄 추가:
```
node_modules/
```

- [ ] **Step 5: Commit**

```bash
git add package.json package-lock.json src/main/css/app.css src/main/resources/static/css/app.css .gitignore
git commit -m "build: Tailwind 4 + DaisyUI 5 CSS 빌드 셋업"
```

---

### Task 8: 공통 레이아웃 + 카테고리/팀 선택 화면

**Files:**
- Create: `src/main/resources/templates/layout.html`
- Create: `src/main/resources/templates/order/category.html`
- Create: `src/main/resources/templates/order/team.html`
- Create: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Test: `src/test/java/dev/sybaek/mukja/order/NavControllerTest.java`

- [ ] **Step 1: 실패하는 컨트롤러 테스트 작성**

```java
// NavControllerTest.java — 카테고리/팀 선택 화면 렌더 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NavControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void categoryPageLists() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("커피")));
    }

    @Test
    void teamPageListsTeams() throws Exception {
        mvc.perform(get("/coffee")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("전체")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("SA팀")));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests NavControllerTest`
Expected: 404 또는 컴파일 실패.

- [ ] **Step 3: layout.html 작성**

```html
<!-- layout.html — 공통 레이아웃 (head, HTMX, 다크모드) -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org" th:fragment="page(title, content)" data-theme="light">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title th:text="${title} ?: 'mukja'">mukja</title>
  <link rel="stylesheet" th:href="@{/css/app.css}">
  <script th:src="@{/webjars/htmx.org/2.0.4/dist/htmx.min.js}" defer></script>
  <script th:src="@{/js/order.js}" defer></script>
</head>
<body class="bg-base-200 min-h-screen">
  <main class="mx-auto max-w-[480px] min-h-screen bg-base-100 shadow-sm">
    <div th:replace="${content}">내용</div>
  </main>
</body>
</html>
```

- [ ] **Step 4: category.html 작성**

```html
<!-- category.html — 카테고리 선택 (커피/음식) -->
<div th:fragment="content" class="p-4">
  <h1 class="text-xl font-bold mb-4">뭐 먹자? 🍽️</h1>
  <div class="grid gap-3">
    <a th:href="@{/coffee}" class="btn btn-lg btn-primary h-20 text-lg">☕ 커피</a>
    <button class="btn btn-lg btn-disabled h-20 text-lg">🍱 음식 (준비중)</button>
  </div>
</div>
```

- [ ] **Step 5: team.html 작성**

```html
<!-- team.html — 팀 선택 ('전체' + 팀 버튼) -->
<div th:fragment="content" class="p-4">
  <h1 class="text-xl font-bold mb-4" th:text="'☕ ' + ${categoryName} + ' · 팀 선택'">☕ 커피 · 팀 선택</h1>
  <div class="grid gap-2">
    <a th:each="team : ${teams}" th:href="@{/{c}/{t}(c=${category}, t=${team.id})}"
       class="btn btn-outline justify-start h-12" th:text="${team.name}">팀</a>
  </div>
</div>
```

- [ ] **Step 6: OrderController 네비 메서드 작성**

```java
// OrderController.java — 네비게이션/주문/상태 라우트
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.config.MukjaProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class OrderController {
    private final MukjaProperties props;

    public OrderController(MukjaProperties props) { this.props = props; }

    // 카테고리 선택 화면
    @GetMapping("/")
    public String category() { return "order/category"; }

    // 팀 선택 화면
    @GetMapping("/{category}")
    public String team(@PathVariable String category, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("categoryName", "커피");
        model.addAttribute("teams", props.teams());
        return "order/team";
    }
}
```

- [ ] **Step 7: 테스트 통과 확인**

Run: `./gradlew test --tests NavControllerTest`
Expected: 2 tests PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/resources/templates/layout.html src/main/resources/templates/order/category.html src/main/resources/templates/order/team.html src/main/java/dev/sybaek/mukja/order/OrderController.java src/test/java/dev/sybaek/mukja/order/NavControllerTest.java
git commit -m "feat: 공통 레이아웃 + 카테고리/팀 선택 화면"
```

---

### Task 9: 주문판 화면 + 메뉴 그리드/옵션 모달 fragment

**Files:**
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Create: `src/main/java/dev/sybaek/mukja/menu/MenuService.java`
- Create: `src/main/resources/templates/order/board.html`
- Create: `src/main/resources/templates/order/fragments/menu-grid.html`
- Create: `src/main/resources/templates/order/fragments/option-modal.html`
- Test: `src/test/java/dev/sybaek/mukja/order/BoardControllerTest.java`

- [ ] **Step 1: MenuService 작성 (카테고리 그룹 필터 + 항목 조회)**

```java
// MenuService.java — 메뉴 조회 (카테고리 그룹 필터, 항목/옵션 lookup)
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.Category;
import dev.sybaek.mukja.menu.domain.MenuData;
import dev.sybaek.mukja.menu.domain.MenuItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {
    private final MenuRepository repo;
    public MenuService(MenuRepository repo) { this.repo = repo; }

    // 전체 메뉴 데이터
    public MenuData data() { return repo.load(); }

    // 카테고리 그룹(coffee/food)에 속한 서브카테고리만 반환
    public List<Category> categoriesIn(String group) {
        return repo.load().categories().stream().filter(c -> group.equals(c.group())).toList();
    }

    // 서브카테고리 id로 조회
    public Optional<Category> category(String subId) {
        return repo.load().categories().stream().filter(c -> c.id().equals(subId)).findFirst();
    }

    // 메뉴 항목 id로 조회
    public Optional<MenuItem> item(int itemId) {
        return repo.load().categories().stream()
                .flatMap(c -> c.items().stream())
                .filter(i -> i.id() == itemId).findFirst();
    }
}
```

- [ ] **Step 2: 실패하는 테스트 작성**

```java
// BoardControllerTest.java — 주문판 화면과 메뉴 그리드/옵션 fragment 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void boardPageShowsHeader() throws Exception {
        mvc.perform(get("/coffee/sa")).andExpect(status().isOk())
           .andExpect(content().string(containsString("SA팀")));
    }

    @Test
    void menuGridFragmentListsItems() throws Exception {
        mvc.perform(get("/coffee/sa/menu").param("cat", "coffee")).andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")));
    }

    @Test
    void optionModalFragmentShowsOptions() throws Exception {
        mvc.perform(get("/coffee/sa/menu/102/options")).andExpect(status().isOk())
           .andExpect(content().string(containsString("HOT")));
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `./gradlew test --tests BoardControllerTest`
Expected: 404.

- [ ] **Step 4: OrderController에 보드/메뉴 라우트 추가**

`OrderController.java`에 필드와 메서드 추가 (기존 생성자에 MenuService, OrderRepository 주입):

```java
    // (필드 추가)
    private final dev.sybaek.mukja.menu.MenuService menuService;
    private final OrderRepository orderRepository;

    // (생성자 교체)
    public OrderController(MukjaProperties props,
                           dev.sybaek.mukja.menu.MenuService menuService,
                           OrderRepository orderRepository) {
        this.props = props;
        this.menuService = menuService;
        this.orderRepository = orderRepository;
    }

    // 팀 표시 이름 조회 (없으면 id 그대로)
    private String teamName(String teamId) {
        return props.teams().stream().filter(t -> t.id().equals(teamId))
                .map(MukjaProperties.Team::name).findFirst().orElse(teamId);
    }

    // 주문판 화면
    @GetMapping("/{category}/{team}")
    public String board(@PathVariable String category, @PathVariable String team, Model model) {
        var board = orderRepository.read(category, team);
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("subCategories", menuService.categoriesIn(category));
        model.addAttribute("closeAt", board.closeAt());
        return "order/board";
    }

    // 서브카테고리 메뉴 그리드 fragment
    @GetMapping("/{category}/{team}/menu")
    public String menuGrid(@PathVariable String category, @PathVariable String team,
                           @org.springframework.web.bind.annotation.RequestParam String cat, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("subCategory", menuService.category(cat).orElseThrow());
        return "order/fragments/menu-grid :: grid";
    }

    // 옵션 선택 모달 fragment
    @GetMapping("/{category}/{team}/menu/{itemId}/options")
    public String optionModal(@PathVariable String category, @PathVariable String team,
                              @PathVariable int itemId, Model model) {
        var item = menuService.item(itemId).orElseThrow();
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("item", item);
        model.addAttribute("optionDefs", menuService.data().optionDefs());
        return "order/fragments/option-modal :: modal";
    }
```

- [ ] **Step 5: board.html 작성**

```html
<!-- board.html — 주문자 화면 (헤더 + 당번컨트롤 + 탭 + 그리드 + 카트바) -->
<div th:fragment="content" class="pb-24"
     th:with="base=@{/{c}/{t}(c=${category}, t=${team})}">
  <header class="sticky top-0 z-10 bg-base-100 border-b p-3">
    <div class="flex items-center justify-between">
      <div class="font-bold" th:text="'☕ 커피 · ' + ${teamName}">☕ 커피 · SA팀</div>
      <span th:if="${closeAt}" id="countdown" class="badge badge-success"
            th:data-close="${closeAt}">마감</span>
    </div>
    <input id="person" class="input input-bordered input-sm w-full mt-2"
           placeholder="이름을 입력하세요">
    <div class="flex gap-1 mt-2 text-xs">
      <button class="btn btn-xs" onclick="setDeadline()">마감설정</button>
      <button class="btn btn-xs" th:onclick="'clearDeadline()'">마감해제</button>
      <button class="btn btn-xs btn-warning" onclick="resetBoard()">초기화</button>
      <a th:href="${base} + '/status'" class="btn btn-xs btn-ghost ml-auto">집계 →</a>
    </div>
  </header>

  <nav class="flex gap-2 overflow-x-auto p-3">
    <button th:each="sub, i : ${subCategories}" class="btn btn-sm whitespace-nowrap"
            th:classappend="${i.first} ? 'btn-primary'"
            th:hx-get="${base} + '/menu?cat=' + ${sub.id}" hx-target="#menu-grid"
            th:text="${sub.name}">커피</button>
  </nav>

  <div id="menu-grid"
       th:hx-get="${base} + '/menu?cat=' + ${subCategories[0].id}"
       hx-trigger="load" hx-swap="innerHTML"></div>

  <div id="cart-bar" class="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] p-3 bg-base-100 border-t">
    <button id="submit-btn" class="btn btn-primary w-full" onclick="submitOrder()" disabled>
      담은 메뉴 없음
    </button>
  </div>
  <dialog id="opt-dialog" class="modal modal-bottom"><div id="opt-content" class="modal-box"></div></dialog>
</div>
```

- [ ] **Step 6: menu-grid.html fragment 작성**

```html
<!-- menu-grid.html — 카테고리별 카드 그리드 fragment -->
<div th:fragment="grid" xmlns:th="http://www.thymeleaf.org"
     class="grid grid-cols-2 gap-2 p-3"
     th:with="base=@{/{c}/{t}(c=${category}, t=${team})}">
  <button th:each="item : ${subCategory.items}"
          class="card bg-base-200 p-3 text-left active:scale-95 min-h-[44px]"
          th:hx-get="${base} + '/menu/' + ${item.id} + '/options'"
          hx-target="#opt-content" onclick="document.getElementById('opt-dialog').showModal()">
    <div class="font-medium text-sm" th:text="${item.name}">아메리카노</div>
    <div class="text-xs opacity-70" th:text="${#numbers.formatInteger(item.price,0,'COMMA')} + '원'">1,600원</div>
    <div class="text-xs mt-1">
      <span th:if="${item.fixedTemp == 'ice'}" class="badge badge-info badge-xs">ICE</span>
      <span th:if="${item.fixedTemp == 'hot'}" class="badge badge-error badge-xs">HOT</span>
      <span th:if="${item.fixedTemp == null}" class="badge badge-ghost badge-xs">HOT/ICE</span>
    </div>
  </button>
</div>
```

- [ ] **Step 7: option-modal.html fragment 작성**

```html
<!-- option-modal.html — 옵션 선택 하단시트 모달 fragment -->
<div th:fragment="modal" xmlns:th="http://www.thymeleaf.org" th:attr="data-item=${item.id},data-price=${item.price},data-name=${item.name}">
  <h3 class="font-bold text-lg" th:text="${item.name}">아메리카노</h3>
  <form id="opt-form" class="py-2 space-y-3">
    <div th:each="optKey : ${item.options}" th:with="def=${optionDefs.get(optKey)}"
         th:if="${!(optKey == 'temp' and item.fixedTemp != null)}">
      <div class="text-sm font-medium" th:text="${def.label}">옵션</div>
      <!-- single -->
      <div th:if="${def.type == 'single'}" class="flex flex-wrap gap-1 mt-1">
        <label th:each="ch : ${def.choices}" class="btn btn-sm">
          <input type="radio" class="hidden" th:name="${optKey}" th:value="${ch.id}"> 
          <span th:text="${ch.name}">HOT</span>
        </label>
      </div>
      <!-- toggle -->
      <input th:if="${def.type == 'toggle'}" type="checkbox" class="toggle mt-1" th:name="${optKey}">
      <!-- counter -->
      <input th:if="${def.type == 'counter'}" type="number" min="0" th:max="${def.max}" value="0"
             class="input input-bordered input-sm w-20 mt-1" th:name="${optKey}">
    </div>
  </form>
  <div class="modal-action">
    <button class="btn" onclick="document.getElementById('opt-dialog').close()">취소</button>
    <button class="btn btn-primary" onclick="addToCart()">담기</button>
  </div>
</div>
```

- [ ] **Step 8: 테스트 통과 확인**

Run: `./gradlew test --tests BoardControllerTest`
Expected: 3 tests PASS. (옵션 fragment의 "HOT"은 temp single 보기에서 렌더됨.)

- [ ] **Step 9: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/menu/MenuService.java src/main/java/dev/sybaek/mukja/order/OrderController.java src/main/resources/templates/order/board.html src/main/resources/templates/order/fragments src/test/java/dev/sybaek/mukja/order/BoardControllerTest.java
git commit -m "feat: 주문판 화면 + 메뉴 그리드/옵션 모달 fragment"
```

---

### Task 10: 주문 제출 (OrderService + POST) (TDD)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/order/OrderService.java`
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Create: `src/main/resources/static/js/order.js`
- Test: `src/test/java/dev/sybaek/mukja/order/OrderSubmitTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// OrderSubmitTest.java — 주문 제출 시 가격 계산·옵션텍스트·저장 검증
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.menu.MenuService;
import dev.sybaek.mukja.menu.OptionTextBuilder;
import dev.sybaek.mukja.menu.PriceCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderSubmitTest {
    @Autowired OrderService service;
    @Autowired OrderRepository repo;

    @Test
    void submitComputesLineTotalAndText() {
        repo.reset("coffee", "test-team");
        var line = new OrderService.LineInput(102, Map.of("temp","ice","shot",1));
        service.submit("coffee", "test-team", "백상열", List.of(line));
        var board = repo.read("coffee", "test-team");
        assertEquals(1, board.orders().size());
        var saved = board.orders().get(0).lines().get(0);
        assertEquals(1600 + 500 + 500, saved.lineTotal());
        assertTrue(saved.optionText().contains("ICE"));
        repo.reset("coffee", "test-team");
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests OrderSubmitTest`
Expected: 컴파일 실패.

- [ ] **Step 3: OrderService 구현**

```java
// OrderService.java — 주문 제출 (가격 계산 + 옵션 텍스트 + 저장)
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.menu.MenuService;
import dev.sybaek.mukja.menu.OptionTextBuilder;
import dev.sybaek.mukja.menu.PriceCalculator;
import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.order.domain.OrderEntry;
import dev.sybaek.mukja.order.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final MenuService menu;
    private final PriceCalculator price;
    private final OptionTextBuilder optionText;

    public OrderService(OrderRepository repo, MenuService menu,
                        PriceCalculator price, OptionTextBuilder optionText) {
        this.repo = repo; this.menu = menu; this.price = price; this.optionText = optionText;
    }

    // 한 줄 주문 입력 (itemId + 선택옵션)
    public record LineInput(int itemId, Map<String, Object> options) {}

    // 주문을 가격 계산·옵션텍스트와 함께 보드에 제출한다
    public void submit(String category, String team, String person, List<LineInput> inputs) {
        var defs = menu.data().optionDefs();
        List<OrderLine> lines = inputs.stream().map(in -> {
            MenuItem item = menu.item(in.itemId()).orElseThrow();
            int total = price.calc(item, defs, in.options());
            String text = optionText.build(item, defs, in.options());
            return new OrderLine(item.id(), item.name(), item.price(), in.options(), text, total);
        }).toList();
        repo.submit(category, team, new OrderEntry(person, OffsetDateTime.now(), lines));
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests OrderSubmitTest`
Expected: PASS.

- [ ] **Step 5: POST /orders 엔드포인트 추가**

`OrderController.java`에 추가 (OrderService 주입 필드/생성자 인자 추가):

```java
    // (필드) private final OrderService orderService;  // 생성자에도 추가

    // 주문 제출/수정 (HTMX). 마감 시 409
    @org.springframework.web.bind.annotation.PostMapping("/{category}/{team}/orders")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> submit(
            @PathVariable String category, @PathVariable String team,
            @org.springframework.web.bind.annotation.RequestBody SubmitRequest body) {
        try {
            orderService.submit(category, team, body.person(), body.lines());
            return org.springframework.http.ResponseEntity.ok("ok");
        } catch (BoardClosedException e) {
            return org.springframework.http.ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // 주문 제출 요청 바디
    public record SubmitRequest(String person, java.util.List<OrderService.LineInput> lines) {}
```

- [ ] **Step 6: order.js 작성 (장바구니/제출/타이머/모달)**

```javascript
// order.js — HTMX 보조: 장바구니 상태, 제출, 카운트다운, 옵션 모달
(function () {
  const cart = []; // {itemId, name, price, options, lineTotal, optionText}
  const PERSON_KEY = 'mukja.person';

  document.addEventListener('DOMContentLoaded', () => {
    const personInput = document.getElementById('person');
    if (personInput) {
      personInput.value = localStorage.getItem(PERSON_KEY) || '';
      personInput.addEventListener('input', () => localStorage.setItem(PERSON_KEY, personInput.value));
    }
    startCountdown();
  });

  // 옵션 모달의 폼에서 선택값을 읽어 장바구니에 담는다
  window.addToCart = function () {
    const box = document.querySelector('#opt-content > div, #opt-content');
    const root = document.querySelector('#opt-content [data-item]');
    const form = document.getElementById('opt-form');
    const data = new FormData(form);
    const options = {};
    for (const [k, v] of data.entries()) {
      if (v === 'on') options[k] = true;
      else if (/^\d+$/.test(v)) options[k] = parseInt(v, 10);
      else options[k] = v;
    }
    cart.push({ itemId: parseInt(root.dataset.item, 10), name: root.dataset.name, options });
    document.getElementById('opt-dialog').close();
    renderCart();
  };

  // 하단 바 갱신
  function renderCart() {
    const btn = document.getElementById('submit-btn');
    if (cart.length === 0) { btn.disabled = true; btn.textContent = '담은 메뉴 없음'; return; }
    btn.disabled = false;
    const first = cart[0].name;
    btn.textContent = cart.length === 1 ? `${first} 주문하기`
      : `${first} 외 ${cart.length - 1}건 주문하기`;
  }

  // 주문 제출 (서버에서 가격 계산)
  window.submitOrder = async function () {
    const person = (document.getElementById('person').value || '').trim();
    if (!person) { alert('이름을 입력하세요'); return; }
    const res = await fetch(location.pathname + '/orders', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ person, lines: cart.map(c => ({ itemId: c.itemId, options: c.options })) })
    });
    if (res.status === 409) { alert('마감된 주문판입니다'); return; }
    if (res.ok) { alert('주문 완료!'); cart.length = 0; renderCart(); }
  };

  // 마감 카운트다운 (10분 이내 주황)
  function startCountdown() {
    const el = document.getElementById('countdown');
    if (!el || !el.dataset.close) return;
    const close = new Date(el.dataset.close).getTime();
    setInterval(() => {
      const diff = close - Date.now();
      if (diff <= 0) { el.textContent = '마감'; el.className = 'badge'; return; }
      const m = Math.floor(diff / 60000), s = Math.floor((diff % 60000) / 1000);
      el.textContent = `${m}:${String(s).padStart(2, '0')}`;
      el.className = 'badge ' + (diff <= 600000 ? 'badge-warning' : 'badge-success');
    }, 1000);
  }

  // 마감 설정/해제/초기화
  window.setDeadline = async function () {
    const v = prompt('마감 시각 (HH:MM)', '14:30'); if (!v) return;
    await fetch(location.pathname + '/deadline', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ time: v }) });
    location.reload();
  };
  window.clearDeadline = async function () {
    await fetch(location.pathname + '/deadline', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ time: null }) });
    location.reload();
  };
  window.resetBoard = async function () {
    if (!confirm('이 주문판을 초기화할까요? 담긴 주문이 모두 지워집니다.')) return;
    await fetch(location.pathname + '/reset', { method: 'POST' });
    location.reload();
  };

  // single 옵션 라디오 선택 시 버튼 강조
  document.addEventListener('change', e => {
    if (e.target.type === 'radio') {
      document.querySelectorAll(`[name="${e.target.name}"]`).forEach(r =>
        r.closest('label')?.classList.toggle('btn-primary', r.checked));
    }
  });
})();
```

- [ ] **Step 7: 단일선택 라디오 검증 — 수동 확인 노트**

`./gradlew bootRun` 후 브라우저에서 `/coffee/sa` → 메뉴 카드 → 옵션 모달에서 HOT/ICE 선택 → 담기 → 하단 바 갱신 → 주문하기. (E2E는 Task 16에서 Playwright로 자동화)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/OrderService.java src/main/java/dev/sybaek/mukja/order/OrderController.java src/main/resources/static/js/order.js src/test/java/dev/sybaek/mukja/order/OrderSubmitTest.java
git commit -m "feat: 주문 제출 (서버 가격계산) + 장바구니 JS"
```

---

## M3 — 집계/발주 화면 & 실시간

### Task 11: OrderAggregator (TDD)

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/order/domain/{MenuAgg,Stats,Aggregation}.java`
- Create: `src/main/java/dev/sybaek/mukja/order/OrderAggregator.java`
- Test: `src/test/java/dev/sybaek/mukja/order/OrderAggregatorTest.java`

- [ ] **Step 1: 집계 결과 record 작성**

```java
// MenuAgg.java — 메뉴별 집계 (총 잔수 + 옵션 분해)
package dev.sybaek.mukja.order.domain;
import java.util.Map;
public record MenuAgg(String name, int totalCount, Map<String, Integer> optionBreakdown) {}
```
```java
// Stats.java — 전체 통계
package dev.sybaek.mukja.order.domain;
public record Stats(int people, int cups, int totalAmount, int perPersonAmount) {}
```
```java
// Aggregation.java — 집계 결과 묶음
package dev.sybaek.mukja.order.domain;
import java.util.List;
import java.util.Map;
public record Aggregation(List<MenuAgg> byMenu, Map<String, List<OrderLine>> byPerson,
                          Stats stats, String summaryText) {}
```

- [ ] **Step 2: 실패하는 테스트 작성**

```java
// OrderAggregatorTest.java — 메뉴별/사람별 집계 + 요약 텍스트 검증
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.order.domain.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderAggregatorTest {
    private OrderEntry e(String p, OrderLine... lines) {
        return new OrderEntry(p, OffsetDateTime.now(), List.of(lines));
    }
    private OrderLine l(String name, String optText, int total) {
        return new OrderLine(1, name, total, Map.of(), optText, total);
    }

    @Test
    void aggregatesByMenuAndPersonAndStats() {
        var board = new BoardData(null, List.of(
            e("백상열", l("아메리카노","ICE",1600), l("카페라떼","HOT",2100)),
            e("김철수", l("아메리카노","HOT",1600))));
        var agg = new OrderAggregator().aggregate("KT 분당 카페", "커피", "SA팀", board);

        assertEquals(2, agg.stats().people());
        assertEquals(3, agg.stats().cups());
        assertEquals(5300, agg.stats().totalAmount());
        var americano = agg.byMenu().stream().filter(m -> m.name().equals("아메리카노")).findFirst().orElseThrow();
        assertEquals(2, americano.totalCount());
        assertEquals(1, americano.optionBreakdown().get("ICE"));
        assertEquals(1, americano.optionBreakdown().get("HOT"));
        assertEquals(2, agg.byPerson().get("백상열").size());
        assertTrue(agg.summaryText().contains("[KT 분당 카페 · 커피 · SA팀]"));
        assertTrue(agg.summaryText().contains("8명") == false);
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `./gradlew test --tests OrderAggregatorTest`
Expected: 컴파일 실패.

- [ ] **Step 4: OrderAggregator 구현**

```java
// OrderAggregator.java — 보드 주문을 메뉴별/사람별로 집계하고 요약 텍스트를 만든다
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.order.domain.*;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderAggregator {
    // 장소·카테고리·팀 라벨과 보드 데이터로 집계 결과를 만든다
    public Aggregation aggregate(String place, String categoryName, String teamName, BoardData board) {
        var lines = board.orders().stream().flatMap(o -> o.lines().stream()).toList();

        // 메뉴별 집계 (등장 순서 유지)
        Map<String, MenuAcc> accs = new LinkedHashMap<>();
        for (var line : lines) {
            var acc = accs.computeIfAbsent(line.name(), k -> new MenuAcc());
            acc.count++;
            String opt = line.optionText().isBlank() ? "기본" : line.optionText();
            acc.breakdown.merge(opt, 1, Integer::sum);
        }
        List<MenuAgg> byMenu = accs.entrySet().stream()
                .map(en -> new MenuAgg(en.getKey(), en.getValue().count, en.getValue().breakdown)).toList();

        // 사람별 집계
        Map<String, List<OrderLine>> byPerson = new LinkedHashMap<>();
        for (var o : board.orders()) byPerson.put(o.person(), o.lines());

        int total = lines.stream().mapToInt(OrderLine::lineTotal).sum();
        int people = board.orders().size();
        Stats stats = new Stats(people, lines.size(), total, people == 0 ? 0 : total / people);

        return new Aggregation(byMenu, byPerson, stats,
                summary(place, categoryName, teamName, byMenu, stats));
    }

    // 복사용 요약 텍스트 생성
    private String summary(String place, String categoryName, String teamName,
                           List<MenuAgg> byMenu, Stats stats) {
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(place).append(" · ").append(categoryName)
          .append(" · ").append(teamName).append("]\n");
        for (var m : byMenu) {
            String detail = m.optionBreakdown().entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", "));
            sb.append("· ").append(m.name()).append(" ").append(m.totalCount()).append("잔 (")
              .append(detail).append(")\n");
        }
        sb.append("합계 ").append(nf.format(stats.totalAmount())).append("원 · ")
          .append(stats.people()).append("명");
        return sb.toString();
    }

    // 메뉴 집계용 가변 누산기
    private static class MenuAcc {
        int count = 0;
        Map<String, Integer> breakdown = new LinkedHashMap<>();
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests OrderAggregatorTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/domain/MenuAgg.java src/main/java/dev/sybaek/mukja/order/domain/Stats.java src/main/java/dev/sybaek/mukja/order/domain/Aggregation.java src/main/java/dev/sybaek/mukja/order/OrderAggregator.java src/test/java/dev/sybaek/mukja/order/OrderAggregatorTest.java
git commit -m "feat: OrderAggregator (메뉴별/사람별 집계 + 요약 텍스트)"
```

---

### Task 12: 집계 화면 + summary.txt

**Files:**
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Create: `src/main/resources/templates/order/status.html`
- Test: `src/test/java/dev/sybaek/mukja/order/StatusControllerTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// StatusControllerTest.java — 집계 화면 및 summary.txt 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerTest {
    @Autowired MockMvc mvc;
    @Autowired OrderService service;
    @Autowired OrderRepository repo;

    @BeforeEach void seed() {
        repo.reset("coffee","stat-team");
        service.submit("coffee","stat-team","백상열",
            java.util.List.of(new OrderService.LineInput(102, java.util.Map.of("temp","ice"))));
    }
    @AfterEach void clean() { repo.reset("coffee","stat-team"); }

    @Test
    void statusPageShowsStats() throws Exception {
        mvc.perform(get("/coffee/stat-team/status")).andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")));
    }

    @Test
    void summaryTxtIsPlainText() throws Exception {
        mvc.perform(get("/coffee/stat-team/status/summary.txt")).andExpect(status().isOk())
           .andExpect(content().string(containsString("[KT")))
           .andExpect(content().string(containsString("1명")));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests StatusControllerTest`
Expected: 404.

- [ ] **Step 3: 컨트롤러에 status 라우트 추가**

`OrderController.java`에 추가 (OrderAggregator 주입):

```java
    // (필드) private final OrderAggregator aggregator;  // 생성자에도 추가

    // 보드 집계 결과를 만든다 (헬퍼)
    private dev.sybaek.mukja.order.domain.Aggregation aggregate(String category, String team) {
        var data = menuService.data();
        String place = data.place() != null ? data.place().name() : "";
        return aggregator.aggregate(place, "커피", teamName(team), orderRepository.read(category, team));
    }

    // 집계/발주 화면
    @GetMapping("/{category}/{team}/status")
    public String status(@PathVariable String category, @PathVariable String team, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("agg", aggregate(category, team));
        return "order/status";
    }

    // 복사용 요약 텍스트
    @GetMapping(value = "/{category}/{team}/status/summary.txt", produces = "text/plain;charset=UTF-8")
    @org.springframework.web.bind.annotation.ResponseBody
    public String summary(@PathVariable String category, @PathVariable String team) {
        return aggregate(category, team).summaryText();
    }
```

- [ ] **Step 4: status.html 작성**

```html
<!-- status.html — 당번 집계/발주 화면 -->
<div th:fragment="content" class="p-3 space-y-4" th:with="s=${agg.stats}">
  <header class="flex items-center justify-between">
    <h1 class="font-bold" th:text="'☕ 커피 · ' + ${teamName}">집계</h1>
    <button class="btn btn-sm btn-primary" onclick="copySummary()">주문 요약 복사</button>
  </header>
  <div class="stats shadow w-full text-center">
    <div class="stat p-2"><div class="stat-title text-xs">인원</div>
      <div class="stat-value text-lg" th:text="${s.people}">8</div></div>
    <div class="stat p-2"><div class="stat-title text-xs">잔수</div>
      <div class="stat-value text-lg" th:text="${s.cups}">12</div></div>
    <div class="stat p-2"><div class="stat-title text-xs">총액</div>
      <div class="stat-value text-lg" th:text="${#numbers.formatInteger(s.totalAmount,0,'COMMA')}">48,500</div></div>
  </div>
  <p class="text-xs opacity-70" th:text="'1인당 약 ' + ${#numbers.formatInteger(s.perPersonAmount,0,'COMMA')} + '원'">1인당</p>

  <section>
    <h2 class="font-semibold mb-2">메뉴별</h2>
    <div th:each="m : ${agg.byMenu}" class="card bg-base-200 p-3 mb-2">
      <div class="flex justify-between"><span th:text="${m.name}">아메리카노</span>
        <span class="badge" th:text="${m.totalCount} + '잔'">5잔</span></div>
      <div class="text-xs opacity-70" th:text="${#strings.listJoin(
            m.optionBreakdown.entrySet().![key + ' ' + value], ', ')}">ICE 3, HOT 2</div>
    </div>
  </section>

  <section>
    <h2 class="font-semibold mb-2">사람별 (배분용)</h2>
    <div th:each="entry : ${agg.byPerson}" class="border-b py-2">
      <div class="font-medium" th:text="${entry.key}">백상열</div>
      <ul class="text-sm opacity-80">
        <li th:each="line : ${entry.value}" th:text="${line.name} + ' · ' + ${line.optionText}">아메리카노 · ICE</li>
      </ul>
    </div>
  </section>

  <div id="sse" hx-ext="sse" th:attr="sse-connect=@{/{c}/{t}/status/stream(c=${category},t=${team})}"
       sse-swap="order-update" hx-target="body" hx-swap="none"></div>
  <script>
    // 요약 텍스트 클립보드 복사 (2초 완료 표시)
    async function copySummary() {
      const res = await fetch(location.pathname + '/summary.txt');
      await navigator.clipboard.writeText(await res.text());
      const btn = event.target; const old = btn.textContent;
      btn.textContent = '복사됨 ✓'; setTimeout(() => btn.textContent = old, 2000);
    }
    // SSE 수신 시 새로고침 (폴백: 5초 폴링)
    const es = document.querySelector('#sse');
  </script>
</div>
```

> 참고: SSE 수신 후 갱신은 Task 13에서 order.js에 EventSource 구독 + 페이지 reload 로직으로 마무리한다. 위 인라인 스크립트는 복사 기능만 담당.

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests StatusControllerTest`
Expected: 2 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/OrderController.java src/main/resources/templates/order/status.html src/test/java/dev/sybaek/mukja/order/StatusControllerTest.java
git commit -m "feat: 집계/발주 화면 + summary.txt"
```

---

### Task 13: 보드별 SSE 실시간 갱신

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/order/sse/OrderSseService.java`
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java` (stream 엔드포인트 + broadcast 호출)
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderService.java` (제출 후 broadcast)
- Modify: `src/main/resources/static/js/order.js` (EventSource 구독)
- Test: `src/test/java/dev/sybaek/mukja/order/OrderSseServiceTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// OrderSseServiceTest.java — 보드별 emitter 등록/브로드캐스트 검증
package dev.sybaek.mukja.order.sse;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class OrderSseServiceTest {
    @Test
    void subscribeReturnsEmitterAndBroadcastDoesNotThrow() {
        OrderSseService sse = new OrderSseService();
        SseEmitter emitter = sse.subscribe("coffee", "sa");
        assertNotNull(emitter);
        assertDoesNotThrow(() -> sse.broadcast("coffee", "sa")); // 구독자에게 전송
        assertDoesNotThrow(() -> sse.broadcast("coffee", "other")); // 구독자 없어도 안전
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests OrderSseServiceTest`
Expected: 컴파일 실패.

- [ ] **Step 3: OrderSseService 구현**

```java
// OrderSseService.java — 보드별 SseEmitter 관리 및 주문 변경 broadcast
package dev.sybaek.mukja.order.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderSseService {
    private static final long TIMEOUT = 30 * 60 * 1000L;
    private final Map<String, List<SseEmitter>> boards = new ConcurrentHashMap<>();

    // 보드(category-team) 구독자를 등록한다
    public SseEmitter subscribe(String category, String team) {
        String key = category + "-" + team;
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        var list = boards.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);
        emitter.onCompletion(() -> list.remove(emitter));
        emitter.onTimeout(() -> list.remove(emitter));
        emitter.onError(e -> list.remove(emitter));
        return emitter;
    }

    // 해당 보드 구독자 전원에게 order-update 이벤트를 보낸다
    public void broadcast(String category, String team) {
        var list = boards.get(category + "-" + team);
        if (list == null) return;
        for (SseEmitter emitter : list) {
            try { emitter.send(SseEmitter.event().name("order-update").data("updated")); }
            catch (IOException e) { list.remove(emitter); }
        }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests OrderSseServiceTest`
Expected: PASS.

- [ ] **Step 5: stream 엔드포인트 + 제출/마감/초기화 후 broadcast 연결**

`OrderController.java`에 추가 (OrderSseService 주입):

```java
    // (필드) private final dev.sybaek.mukja.order.sse.OrderSseService sse;  // 생성자에도 추가

    // SSE 구독 (보드별)
    @GetMapping("/{category}/{team}/status/stream")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter stream(
            @PathVariable String category, @PathVariable String team) {
        return sse.subscribe(category, team);
    }
```

`OrderService.submit(...)` 마지막 줄(repo.submit 이후)에 broadcast 추가 — OrderService에 OrderSseService 주입 후:

```java
        repo.submit(category, team, new OrderEntry(person, OffsetDateTime.now(), lines));
        sse.broadcast(category, team);  // 추가
```

- [ ] **Step 6: order.js에 SSE 구독 추가**

`order.js`의 `DOMContentLoaded` 핸들러 안에 추가:

```javascript
    // 집계 화면에서 SSE 구독 → 갱신 시 새로고침 (폴백: 5초 폴링)
    const sseEl = document.getElementById('sse');
    if (sseEl && sseEl.getAttribute('sse-connect')) {
      try {
        const es = new EventSource(sseEl.getAttribute('sse-connect'));
        es.addEventListener('order-update', () => location.reload());
        es.onerror = () => { es.close(); setInterval(() => location.reload(), 5000); };
      } catch (e) { setInterval(() => location.reload(), 5000); }
    }
```

- [ ] **Step 7: 전체 테스트 + 수동 확인**

Run: `./gradlew test`
Expected: 전체 PASS. 수동: 두 탭에서 `/coffee/sa`(주문)과 `/coffee/sa/status`(집계)를 열고 주문 시 집계가 자동 갱신되는지 확인.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/sse src/main/java/dev/sybaek/mukja/order/OrderController.java src/main/java/dev/sybaek/mukja/order/OrderService.java src/main/resources/static/js/order.js src/test/java/dev/sybaek/mukja/order/OrderSseServiceTest.java
git commit -m "feat: 보드별 SSE 실시간 갱신 + 폴링 폴백"
```

---

## M4 — 당번 컨트롤 & 관리자

### Task 14: 마감 설정/해제 + 초기화 엔드포인트

**Files:**
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Test: `src/test/java/dev/sybaek/mukja/order/DeadlineResetControllerTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// DeadlineResetControllerTest.java — 마감 설정/해제/초기화 엔드포인트 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeadlineResetControllerTest {
    @Autowired MockMvc mvc;
    @Autowired OrderRepository repo;

    @AfterEach void clean() { repo.reset("coffee","dl-team"); }

    @Test
    void setDeadlineThenClear() throws Exception {
        mvc.perform(post("/coffee/dl-team/deadline").contentType("application/json")
                .content("{\"time\":\"14:30\"}")).andExpect(status().isOk());
        Assertions.assertNotNull(repo.read("coffee","dl-team").closeAt());
        mvc.perform(post("/coffee/dl-team/deadline").contentType("application/json")
                .content("{\"time\":null}")).andExpect(status().isOk());
        Assertions.assertNull(repo.read("coffee","dl-team").closeAt());
    }

    @Test
    void resetClearsBoard() throws Exception {
        mvc.perform(post("/coffee/dl-team/reset")).andExpect(status().isOk());
        Assertions.assertTrue(repo.read("coffee","dl-team").orders().isEmpty());
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests DeadlineResetControllerTest`
Expected: 404.

- [ ] **Step 3: 엔드포인트 추가**

`OrderController.java`에 추가:

```java
    // 마감 시각 설정/해제. time이 "HH:MM"이면 오늘 KST 기준으로 설정, null이면 해제
    @org.springframework.web.bind.annotation.PostMapping("/{category}/{team}/deadline")
    @org.springframework.web.bind.annotation.ResponseBody
    public String deadline(@PathVariable String category, @PathVariable String team,
                           @org.springframework.web.bind.annotation.RequestBody DeadlineRequest body) {
        if (body.time() == null || body.time().isBlank()) {
            orderRepository.clearDeadline(category, team);
        } else {
            var parts = body.time().split(":");
            var now = java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
            var close = now.withHour(Integer.parseInt(parts[0])).withMinute(Integer.parseInt(parts[1]))
                    .withSecond(0).withNano(0);
            orderRepository.setDeadline(category, team, close);
        }
        sse.broadcast(category, team);
        return "ok";
    }

    // 주문판 초기화 (PIN 없음)
    @org.springframework.web.bind.annotation.PostMapping("/{category}/{team}/reset")
    @org.springframework.web.bind.annotation.ResponseBody
    public String reset(@PathVariable String category, @PathVariable String team) {
        orderRepository.reset(category, team);
        sse.broadcast(category, team);
        return "ok";
    }

    // 마감 설정 요청 바디
    public record DeadlineRequest(String time) {}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests DeadlineResetControllerTest`
Expected: 2 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/order/OrderController.java src/test/java/dev/sybaek/mukja/order/DeadlineResetControllerTest.java
git commit -m "feat: 마감 설정/해제 + 초기화 엔드포인트 (PIN 없음)"
```

---

### Task 15: PIN 인터셉터 + 관리자 메뉴 CRUD

**Files:**
- Create: `src/main/java/dev/sybaek/mukja/config/AdminPinInterceptor.java`
- Create: `src/main/java/dev/sybaek/mukja/config/WebConfig.java`
- Create: `src/main/java/dev/sybaek/mukja/admin/AdminController.java`
- Create: `src/main/resources/templates/admin/{login.html,index.html}`
- Test: `src/test/java/dev/sybaek/mukja/admin/AdminPinTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// AdminPinTest.java — PIN 없이 /admin 접근 차단, 올바른 PIN 후 접근 허용
package dev.sybaek.mukja.admin;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPinTest {
    @Autowired MockMvc mvc;

    @Test
    void redirectsToLoginWithoutPin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }

    @Test
    void allowsWithValidPinCookie() throws Exception {
        mvc.perform(get("/admin").cookie(new Cookie("admin", "1234")))
           .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests AdminPinTest`
Expected: 실패(인터셉터/컨트롤러 없음 → 200 또는 404).

- [ ] **Step 3: AdminPinInterceptor 구현**

```java
// AdminPinInterceptor.java — /admin/** 접근을 PIN 쿠키로 보호
package dev.sybaek.mukja.config;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminPinInterceptor implements HandlerInterceptor {
    private final String pin;
    public AdminPinInterceptor(String pin) { this.pin = pin; }

    // admin 쿠키 값이 설정 PIN과 일치하지 않으면 로그인으로 리다이렉트한다
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getRequestURI().startsWith("/admin/login") || req.getRequestURI().equals("/admin/auth"))
            return true;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("admin".equals(c.getName()) && pin.equals(c.getValue())) return true;
            }
        }
        res.sendRedirect("/admin/login");
        return false;
    }
}
```

- [ ] **Step 4: WebConfig 구현**

```java
// WebConfig.java — 인터셉터 등록
package dev.sybaek.mukja.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MukjaProperties props;
    public WebConfig(MukjaProperties props) { this.props = props; }

    // /admin/** 경로에 PIN 인터셉터를 적용한다
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminPinInterceptor(props.adminPin()))
                .addPathPatterns("/admin/**", "/admin");
    }
}
```

- [ ] **Step 5: AdminController + 템플릿 구현**

```java
// AdminController.java — 로그인 + 메뉴 관리
package dev.sybaek.mukja.admin;

import dev.sybaek.mukja.menu.MenuRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final MenuRepository menuRepository;
    public AdminController(MenuRepository menuRepository) { this.menuRepository = menuRepository; }

    // PIN 입력 화면
    @GetMapping("/login")
    public String login() { return "admin/login"; }

    // PIN 제출 → 쿠키 설정
    @PostMapping("/auth")
    public String auth(@RequestParam String pin, HttpServletResponse res) {
        Cookie cookie = new Cookie("admin", pin);
        cookie.setPath("/"); cookie.setHttpOnly(true);
        res.addCookie(cookie);
        return "redirect:/admin";
    }

    // 관리자 메인 (현재 메뉴 표시)
    @GetMapping
    public String index(Model model) {
        model.addAttribute("menu", menuRepository.load());
        return "admin/index";
    }
}
```
```html
<!-- login.html — 관리자 PIN 입력 -->
<!DOCTYPE html><html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><link rel="stylesheet" th:href="@{/css/app.css}"></head>
<body class="p-6"><form method="post" th:action="@{/admin/auth}" class="max-w-xs mx-auto space-y-2">
  <input name="pin" type="password" class="input input-bordered w-full" placeholder="PIN">
  <button class="btn btn-primary w-full">입력</button>
</form></body></html>
```
```html
<!-- index.html — 관리자 메인 (메뉴 목록) -->
<!DOCTYPE html><html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><link rel="stylesheet" th:href="@{/css/app.css}"></head>
<body class="p-6"><h1 class="font-bold mb-3">메뉴 관리</h1>
  <div th:each="cat : ${menu.categories}" class="mb-3">
    <h2 class="font-semibold" th:text="${cat.name}">커피</h2>
    <ul class="text-sm"><li th:each="i : ${cat.items}"
        th:text="${i.name} + ' — ' + ${i.price} + '원'">아메리카노 — 1600원</li></ul>
  </div>
  <p class="text-xs opacity-60">메뉴 편집은 data/menus.json을 직접 수정 후 재기동. (인라인 편집은 후속)</p>
</body></html>
```

> 메뉴 CRUD는 1차에서 읽기 + 파일 직접 편집으로 한정한다(YAGNI). 인라인 편집/추가/삭제 UI는 후속 작업으로 남긴다.

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests AdminPinTest`
Expected: 2 tests PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/dev/sybaek/mukja/config/AdminPinInterceptor.java src/main/java/dev/sybaek/mukja/config/WebConfig.java src/main/java/dev/sybaek/mukja/admin src/main/resources/templates/admin src/test/java/dev/sybaek/mukja/admin/AdminPinTest.java
git commit -m "feat: 관리자 PIN 인터셉터 + 메뉴 조회 화면"
```

---

## M5 — E2E & 배포

### Task 16: Playwright E2E (핵심 주문 흐름)

**Files:**
- Create: `e2e/package.json`, `e2e/playwright.config.ts`, `e2e/tests/order.spec.ts`

- [ ] **Step 1: Playwright 설치**

Run: `cd e2e && npm init -y && npm i -D @playwright/test && npx playwright install chromium`
Expected: Playwright 설치 완료.

- [ ] **Step 2: playwright.config.ts 작성**

```typescript
// playwright.config.ts — 로컬 8080 대상 E2E 설정
import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir: './tests',
  use: { baseURL: 'http://localhost:8080', viewport: { width: 390, height: 844 } },
});
```

- [ ] **Step 3: 주문 흐름 E2E 작성**

```typescript
// order.spec.ts — 카테고리→팀→주문→집계 핵심 흐름
import { test, expect } from '@playwright/test';

test('coffee order flow', async ({ page }) => {
  await page.goto('/');
  await page.getByText('커피').click();
  await page.getByText('SA팀').click();
  await page.getByPlaceholder('이름을 입력하세요').fill('테스트');
  await page.getByText('아메리카노').click();
  await page.getByText('HOT').click();
  await page.getByText('담기').click();
  await page.getByRole('button', { name: /주문하기/ }).click();
  await page.goto('/coffee/sa/status');
  await expect(page.getByText('아메리카노')).toBeVisible();
});
```

- [ ] **Step 4: 앱 기동 후 E2E 실행**

Run: (터미널 1) `./gradlew bootRun` / (터미널 2) `cd e2e && npx playwright test`
Expected: 1 passed. 실패 시 systematic-debugging으로 셀렉터/타이밍 수정.

- [ ] **Step 5: Commit**

```bash
git add e2e
git commit -m "test: Playwright E2E 주문 흐름"
```

---

### Task 17: Docker multi-stage 빌드 & compose

**Files:**
- Create: `Dockerfile`
- Create: `docker-compose.yml`
- Create: `.dockerignore`

- [ ] **Step 1: Dockerfile 작성 (node css → gradle jar → jre run)**

```dockerfile
# Dockerfile — multi-stage: CSS 빌드 → JAR 빌드 → 실행
FROM node:22-alpine AS css
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY src/main/css ./src/main/css
COPY src/main/resources/templates ./src/main/resources/templates
COPY src/main/resources/static ./src/main/resources/static
RUN npm run build:css

FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
COPY --from=css /app/src/main/resources/static/css/app.css ./src/main/resources/static/css/app.css
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV MUKJA_DATA_DIR=/data
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: docker-compose.yml 작성**

```yaml
# docker-compose.yml — mukja 서비스 + data 볼륨
services:
  mukja:
    build: .
    container_name: mukja
    ports: ["8080:8080"]
    environment:
      MUKJA_ADMIN_PIN: ${MUKJA_ADMIN_PIN:-1234}
      TZ: Asia/Seoul
    volumes:
      - mukja-data:/data
    restart: unless-stopped
volumes:
  mukja-data:
```

- [ ] **Step 3: .dockerignore 작성**

```
.git
node_modules
e2e/node_modules
build
.gradle
.superpowers
```

- [ ] **Step 4: 빌드 및 기동 검증**

Run: `docker compose up --build -d && sleep 15 && curl -s localhost:8080/ | grep -o '커피'`
Expected: `커피` 출력 (앱 정상 서빙). 이후 `docker compose down`.

- [ ] **Step 5: Commit**

```bash
git add Dockerfile docker-compose.yml .dockerignore
git commit -m "build: Docker multi-stage 빌드 + compose (data 볼륨)"
```

- [ ] **Step 6: 배포 노트 (수동)**

NPM(Nginx Proxy Manager)에서 서브도메인 `mukja.sybaek.dev` → `mukja:8080` 프록시 추가. (Mac Mini OrbStack 환경, 수동 1회 설정)

---

### Task 18: CLAUDE.md 작성 & PRD 정합화

**Files:**
- Create: `CLAUDE.md`
- Modify: `docs/PRD.md` (세션 모델 → 보드 모델 정합화, 이름 변경)

- [ ] **Step 1: CLAUDE.md 작성**

```markdown
# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview
mukja — 사내 커피/점심 주문을 카테고리×팀 상시 주문판으로 취합하는 모바일 우선 웹앱.
Spring Boot 3.5 + Thymeleaf SSR + HTMX, 데이터는 보드별 JSON 파일(JsonStore, RWLock).

## Build and Run Commands
- 실행: `./gradlew bootRun` (기본 8080)
- 테스트: `./gradlew test`
- CSS 빌드: `npm run build:css` (Tailwind4 + DaisyUI5 → static/css/app.css)
- E2E: `cd e2e && npx playwright test` (앱 기동 필요)
- Docker: `docker compose up --build`

## Architecture
- 주문판(Board) = (카테고리, 팀). 저장: `data/orders/{category}-{team}.json`.
- 세션 개념 없음. 마감(closeAt) 경과 시 제출 409. 수동 초기화로 재사용.
- JSON 접근은 항상 `JsonStore<T>` 경유 (직접 ObjectMapper 호출 금지).
- 가격/옵션텍스트는 데이터 주도(PriceCalculator/OptionTextBuilder) — menus.json만 바꾸면 화면이 따라옴.
- 실시간: 보드별 SSE(OrderSseService) + 폴링 폴백.

## Key Configuration
- `mukja.data-dir`(기본 ./data), `mukja.admin-pin`(env MUKJA_ADMIN_PIN), `mukja.teams`(목록, 'all' 포함).

## API Endpoints
- 네비: `/`, `/{category}`, `/{category}/{team}`
- 주문: `POST /{category}/{team}/orders` / 집계: `/{category}/{team}/status(.txt/stream)`
- 당번: `POST .../deadline`, `POST .../reset` (PIN 없음)
- 관리: `/admin/**` (PIN 보호, 메뉴 조회)

## Important Notes
- 모든 파일 최상단에 역할 주석, 모든 함수 위에 기능 설명 주석 (한국어).
- Java 21 record 적극 활용, 생성자 주입, Lombok 미사용.
- 마감/초기화는 권한 없음(1차). 계좌/금융정보 저장 금지.
- 모든 시각 KST(Asia/Seoul) 직렬화.
- 커밋: 한 커밋에 한 관심사.

## Operational Decisions
- 세션 모델 폐기 사유·결정 기록은 `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md` 참고.
- 음식 카테고리는 menus.json에 group:"food" 데이터 추가 시 자동 동작(구조만 준비됨).
```

- [ ] **Step 2: PRD.md 정합화**

`docs/PRD.md`의 제목/메타(order-hub→mukja, 도메인, 패키지), 0절(세션 scope→카테고리×팀 보드), 2절(패키지), 3.2/3.3, 4절 라우트, 6절 화면, 10절 미해결을 설계 스펙(`docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md`)에 맞춰 갱신한다. 스펙과 모순되는 문장이 없도록 한다.

- [ ] **Step 3: 최종 전체 테스트**

Run: `./gradlew test`
Expected: 전체 PASS.

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md docs/PRD.md
git commit -m "docs: CLAUDE.md 작성 + PRD 보드 모델 정합화"
```

---

## Self-Review 결과

- **Spec 커버리지**: 보드 모델(T6), 저장 구조(T2/T6), 마감 차단(T6/T14), 초기화(T6/T14), `/{category}/{team}` 라우트(T8/T9/T12), '전체' 팀(application.yml T1), 메뉴 group(T3), 데이터 주도 가격(T5), 집계+요약(T11/T12), 보드별 SSE(T13), 모바일 UI/다크모드/이름기억(T7~T10), PIN 관리자(T15), 배포(T17), 이름 변경(T1·T18) — 모두 태스크에 매핑됨.
- **Placeholder 스캔**: 코드 스텝에 실제 코드 포함. 메뉴 인라인 CRUD와 NPM 프록시 설정은 의도적 범위 축소/수동 작업으로 명시(스펙의 out-of-scope·운영과 일치).
- **타입 일관성**: `BoardData(closeAt, orders)`, `OrderEntry(person, submittedAt, lines)`, `OrderLine(itemId,name,unitPrice,options,optionText,lineTotal)`, `OrderService.LineInput(itemId,options)`, `OrderRepository.{read,submit,setDeadline,clearDeadline,reset}`, `OrderSseService.{subscribe,broadcast}`, `OrderAggregator.aggregate(place,categoryName,teamName,board)` — 태스크 전반에서 시그니처 일치 확인.
