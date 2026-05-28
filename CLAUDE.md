# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview
mukja — 사내 커피/점심 주문을 **가게(vendor) × 팀(team) 단위 상시 주문판**으로 취합하는 모바일 우선 웹앱.
Spring Boot 3.5.3 + Thymeleaf SSR + HTMX 부분 갱신, 데이터는 보드별 JSON 파일(`JsonStore`, RWLock·atomic move)에 저장. 세션 생명주기 없이 보드는 상시 존재하고, 당번이 마감 시각을 설정하거나 매일 자정(KST) 자동 초기화로 재사용한다. 실시간은 보드별 SSE + 폴링 폴백.

## Build and Run Commands
- 실행: `./gradlew bootRun` (기본 8080)
- 테스트: `./gradlew test`
- Docker: `docker build -t mukja .` (배포는 GitHub Actions → self-hosted 러너에서 빌드·기동. 로컬 compose 없음)

> **Node/npm 미사용 프로젝트.** Tailwind 빌드 툴체인·E2E(Playwright)는 제거됨. CSS는 동결된 정적 산출물 `src/main/resources/static/css/app.css`(Tailwind/DaisyUI 컴파일 결과)와 손수 쓴 평문 CSS `app-custom.css`(app.css 뒤 로드)로 운영. htmx는 `org.webjars.npm:htmx.org`(Gradle 의존성, npm 아님)으로 제공.

> 주의: Gradle 래퍼는 8.14 핀(JDK 23 런타임 호환), 컴파일 toolchain은 JDK 21. 시스템에 JDK 21이 없으면 `~/.gradle/gradle.properties`의 `org.gradle.java.installations.paths`로 경로를 지정한다.

## Architecture
- **주문판(Board) = (가게 × 팀)** 조합. 저장: `data/orders/{vendor}-{team}.json` (보드마다 독립 `JsonStore`, 독립 락).
- **가게 레이어**: 상위 그룹 `coffee|food`(URL의 `{category}`) → 가게(`{vendor}`) → 메뉴 카테고리(`{sub}`) → 메뉴 → 옵션. 식당은 `phone` 필드를 가지면 집계의 "전화하기" 버튼 노출.
- 세션 개념 없음. 마감(`closeAt`) 경과 시 `POST .../orders`는 `BoardClosedException` → 409. 수동 초기화(`reset`)와 매일 00:00 KST 자동 초기화(`BoardResetScheduler`)가 `BoardData.empty()`로 주문·마감을 함께 비운다(`@EnableScheduling`).
- **JSON 접근은 항상 `JsonStore<T>` 경유** (직접 ObjectMapper 호출 금지). API: `read()`, `write(T)`, `mutate(UnaryOperator<T>)` (read-modify-write). 쓰기는 tmp 파일 → `ATOMIC_MOVE`.
- **데이터 주도 설계**: 가격/옵션텍스트는 `PriceCalculator`/`OptionTextBuilder`가 `menus.json`의 optionDefs로 계산 — menus.json만 바꾸면 화면·계산이 따라온다. counter 옵션은 클라이언트가 숫자/문자열 어떻게 보내도 안전 변환.
- 실시간: `OrderSseService`(보드별 SseEmitter) + `order.js`의 EventSource 구독, 실패 시 5초 폴링 폴백.
- 패키지: `com.mukja` — `common/store`(JsonStore), `config`(MukjaProperties/Jackson/Web/AdminPin), `menu`, `order`(+`order.sse`), `admin`.

## Key Configuration
- `mukja.data-dir` (기본 `./data`, env `MUKJA_DATA_DIR`)
- `mukja.admin-pin` (기본 `1234`, env `MUKJA_ADMIN_PIN`) — `/admin/**` 보호
- `mukja.teams` — 팀 목록(현재 `ice`/`kos`/`icis`/`vivaldi`). id는 URL 경로 세그먼트로 사용. 팀별 명단은 `teams.seed.json` → `data/teams.json`.
- 모든 시각 KST(Asia/Seoul) 직렬화 (`spring.jackson.time-zone`)
- 정적 자산 콘텐츠 해시 버저닝: `spring.web.resources.chain.strategy.content.enabled: true` → `/css/app-custom-<hash>.css` 식으로 서빙되어 CDN(Cloudflare) 캐시가 자동 무효화된다.

## API Endpoints
- 네비: `GET /` → `/coffee/{기본가게}/{기본팀}`, `GET /{category}` → 그룹의 첫 가게·기본팀, `GET /{category}/{vendor}/{team}`(단일 주문판: 가게 드로어 + 팀 알약 + 주문자 알약(가나다순·가로 슬라이드·드래그) + 주문/집계 토글)
- 주문: `POST /{category}/{vendor}/{team}/orders` (JSON, 마감 시 409, 빈 이름·복수 라인 400)
- 주문 취소: `POST /{category}/{vendor}/{team}/orders/delete`
- 메뉴 fragment: `GET /{category}/{vendor}/{team}/menu?cat={sub}`, `GET /{category}/{vendor}/{team}/menu/{itemId}/options`
- 집계: `GET /{category}/{vendor}/{team}/status`(HTMX `HX-Request` 헤더면 `order/status :: panel` fragment, 아니면 전체 페이지), `/status/summary.txt`, `/status/stream`(SSE)
- 당번(PIN 없음): `POST /{category}/{vendor}/{team}/deadline`, `POST /{category}/{vendor}/{team}/reset`
- 관리(PIN 보호): `GET /admin/login`, `POST /admin/auth`, `GET /admin`(메뉴 조회)
- `@GetMapping`의 category 세그먼트는 `{category:coffee|food}` 정규식으로 제약 — static(`/css`,`/js`,`/webjars`,`/img`) 경로 보호

## Important Notes
- 모든 파일 최상단에 역할 주석, 모든 함수 위에 기능 설명 주석 (한국어).
- Java 21 record 적극 활용, 생성자 주입, Lombok 미사용. 두 개 이상의 생성자를 가진 빈은 주입 생성자에 `@Autowired` 필수.
- 마감·초기화는 권한 없음(1차, 팀 내부 신뢰 전제). 계좌/금융정보 저장 금지 — 이름·주문만 보관.
- 1인 1메뉴(서버: `lines.size() != 1` → 400). 같은 person 재제출은 덮어쓰기.
- 템플릿 레이아웃: 전체 페이지는 `<html th:replace="~{layout :: page('제목', ~{:: #content})}">` + `<div id="content" th:fragment="content">`. fragment 응답은 `뷰 :: fragment` 반환.
- **스타일은 npm/CDN 없이 운영**: 동결 `static/css/app.css`를 재빌드하지 않고, 새 스타일은 손수 쓴 `static/css/app-custom.css`(app.css 뒤 로드)에 추가. 미컴파일 DaisyUI 클래스(`drawer*`, `select-bordered/sm` 등)는 쓰지 말고 평문 CSS로 직접 스타일링한다.
- **PC도 폰처럼**: `main.app-shell`이 480 컬럼이고, 하단시트(`.modal-bottom .modal-box`)·드로어(`#cat-drawer`, 열 때 `translateX(50vw-240px)`)·CTA(`.cta-bar` max-width 480)가 모두 컬럼에 정렬된다.
- 링크 미리보기/파비콘: layout.html `<head>`에 OG/Twitter 메타 + `/img/og.png`(1200×630), `/img/favicon.png` 링크. 카카오톡 등 메신저는 미리보기를 캐시하므로 옛 링크는 새로 공유해야 갱신.
- 커밋: 한 커밋에 한 관심사.

## Operational Decisions
- 보드 모델·UI 진화의 배경은 `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md`(보드 모델)와 `2026-05-26-mukja-single-board-layout-redesign.md`(단일 보드 UI)에 정리됨.
- 음식 카테고리는 데이터 확장으로 동작: `menus.json`에 `group:"food"` vendor와 `phone`만 추가하면 보드·전화하기 버튼이 자동 노출.
- 마감 세션 보관/이력 없음 — 매일 자정 자동 초기화 + 당번 수동 초기화로 재사용. PIN은 평문(내부망·민감정보 미저장). Jasypt/Cloudflare Access는 후속.
- 미완료: 관리자 인라인 메뉴 편집(현재 조회만, 편집은 menus.json 직접 수정 후 재기동), 자정 다운타임 보정(서버가 자정에 꺼져 있으면 그날 초기화는 건너뜀), 주문 이력/통계.
