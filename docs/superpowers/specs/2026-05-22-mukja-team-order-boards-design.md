# 설계: mukja — 카테고리 × 팀 상시 주문판

**작성일**: 2026-05-22
**프로젝트**: mukja (구 order-hub)
**상태**: 확정 (브레인스토밍 산출물)
**기준 문서**: `docs/PRD.md` (이 설계가 PRD의 세션 모델 부분을 대체한다)

> 이 문서는 사내 커피·점심 주문 취합 웹앱 `mukja`의 설계 결정을 기록한다.
> PRD.md의 기술 스택·메뉴/옵션 구조·가격 계산·UI 프로토타입은 그대로 유지하되,
> **"세션 한 번에 하나만 OPEN"** 모델을 폐기하고 **"카테고리 × 팀 = 상시 주문판"** 모델로 대체한다.

---

## 1. 개요

사내에서 커피·점심을 함께 주문할 때, 당번 한 명이 취합하는 과정을 돕는 모바일 우선 웹앱이다.

- 흐름: **카테고리 선택(커피/음식) → 팀 선택 → 주문 → 집계/발주**.
- 조직에 업무별 팀이 있어, 전체가 함께 시키기도 하고 팀별로 시키기도 한다.
- 각자 자기 메뉴를 담고, 당번이 집계 화면에서 요약을 복사해 발주하며, 음식이 나오면 사람별 리스트로 나눠준다.

---

## 2. 확정된 설계 결정 (브레인스토밍 기록)

| # | 결정 | 선택 | 비고 |
|---|------|------|------|
| 1 | PC(웹) 레이아웃 | 모바일 우선, 데스크톱은 `max-width: 480px` 가운데 정렬 | 추후 반응형 2단 확장 가능 |
| 2 | 첫 빌드 범위 | **커피만** | 음식은 `menus.json` 데이터 추가로 동작 |
| 3 | 마감된 주문 처리 | **수동 초기화** (이력/날짜별 보관 없음) | 자동 초기화 없음 |
| 4 | 관리자 PIN | `application.yml` 평문 (환경변수 오버라이드) | 내부망·민감정보 미저장 |
| 5 | 세션 모델 | **폐기** → 카테고리 × 팀 상시 주문판 | 세션 열기/닫기 없음 |
| 6 | 마감 타이머 | 당번이 설정, **지나면 주문 차단(409)** | 보드별 `closeAt` |
| 7 | URL 구조 | **`/{category}/{team}`** (카테고리 상위) | 보드별 직접 진입 가능 |
| 8 | 전사(전체) 주문판 | **팀 목록에 '전체' 포함** | 별도 개념 없이 팀처럼 취급 |
| 9 | 마감 설정·초기화 권한 | **PIN 없음, 누구나** | 확인 다이얼로그 1회 |

---

## 3. 아키텍처

### 3.1 핵심 개념 — 주문판(Board)

**주문판 = (카테고리, 팀)** 조합. 각 조합은 독립적이고 상시 존재한다.

- 카테고리: `coffee`, `food` (food는 이번 범위 비활성, 데이터로 확장).
- 팀: `application.yml` 설정 목록. **'전체'(`all`)도 팀 목록의 한 항목**으로 포함.
- 보드는 열기/닫기·날짜별 보관·히스토리가 없다. 들어가서 비어 있으면 바로 주문, 지난 주문이 남아 있으면 당번이 초기화 후 시작.

### 3.2 저장소

- 보드별 파일: **`data/orders/{category}-{team}.json`** (예: `coffee-all.json`, `coffee-sa.json`).
  - 보드마다 독립 락(JsonStore) → 서로 다른 보드의 동시 주문이 경합하지 않는다.
  - 파일은 보드에 첫 주문/마감 설정이 생길 때 lazy 생성. 없으면 빈 보드로 간주.
- 메뉴: `data/menus.json` (최초 기동 시 `menus.seed.json` 복사). 단일 파일.
- 모든 JSON 접근은 `JsonStore<T>` 경유 (직접 ObjectMapper 호출 금지). 쓰기는 tmp 파일 → `ATOMIC_MOVE`.

### 3.3 보드 데이터 구조

```json
// data/orders/coffee-sa.json
{
  "closeAt": "2026-05-22T14:30:00+09:00",   // nullable. null이면 마감 없음(상시 주문 가능)
  "orders": [
    {
      "person": "백상열",
      "submittedAt": "2026-05-22T14:18:00+09:00",
      "lines": [
        {
          "itemId": 102, "name": "아메리카노", "unitPrice": 1600,
          "options": { "temp": "ice", "ice": "ice_less", "light": true, "shot": 1 },
          "optionText": "ICE·얼음 적게·연하게·샷+1", "lineTotal": 2600
        }
      ]
    }
  ]
}
```

- 보드의 카테고리·팀은 파일명(경로)에서 결정 → 본문에 중복 저장하지 않는다.
- `status`(OPEN/CLOSED) 필드 없음. **"주문 받는 중"은 `closeAt == null || now < closeAt`로 파생**.
- 동일 `person` 재제출 시 기존 엔트리 덮어쓰기(수정 = 재제출).

### 3.4 마감 타이머

- 당번이 보드 화면에서 **마감 시각을 설정/해제**한다 (PIN 없음).
- 설정되면 카운트다운 표시. 마감 10분 이내 주황, 마감 후 회색.
- 마감 시각 경과 후 `POST .../orders`는 **409로 거부**한다.
- 초기화 시 `closeAt`도 null로 비워 다시 주문 가능 상태가 된다.

### 3.5 초기화 (재사용)

- 보드 화면의 **"초기화" 버튼** → 확인 다이얼로그 1회 → `orders` 비우고 `closeAt` null.
- PIN 없음. 팀 내부 신뢰 전제(1차). 자동 초기화 없음.

### 3.6 메뉴 그룹

- `menus.json`의 각 카테고리(서브탭)에 **`group: "coffee" | "food"`** 추가.
- 보드는 자기 카테고리(group)에 속한 서브카테고리만 노출.
- 이번 빌드: 모든 시드 카테고리 `group: "coffee"`. 음식 그룹은 데이터 추가 시 자동 동작.

---

## 4. 라우트

| Method | Path | 설명 | 응답 |
|---|---|---|---|
| GET | `/` | 카테고리 선택(커피/음식) | HTML |
| GET | `/{category}` | 팀 선택('전체'+팀 버튼) | HTML |
| GET | `/{category}/{team}` | 주문자 화면(보드) | HTML |
| GET | `/{category}/{team}/menu?cat={sub}` | 서브카테고리 그리드 fragment | HTML(HTMX) |
| GET | `/{category}/{team}/menu/{itemId}/options` | 옵션 모달 fragment | HTML(HTMX) |
| POST | `/{category}/{team}/orders` | 주문 제출/수정 (마감 후 409) | HTML |
| GET | `/{category}/{team}/status` | 집계/발주 화면 | HTML |
| GET | `/{category}/{team}/status/stream` | SSE 구독(보드별) | text/event-stream |
| GET | `/{category}/{team}/status/summary.txt` | 요약 텍스트(복사용) | text/plain |
| POST | `/{category}/{team}/deadline` | 마감 시각 설정/해제 (PIN 없음) | HTML/redirect |
| POST | `/{category}/{team}/reset` | 초기화 (PIN 없음) | HTML/redirect |
| GET/POST/DELETE | `/admin/**` | 메뉴 CRUD (PIN 보호) | HTML/redirect/204 |

- **팀 미설정 시**: `/`가 단일 기본 보드로 바로 동작(팀 선택 생략, `default` 팀 사용).
- 보드별 URL(`/coffee/sa`)로 직접 진입 가능. 팀 선택 화면 경유와 직접 진입 둘 다 지원.

### 설정 (application.yml)

```yaml
mukja:
  admin-pin: ${MUKJA_ADMIN_PIN:1234}   # /admin 보호. 환경변수 오버라이드
  teams:
    - { id: all,  name: 전체 }
    - { id: sa,   name: SA팀 }
    - { id: imdg, name: IMDG팀 }
```

- `@ConfigurationProperties("mukja")` record 바인딩.
- 팀 id는 URL 경로에 그대로 쓰인다(소문자 영숫자).

---

## 5. 핵심 로직

### 5.1 JsonStore<T> (제네릭)
PRD 5.1 그대로 유지. read(readLock) / write(writeLock, tmp→ATOMIC_MOVE) / update(read-modify-write).

### 5.2 OrderRepository (보드 인지)
- `(category, team)` → `data/orders/{category}-{team}.json` 경로 해석.
- 보드별 `JsonStore` 인스턴스를 캐시(map). 파일 없으면 빈 보드(closeAt=null, orders=[]) 반환.
- `submit`, `reset`, `setDeadline`, `clearDeadline` 제공. submit은 마감 검사(closeAt 경과 시 예외→409).

### 5.3 OrderAggregator
- 입력: 보드의 `orders` (+ 카테고리/팀 라벨, closeAt).
- 출력: `byMenu`(메뉴→{count, optionBreakdown}), `byPerson`(사람→주문줄), `stats`(인원/잔수/총액/1인당), `summaryText`.
- 요약 텍스트 헤더: `[장소 · 카테고리 · 팀명]`.
  ```
  [KT 분당 카페 · 커피 · SA팀]
  · 아메리카노 5잔 (ICE 3, ICE·샷+1 1, HOT 1)
  · 카페라떼 3잔 (HOT 2, ICE 1)
  합계 48,500원 · 8명
  ```

### 5.4 실시간 (SSE)
- `/{category}/{team}/status/stream` 구독 시 **보드별** SseEmitter 등록(timeout 30분).
- 해당 보드에 주문 제출/수정/초기화/마감 변경 시 그 보드 구독자에게만 `event: order-update` broadcast.
- 폴백: HTMX 폴링 5초.

---

## 6. UI/UX (모바일 우선, 결정 A)

### 공통
- 기준 폭 360~480px. 데스크톱은 `max-width: 480px` 가운데 정렬.
- **Tailwind 4 + DaisyUI 5**, 다크모드(`prefers-color-scheme` + 토글).
- 신호등 색상(색+텍스트 병기): 주문중=초록 `#1D9E75`, 마감임박(10분 이내)=주황 `#BA7517`, 마감=회색 `#888780`.
- 접근성: 터치 타깃 ≥ 44px, 색상만으로 상태 구분 금지.
- **이름 기억**: 입력한 이름을 `localStorage`에 저장 → 다음 주문 시 자동 채움.

### `/` 카테고리 선택
- 커피 / 음식 큰 버튼 2개. 음식은 비활성(준비중) 표시.

### `/{category}` 팀 선택
- '전체' + 팀 버튼 그리드. 탭하면 해당 보드로 이동.

### `/{category}/{team}` 주문자 화면
- 상단 sticky 헤더: `카테고리 · 팀명` + 마감 카운트다운(설정 시) + 이름 입력 + 집계 링크.
- 당번 컨트롤 줄: **마감 설정/해제**, **초기화**(확인 다이얼로그) — PIN 없음.
- 가로 스크롤 서브카테고리 탭 → HTMX 그리드 swap.
- 2열 카드 그리드: 메뉴명·가격·온도 배지. 담긴 메뉴 강조 + 수량 뱃지.
- 카드 탭 → **하단 시트(bottom-sheet) 모달** 옵션 선택. 필수 미선택 시 담기 비활성.
- 하단 sticky 바: "○○ 외 N건 · 총액" → 제출(이름 검증, 마감 후 409 안내).

### `/{category}/{team}/status` 집계/발주
- 상단 통계(인원/잔수/총액+1인당) + `카테고리·팀명`.
- "주문 요약 복사" → 클립보드 + 2초 완료 표시.
- 메뉴별 집계 카드(옵션 breakdown) + **사람별 리스트(배분 겸용)**.
- SSE 실시간 갱신.

### `/admin` 메뉴 관리
- PIN 게이트 → 메뉴 CRUD(menus.json 수정). (세션 관리 없음)

---

## 7. 구현 순서 (마일스톤)

1. **M1 — 뼈대 & 이름**: `mukja`로 전체 네이밍(`com.mukja`), `build.gradle.kts`, `application.yml`(teams/pin), `JsonStore<T>`+테스트, `menus.seed.json`(group 포함) 복사, 메뉴 도메인 + 로드 검증.
2. **M2 — 네비 & 주문자 화면**: layout + Tailwind/DaisyUI, `/` `/{category}` `/{category}/{team}`, 메뉴 그리드/옵션 모달/카트 바 fragment, 가격 계산+테스트, `POST /orders`(보드 저장, 이름 검증), 이름 localStorage.
3. **M3 — 집계/발주 & 실시간**: `OrderAggregator`+테스트, `/status` `/status/summary.txt`, 보드별 SSE + 폴백 폴링.
4. **M4 — 당번 컨트롤 & 관리자**: 마감 설정/해제(409 차단 로직), 초기화(확인 다이얼로그), `/admin` PIN + 메뉴 CRUD.
5. **M5 — 배포**: multi-stage Dockerfile, docker-compose(data 볼륨), NPM 서브도메인 `mukja.sybaek.dev`.

---

## 8. 비기능 요구사항

- **동시성**: 보드별 JsonStore.update(read-modify-write)로 동시 제출 시 데이터 유실 없음.
- **데이터 보존**: 컨테이너 재시작에도 `data/` 유지(Docker 볼륨).
- **반응형**: 모바일 우선, 데스크톱 480px 가운데 정렬.
- **프라이버시**: 계좌/금융정보 저장 금지. 이름·주문만. 외부 전송 없음.
- **시간대**: 모든 시각 KST(Asia/Seoul) 직렬화.
- **코딩 컨벤션(기본 규칙)**: 모든 파일 최상단 역할 주석, 모든 함수 위 기능 주석(한국어). record 적극 활용, 생성자 주입, Lombok 미사용.

---

## 9. 범위 밖 (이번 빌드 제외)

- 음식 카테고리 실데이터(구조만 준비).
- 주문 이력/날짜별 보관/통계.
- 마감·초기화 권한 제어(PIN/로그인) — 1차는 누구나.
- 자동 초기화, 다중 장소(음식 식당별 place) — 음식 도입 시 검토.
- Jasypt PIN 암호화, Cloudflare Access.

---

## 10. PRD.md 갱신 필요 항목

이 설계 확정에 따라 `docs/PRD.md`의 아래를 새 모델로 갱신한다(세션→보드):
- 제목/메타(order-hub→mukja, 도메인, 패키지).
- 0절(세션 scope → 카테고리×팀 보드), 2절 패키지(OrderSession/SessionScope 제거, Board/OrderRepository 보드 인지), 3.2/3.3(보드 파일 구조, teams 설정), 4절 라우트, 6절 화면, 10절 미해결(대부분 해소).
