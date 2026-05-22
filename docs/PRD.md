# PRD: mukja — 사내 주문 취합 웹앱

**Version**: 2.0 (board model)
**Date**: 2026-05-22
**Project**: mukja
**Tech Stack**: JDK 21 · Spring Boot 3.5 · Thymeleaf · HTMX 2 · Tailwind 4 + DaisyUI 5 · SSE · JSON 파일 저장(DB 없음)

> 이 PRD는 구현된 시스템의 단일 기준이다. 설계 결정의 배경은
> `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md`,
> 구현 가이드는 루트 `CLAUDE.md`를 참고한다.
> **v1(단일 OPEN 세션 + scope) 모델은 폐기**하고 카테고리×팀 상시 주문판 모델로 대체했다.

---

## 1. 프로젝트 개요

### 1.1 목적
사내에서 커피·점심을 함께 주문할 때, 당번 한 명이 취합하는 과정을 돕는다. 각자 자기 메뉴를 담고, 당번이 집계 화면에서 요약을 복사해 발주하며, 음식이 나오면 사람별 리스트로 나눠준다.

### 1.2 배경
- 기존: 메신저로 "뭐 마실래?"를 일일이 받아 손으로 취합 — 누락·오타·재확인이 잦다.
- 해결: 링크 하나로 각자 셀프 주문 → 자동 집계·금액 계산 → 복사용 발주 요약.

### 1.3 범위
- **포함**: 카테고리(커피) 메뉴·옵션 선택, 데이터 주도 가격 계산, 팀별/전체 주문판, 당번 마감 타이머·수동 초기화, 메뉴별/사람별 집계, 요약 텍스트 복사, 보드별 SSE 실시간, 관리자 메뉴 조회(PIN).
- **제외(이번 빌드)**: 음식 메뉴 실데이터(구조만 준비), 주문 이력/보관/통계, 마감·초기화 권한 제어, 관리자 인라인 메뉴 편집, 자동 초기화, Jasypt/Cloudflare Access.

### 1.4 대상 사용자
- **주문자**: 이름만 입력하고 메뉴를 담아 제출. 역할 구분 없음(이름 = 식별자, 재제출 = 수정).
- **당번**: 마감 시각 설정/해제·초기화·집계 발주. PIN 없음(1차, 팀 내부 신뢰 전제).
- **관리자**: `/admin` PIN으로 메뉴 데이터 조회.

### 1.5 운영 환경
- 사내 내부망, Mac Mini(OrbStack) Docker Compose, 서브도메인 `mukja.sybaek.dev`(NPM 프록시).
- 모바일 우선(360~480px), 데스크톱은 480px 가운데 정렬. 다크모드(prefers-color-scheme + DaisyUI).

---

## 2. 핵심 개념 — 주문판(Board)

**주문판 = (카테고리, 팀)** 조합. 각 조합은 독립적이고 상시 존재한다.
- 카테고리: `coffee`, `food`(이번 범위 비활성). 메뉴 카테고리에 `group`으로 구분.
- 팀: 설정(`mukja.teams`) 목록. **`all`(전체)**도 팀 목록의 한 항목으로 전사 보드 역할.
- 흐름: **카테고리 선택 → 팀 선택 → 주문 → 집계/발주**. 보드별 URL(`/coffee/sa`) 직접 진입도 지원.
- 세션 열기/닫기·날짜별 보관·히스토리 없음. 비어 있으면 바로 주문, 지난 주문이 남았으면 당번이 초기화 후 시작.

---

## 3. 데이터 모델 (JSON, `JsonStore` 경유)

### 3.1 menus.json (시드: `menus.seed.json` 최초 복사)
`place`, `optionDefs`(temp/ice/light/shot), `categories[*].group`("coffee"|"food"). 옵션 타입:
- `single`: 보기 중 하나(required면 미선택 시 담기 불가). `fixedTemp` 메뉴는 temp UI 숨김·가산 무시.
- `toggle`: on/off. `counter`: 0~max, 개당 extra 가산.
- 가격/옵션텍스트는 `PriceCalculator`/`OptionTextBuilder`가 optionDefs로 계산(데이터 주도).

### 3.2 보드 파일 `data/orders/{category}-{team}.json`
```json
{ "closeAt": "2026-05-22T14:30:00+09:00",
  "orders": [ { "person": "백상열", "submittedAt": "...",
    "lines": [ { "itemId":102, "name":"아메리카노", "unitPrice":1600,
      "options":{"temp":"ice","shot":1}, "optionText":"ICE·샷+1", "lineTotal":2600 } ] } ] }
```
- `closeAt` nullable. "주문 받는 중" = `closeAt == null || now < closeAt`. 경과 시 제출 409.
- 동일 `person` 재제출 시 덮어쓰기. `reset`은 orders·closeAt 모두 비움.

### 3.3 팀 설정 (application.yml)
```yaml
mukja:
  data-dir: ${MUKJA_DATA_DIR:./data}
  admin-pin: ${MUKJA_ADMIN_PIN:1234}
  teams: [ { id: all, name: 전체 }, { id: sa, name: SA팀 }, { id: imdg, name: IMDG팀 } ]
```

---

## 4. API / 라우트

| Method | Path | 설명 |
|---|---|---|
| GET | `/` | 카테고리 선택 |
| GET | `/{category}` | 팀 선택 |
| GET | `/{category}/{team}` | 주문판 |
| GET | `/{category}/{team}/menu?cat={sub}` | 서브카테고리 그리드 fragment |
| GET | `/{category}/{team}/menu/{itemId}/options` | 옵션 모달 fragment |
| POST | `/{category}/{team}/orders` | 주문 제출/수정 (마감 409) |
| GET | `/{category}/{team}/status` | 집계/발주 화면 |
| GET | `/{category}/{team}/status/summary.txt` | 복사용 요약 텍스트 |
| GET | `/{category}/{team}/status/stream` | SSE 구독 |
| POST | `/{category}/{team}/deadline` | 마감 설정/해제 (PIN 없음) |
| POST | `/{category}/{team}/reset` | 초기화 (PIN 없음) |
| GET/POST | `/admin/**` | 메뉴 조회 (PIN 보호) |

`{category}`는 `coffee|food` 정규식으로 제약(static 경로 보호).

---

## 5. 화면 (모바일 우선)

- **주문자 `/{category}/{team}`**: sticky 헤더(카테고리·팀명 + 마감 카운트다운 + 이름 입력) · 당번 컨트롤(마감설정/해제/초기화·집계 링크) · 가로 스크롤 서브카테고리 탭(HTMX swap) · 2열 카드 그리드 · 카드 탭 → 하단시트 옵션 모달 · 하단 고정 제출 바. 이름은 localStorage 기억.
- **집계 `/status`**: 통계(인원/잔수/총액·1인당) · 요약 복사(2초 완료 표시) · 메뉴별 카드(옵션 breakdown) · **사람별 리스트(배분 겸용)** · SSE 실시간 갱신.
- **관리자 `/admin`**: PIN 게이트 → 메뉴 조회.
- 신호등 색상(색+텍스트 병기): 주문중 초록 / 마감임박(10분) 주황 / 마감 회색. 터치 타깃 ≥ 44px.

---

## 6. 비기능 요구사항
- **동시성**: 보드별 `JsonStore.mutate`(write-lock read-modify-write, tmp→ATOMIC_MOVE)로 동시 제출 무손실.
- **데이터 보존**: 컨테이너 재시작에도 `data/` 유지(Docker 볼륨).
- **프라이버시**: 계좌/금융정보 저장 금지, 외부 전송 없음.
- **시간대**: 모든 시각 KST 직렬화.
- **코딩 컨벤션**: 파일 최상단 역할 주석·함수 위 기능 주석(한국어), record·생성자 주입, Lombok 미사용.

---

## 7. 빌드/배포
- 빌드/실행 `./gradlew bootRun`, 테스트 `./gradlew test`, CSS `npm run build:css`, E2E `cd e2e && npx playwright test`.
- Docker multi-stage(node CSS → gradle bootJar → JRE), `docker compose up --build`, `data` 볼륨, NPM 서브도메인 `mukja.sybaek.dev`.

---

## 8. 미해결 / 후속
- 음식 카테고리 실데이터(식당별 place·밥양/맵기 옵션) — 구조는 준비됨.
- 관리자 인라인 메뉴 편집(현재 조회만), 마감·초기화 권한 제어, 주문 이력/통계, Jasypt PIN 암호화.
