# mukja — 단일 주문판 레이아웃 리디자인 (Single-Board Layout)

> **상태:** 설계 승인 대기 → 구현 계획 작성 예정
> **날짜:** 2026-05-26
> **선행 설계:** `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md` (보드 모델 — 변경 없음)

## 배경 / 동기

현재 UI는 `카테고리 선택(/) → 팀 선택(/{category}) → 주문판(/{category}/{team})` 3단계 페이지 이동을 강제한다.
카테고리·팀을 바꿀 때마다 뒤로 가서 다시 고르는 흐름이 번거롭고, 화면이 밋밋하다.

**한 화면에서** 카테고리(왼쪽 드로어)·팀(상단 드롭다운)·이름을 고르고 바로 주문하며, 집계도 같은 화면에서 토글로 보는 **앱 같은 단일 주문판**으로 바꾼다.

## 범위 (Scope)

- **레이아웃/네비게이션 구조만** 변경. 색·폰트·DaisyUI 라이트 테마·카드 스타일 등 **감성은 현행 유지**.
- 모바일 우선(최대 480px). 왼쪽 카테고리는 **슬라이드아웃 햄버거 드로어**(DaisyUI `drawer`).
- 집계는 **별도 페이지 대신 주문판 안의 토글 패널**로 통합(옵션 A).
- 백엔드 도메인·저장소·집계 로직·SSE·주문 API는 **변경하지 않는다**. 컨트롤러 라우팅과 템플릿만 손본다.

### 비목표 (Non-goals)
- 색상/타이포/테마 변경, 다크모드.
- 관리자 메뉴 편집, 음식 실데이터 추가(별개 작업).
- 새 백엔드 엔드포인트 추가(기존 라우트 재사용, status는 fragment 응답만 추가).

## 네비게이션 변화

| 현재 | 변경 후 |
|---|---|
| `GET /` → 카테고리 선택 페이지(`category.html`) | `GET /` → **`/coffee/all` 로 302 리다이렉트** (기본 보드) |
| `GET /{category}` → 팀 선택 페이지(`team.html`) | `GET /{category}` → **`/{category}/{firstTeam}` 로 302 리다이렉트** (firstTeam = `props.teams().get(0).id()`, 즉 `all`) |
| `GET /{category}/{team}` → 주문판 | 동일 라우트, **새 단일 레이아웃**으로 렌더 |
| `GET /{category}/{team}/status` → 집계 **페이지** | 동일 라우트. **`HX-Request` 헤더면 fragment, 아니면 전체 페이지** 반환 (딥링크 호환) |

- `category.html`, `team.html` 템플릿은 **삭제**한다(드로어 + 드롭다운이 대체).
- `/{category:coffee|food}` 정규식 제약은 유지(static 경로 보호).

## 레이아웃 (단일 주문판, `board.html`)

```
┌───────────────────────────────────────┐
│ [☰]  [팀 ▾]  [이름 입력........]        │  ← 상단 바 (sticky)
│         [ 주문 | 집계 ]   ⏱ 마감 12:00  │  ← 세그먼트 토글 + 마감 뱃지
├───────────────────────────────────────┤
│  (주문 모드)        |  (집계 모드)       │
│  서브카테고리 탭     |  통계(인원/잔/총액) │
│  ┌────┐ ┌────┐     |  메뉴별 / 사람별    │
│  │카드│ │카드│     |  [당번: 마감/초기화]│
│  └────┘ └────┘     |  [요약 복사]        │
├───────────────────────────────────────┤
│      [ 담은 메뉴 N개 · 주문하기 ]        │  ← 카트바(주문 모드 only)
└───────────────────────────────────────┘
  옵션 모달(하단시트)은 현행 그대로
  왼쪽 드로어(열림): ☕커피 / 🍱점심(준비중)
```

### 컴포넌트

1. **드로어 셸** — DaisyUI `drawer`. 본문 전체를 `drawer-content`로 감싸고, `drawer-side`에 카테고리 목록. 햄버거(☰) 토글. 모바일에서 좌측에서 슬라이드, 480px 컨테이너 안에서 동작.
2. **상단 바(sticky)** — 햄버거, **팀 드롭다운**(DaisyUI `select` 또는 `dropdown`; 변경 시 `/{category}/{newTeam}` 이동), **이름 입력**(기존 `#person` 유지 — order.js가 참조). 마감 뱃지/카운트다운 유지.
3. **세그먼트 토글 `[주문 | 집계]`** — 두 버튼(DaisyUI `join` 또는 `tabs`). `주문`은 메뉴 그리드 영역, `집계`는 `hx-get`으로 `/{c}/{t}/status` fragment를 메인 영역에 swap.
4. **주문 영역** — 기존 서브카테고리 탭 nav + `#menu-grid`(menu-grid fragment) 그대로 재사용.
5. **집계 영역** — 기존 `status.html`의 본문을 fragment(`order/status :: panel`)로 분리해 swap. **당번 컨트롤(마감설정/해제/초기화)을 이 패널 헤더로 이동**(주문 화면에서는 제거). 요약 복사 버튼·SSE 구독 포함.
6. **카트바** — 현행 유지하되 **주문 모드에서만 표시**(집계 모드에서는 숨김).
7. **옵션 모달** — 변경 없음.

### 카테고리 드로어 데이터
- 상위 카테고리 고정 목록: `coffee → ☕ 커피`, `food → 🍱 점심`.
- 활성 여부는 데이터 주도: `menuService.categoriesIn(group)` 가 비어있지 않으면 활성. 현재 `food`는 데이터 없음 → "준비중" 비활성.
- 컨트롤러가 `topCategories`(= `{id, name, available, current}` 목록)를 모델에 추가.

## 데이터 흐름

- 카테고리 전환: 드로어 링크 → 풀 페이지 이동 `/{newCat}/{team}`.
- 팀 전환: 드롭다운 `onchange` → `location` 이동 `/{category}/{newTeam}` (order.js에 작은 핸들러 추가).
- 주문/집계 토글: HTMX `hx-get` 부분 swap. 집계는 status fragment.
- 실시간: 집계 패널 안의 SSE 구독은 현행과 동일(`sse-connect`). 주문 모드에서는 기존 폴백 유지.

## 백엔드 변경 (최소)

`OrderController` 만 수정:
- `category()` (`GET /`) → `"redirect:/coffee/all"`.
- `team()` (`GET /{category}`) → `"redirect:/" + category + "/" + props.teams().get(0).id()`.
- `board()` → 모델에 `teams`(드롭다운용), `topCategories`(드로어용) 추가. 기존 속성 유지.
- `status()` → `@RequestHeader(value="HX-Request", required=false)` 가 있으면 `"order/status :: panel"`, 없으면 `"order/status"` 반환.
- 삭제: `category.html`, `team.html` 참조(메서드는 redirect로 대체되므로 뷰 없음).

도메인/저장소/집계/SSE/주문 API: **변경 없음**.

## 템플릿 작업

- `board.html` — 드로어 셸 + 상단 바(드롭다운/이름) + 세그먼트 토글 + 주문영역 + 집계영역 swap 타깃으로 재작성.
- `status.html` — 본문을 `th:fragment="panel"` 로 감싸 fragment/전체 페이지 양쪽에서 재사용. 당번 컨트롤 추가.
- `category.html`, `team.html` — 삭제.
- `order.js` — 팀 드롭다운 `onchange` 이동 핸들러, 주문/집계 토글 시 카트바 show/hide 로직 추가.
- CSS — **`npm run build:css` 1회 필요**. 컴파일된 `static/css/app.css` 확인 결과: `dropdown`/`select`/`tabs`/`btn`/`card`/`stat`/`badge`/`modal`/`input`/`toggle`/`menu` 는 **이미 포함**, `drawer`/`drawer-side`/`drawer-content` 와 `join`/`tab` 은 **미포함**. 따라서 좌측 드로어 도입 시 재빌드 후 `static/css/app.css` 커밋. 세그먼트 토글은 이미 있는 `btn`/`tabs`로 구성하면 추가 클래스 최소화 가능. npm/node는 로컬 설치됨, `package.json`·Dockerfile node 스테이지에 이미 존재(신규 의존성 아님).

## 테스트 영향 (반드시 갱신)

- `NavControllerTest.categoryPageLists` — `GET /` 가 이제 302. → `/coffee/all` 리다이렉트 단언으로 변경.
- `NavControllerTest.teamPageListsTeams` — `GET /coffee` 가 이제 302. → 리다이렉트 단언, 또는 보드 화면(`/coffee/all`)의 팀 드롭다운에 `전체`/`SA팀` 포함 단언으로 변경.
- `BoardControllerTest.boardPageShowsHeader` — 보드에 `SA팀`(드롭다운 선택값) 여전히 존재 → 유지/소폭 조정.
- `StatusControllerTest` — `/status` 전체 페이지 응답 유지(HX-Request 없음) → 통과해야 함. fragment 경로 테스트 1건 추가(`HX-Request: true` → `panel` 내용).
- E2E(`e2e/`) — 카테고리→팀→주문→집계 흐름의 셀렉터가 단일 화면으로 바뀜. 드로어/드롭다운/토글 기준으로 재작성.

## 마이그레이션/리스크

- 외부에서 `/` 또는 `/coffee` 북마크 → 리다이렉트로 자연 흡수.
- 모바일 480px 안에서 드로어가 컨테이너를 벗어나지 않도록 `drawer` 컨테이너를 `main`(max-w-480) 기준으로 배치.
- 당번 컨트롤이 집계 패널로 이동하므로, 주문자만 쓰는 화면이 단순해지고 당번 동작은 집계 화면에 모인다(권한 모델은 현행대로 PIN 없음).

## 성공 기준

1. `/coffee/all` 한 화면에서 햄버거로 카테고리, 드롭다운으로 팀, 입력으로 이름을 바꾸며 주문할 수 있다.
2. `[집계]` 토글로 같은 화면에서 집계·당번 컨트롤을 보고, `[주문]`으로 되돌아온다.
3. `/`, `/coffee` 접근 시 기본 보드로 리다이렉트된다.
4. 기존 주문/마감/초기화/SSE/요약 동작이 그대로 작동한다.
5. `./gradlew test` 통과(갱신된 테스트 포함), E2E 통과.
