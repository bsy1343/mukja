# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview
mukja — 사내 커피/점심 주문을 **카테고리×팀 단위 상시 주문판**으로 취합하는 모바일 우선 웹앱.
Spring Boot 3.5 + Thymeleaf SSR + HTMX 부분 갱신, 데이터는 보드별 JSON 파일(`JsonStore`, RWLock·atomic move)에 저장. 세션 생명주기 없이 보드는 상시 존재하고, 당번이 마감 시각 설정·수동 초기화로 재사용한다. 실시간은 보드별 SSE + 폴링 폴백.

## Build and Run Commands
- 실행: `./gradlew bootRun` (기본 8080)
- 테스트: `./gradlew test`
- Docker: `docker build -t mukja .` (배포는 CI/Deploy 워크플로우 → self-hosted 러너에서 빌드·기동. 로컬 compose 없음)

> **Node/npm 미사용 프로젝트.** Tailwind 빌드 툴체인·E2E(Playwright)는 제거됨. CSS는 정적 산출물 `src/main/resources/static/css/app.css`(동결, Tailwind/DaisyUI 컴파일 결과)를 그대로 서빙하고, 추가 스타일은 손수 쓴 `app-custom.css`에 평문 CSS로 작성한다. htmx는 `org.webjars.npm:htmx.org`(Gradle 의존성, npm 아님)으로 제공.

> 주의: Gradle 래퍼는 8.14 핀(JDK 23 런타임 호환), 컴파일 toolchain은 JDK 21. 시스템에 JDK 21이 없으면 `~/.gradle/gradle.properties`의 `org.gradle.java.installations.paths`로 경로를 지정한다.

## Architecture
- **주문판(Board) = (카테고리, 팀)** 조합. 저장: `data/orders/{category}-{team}.json` (보드마다 독립 `JsonStore`, 독립 락).
- 세션 개념 없음. 마감(`closeAt`) 경과 시 `POST .../orders`는 `BoardClosedException` → 409. 수동 초기화(`reset`)로 orders·closeAt 비우고 재사용.
- **JSON 접근은 항상 `JsonStore<T>` 경유** (직접 ObjectMapper 호출 금지). API: `read()`, `write(T)`, `mutate(UnaryOperator<T>)` (read-modify-write). 쓰기는 tmp 파일 → `ATOMIC_MOVE`.
- **데이터 주도 설계**: 가격/옵션텍스트는 `PriceCalculator`/`OptionTextBuilder`가 `menus.json`의 optionDefs로 계산 — menus.json만 바꾸면 화면·계산이 따라온다.
- 메뉴 카테고리는 `group: "coffee"|"food"`로 상위 카테고리를 구분. 음식은 데이터만 추가하면 동작(현재 커피만).
- 실시간: `OrderSseService`(보드별 SseEmitter) + `order.js`의 EventSource 구독, 실패 시 5초 폴링 폴백.
- 패키지: `com.mukja` — `common/store`(JsonStore), `config`(MukjaProperties/Jackson/Web/AdminPin), `menu`, `order`(+`order.sse`), `admin`.

## Key Configuration
- `mukja.data-dir` (기본 `./data`, env `MUKJA_DATA_DIR`)
- `mukja.admin-pin` (기본 `1234`, env `MUKJA_ADMIN_PIN`) — `/admin/**` 보호
- `mukja.teams` — 팀 목록(`all`/`sa`/`imdg` …). `all`이 전사 보드 역할. id는 URL 경로 세그먼트로 사용
- 모든 시각 KST(Asia/Seoul) 직렬화 (`spring.jackson.time-zone`)

## API Endpoints
- 네비: `GET /`→`/{기본팀}` 리다이렉트, `GET /{category}`→`/{category}/{기본팀}` 리다이렉트, `GET /{category}/{team}`(단일 주문판: 좌측 카테고리 드로어 + 상단 팀 드롭다운/이름 + 주문/집계 토글)
- 주문: `POST /{category}/{team}/orders` (JSON, 마감 시 409)
- 메뉴 fragment: `GET /{category}/{team}/menu?cat={sub}`, `GET /{category}/{team}/menu/{itemId}/options`
- 집계: `GET /{category}/{team}/status`(HTMX `HX-Request` 헤더면 `order/status :: panel` fragment, 아니면 전체 페이지), `/status/summary.txt`, `/status/stream`(SSE)
- 당번(PIN 없음): `POST /{category}/{team}/deadline`, `POST /{category}/{team}/reset`
- 관리(PIN 보호): `GET /admin/login`, `POST /admin/auth`, `GET /admin`(메뉴 조회)
- `@GetMapping`의 category 세그먼트는 `{category:coffee|food}` 정규식으로 제약 — static(`/css`,`/js`,`/webjars`) 경로 보호

## Important Notes
- 모든 파일 최상단에 역할 주석, 모든 함수 위에 기능 설명 주석 (한국어).
- Java 21 record 적극 활용, 생성자 주입, Lombok 미사용. 두 개 이상의 생성자를 가진 빈은 주입 생성자에 `@Autowired` 필수.
- 마감·초기화는 권한 없음(1차, 팀 내부 신뢰 전제). 계좌/금융정보 저장 금지 — 이름·주문만 보관.
- 템플릿 레이아웃: 전체 페이지는 `<html th:replace="~{layout :: page('제목', ~{:: #content})}">` + `<div id="content" th:fragment="content">`. fragment 응답은 `뷰 :: fragment` 반환.
- **스타일은 npm/CDN 없이 운영**: 기존 `static/css/app.css`(Tailwind+DaisyUI 산출물)는 동결해 그대로 사용하고 재빌드하지 않는다. 새 스타일이 필요하면 손수 쓴 평문 CSS를 `static/css/app-custom.css`(layout.html에서 app.css 뒤 로드)에 추가한다. 미컴파일 DaisyUI 클래스(`drawer*`, `select-bordered/sm` 등)는 쓰지 말고 app-custom.css로 직접 스타일링한다.
- 커밋: 한 커밋에 한 관심사.

## Operational Decisions
- 세션 모델 폐기 사유·전체 설계 결정은 `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md` 참고.
- 음식 카테고리: `menus.json`에 `group:"food"` 데이터 추가 시 자동 동작(구조만 준비).
- 마감 세션 보관/이력 없음 — 수동 초기화 재사용. PIN은 평문(내부망·민감정보 미저장). Jasypt/Cloudflare Access는 후속.
- 미완료: 관리자 인라인 메뉴 편집(현재 조회만, 편집은 menus.json 직접 수정 후 재기동), Docker 이미지 빌드 검증(로컬 자격증명 프롬프트로 미완), 음식 메뉴 실데이터.
