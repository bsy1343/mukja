# DevForge Platform — Product Requirements Document

> **버전:** v2.14
> **작성일:** 2026-04-22
> **이전 버전:** v2.13 (2026-04-20 오후)
> **스택:** Spring Boot 3.5.x + JDK 21 + Thymeleaf + Tailwind CSS 4 + DaisyUI 5 + HTMX + SSE + SpringDoc OpenAPI
> **타입:** Self-hosted Internal Developer Platform (IDP) — **K8s 전용**
> **개발 방식:** Claude Code 기반 전체 구조 일괄 생성

### v2.14 변경 사항 요약 (포트폴리오 정체성 강화 · UX 디테일 정제)

> 📊 **구현 진행도 (2026-04-22)**: About 최상단 저작자 · 프로필 · 연락처 4종 · Hero 4카드 (MSA Cloud 표준 아키텍처 · Saga 체인 · Gateway 라우팅 · Common F/W · 관찰성) · 소개 메뉴 펄스 강조 · 시스템 카드 ↗ → "상세 보기 →" CTA 통합 · DORA 카드 우상단 체크박스 · 전역 WIP 마커 제거. PRD/CLAUDE/FRONTEND/Master Plan 일괄 최신화.

| 항목 | 변경 내용 | 상태 |
|---|---|---|
| **About 저작자 블록 최상단 승격 + 프로필 이미지** | 기존 푸터 위치 → 탭 네비 바로 다음 페이지 최상단. **프로필 사진**(`/img/profile.jpeg` · `ring-2 ring-primary/50`) + 이름/영문/역할(**Software Architect**) + 4 연락처 링크(이메일/GitHub/LinkedIn/전화) + WIP 뱃지 + 간결한 저작권 한 줄. 첫 방문자 즉시 노출로 포트폴리오 첫인상 강화 | ✅ 완료 |
| **Hero 4 카드 — MSA Cloud 표준 아키텍처** | 기존 단순 1 문단 → "✨ 프로젝트 생성 1 클릭 = **MSA Cloud 표준 아키텍처 자동 적용**" 헤더 + 4 카드 (info/accent/success/warning 색상 대비): ① 체인 자동 구성 (Saga · SCM·CI·Registry·CD) ② Gateway · 라우팅 자동화 (Per-Domain · routes.yml PR · Filter) ③ Common F/W · 회복성 · 보안 (Resilience4j · JWT/CSRF/AES · MDC · RFC 7807 · BOM · CQRS) ④ 관찰성 · 감사 즉시 연동 (OTel · APM · CI/CD · Drift · SAST · Audit) | ✅ 완료 |
| **네비 "소개" 메뉴 강조 (포트폴리오 진입점)** | primary 색 + semibold + **펄스 점(animate-ping)** + ring — 다른 메뉴 대비 시선 유도. `title="먼저 보기"` 툴팁 | ✅ 완료 |
| **HTML meta + 소셜 공유 태그** | `<meta name="author/copyright/description/keywords">` + Open Graph (`og:title/description/type/site_name/article:author`) + Twitter Card. 저작자 증명 · 슬랙/카톡/링크드인 공유 시 썸네일 자동 생성 | ✅ 완료 |
| **시스템 카드 ↗ 제거 + "상세 보기 →" CTA 한 줄 통합** | 우상단 ↗ 외부 이동 아이콘 제거 (하단 CTA 와 중복). 알림과 "상세 보기 →" 를 flex justify-between 한 줄로 통합 — 카드 높이 11% 감소 + 시각 신호 1곳 집중. `font-mono text-primary/70 group-hover:text-primary` 로 hover 연동 | ✅ 완료 |
| **DORA 카드 우상단 체크박스 (subtle)** | 14×14 체크박스 — 미선택: opacity 40% 빈 박스, hover: 70%, 선택: 환경색(cssTone) 채움 + ✓ 아이콘 (opacity 90%). 기존 border/ring/라벨색 3단계 + 체크박스로 **선택 시각 신호 4중 복합** — 접근성 개선 | ✅ 완료 |
| **전역 WIP 마커 제거** | 우상단 `● PERSONAL PROJECT · WIP © 백상열` fixed 마커 제거. About 최상단 블록이 동일 신호를 더 강력히 전달 · 운영 페이지에 상시 표시는 역효과(KT DS 실 운영 맥락에서 "실제 돌아가는 거야?" 의심). 네비 "소개" 펄스로 진입 유도 역할 대체 | ✅ 완료 |
| **역할명 변경 + 문구 간결화** | `Software Engineer` → **`Software Architect`** (MSA Cloud 설계 포지셔닝과 호응). 저작권 문구에서 "인용 시 출처 표시 요청" 제거 → `© 2026 백상열` 만 | ✅ 완료 |
| **Profile 이미지 정적 리소스 추가** | `docs/github_profile.jpeg` → `src/main/resources/static/img/profile.jpeg` 복사. Spring Boot 자동 `/img/*` 서빙. Thymeleaf `@{/img/profile.jpeg}` context-path 인식 | ✅ 완료 |
| **클릭 인디케이터 컨벤션 갱신** | ① **DORA/환경 탭 (필터·선택)**: border+ring + 환경색 라벨 + **우상단 체크박스(신규)** — 4중 복합. ② **시스템 카드 (외부 진입)**: 하단 **"상세 보기 →" CTA 한 줄 통합** (↗ 아이콘 제거). ③ 기타 외부 링크(알림·승인): 기존 `↗` 유지 | ✅ 완료 |

**검증**: Playwright 스크린샷 4개 (dashboard 카드 단순화·DORA 체크박스·About 4카드 Hero·크레딧 블록 최상단) — 회귀 0건 · SSE/드래그/Y-zoom 전 기능 정상.

**포트폴리오 정체성**:
- Hero 4 카드 가 Backstage/Cortex/Port **명시적 차별점**(일반 IDP = 카탈로그 / DevForge = 체인 자동 + Common F/W + 관찰성 통합)을 3초 내 전달
- `Software Architect` + `MSA Cloud 표준 아키텍처 자동 적용` 내러티브 호응
- 저작자 최상단 블록 + meta 태그 + 네비 강조 = 첫 방문자 시선 흐름 최적화

상세 문서:
- CLAUDE.md · FRONTEND.md · Phase 1 Master Plan 동기 갱신
- 프로필 이미지: `src/main/resources/static/img/profile.jpeg`

---

### v2.13 변경 사항 요약 (프론트 ES Modules 리팩토링 · 확장성 기반 확보)

> 📊 **구현 진행도 (2026-04-20 오후)**: `detail.html` 1,479줄 인라인 JS 를 **ES Modules 14개 + CSS 1개로 외부화**. shared/ 공용 유틸 4종 (event-bus · dom-utils · sse-factory · htmx-helpers) 확보 → CI/CD·Alerts 도메인 확장 시 재사용 기반. 빌드 체인 없음(순수 브라우저 ES Modules) · TypeScript 미도입 · npm 의존성 0.

| 항목 | 변경 내용 | 상태 |
|---|---|---|
| **APM 페이지 모듈화** | `detail.html` 1,479→**634줄**(57% 감소). 인라인 1,479줄 `<script>` → `static/js/apm/` 10개 모듈(constants · state · y-axis · scatter · drag-select · sse · topology · ui-extras · seed · index) + `static/css/apm/detail.css`. Thymeleaf `<script type="module" th:src="@{/js/apm/index.js}">` 하나로 대체 | ✅ 완료 |
| **shared 공용 유틸 4종** | `event-bus.js`(EVT 2단계 네임스페이스 + publish/subscribe) · `dom-utils.js`(fmtHMS · setText) · `sse-factory.js`(createSSE — EventSource 생성·JSON 파싱·재연결·에러 처리 공통화) · `htmx-helpers.js`(registerHtmxLifecycle — beforeSwap/afterSwap 생명주기 표준화). 다른 탭 도입 시 재사용 보장 | ✅ 완료 |
| **EVT 네임스페이스 구조** | `EVT.APM.*` / `EVT.CICD.*` / `EVT.ALERTS.*` / `EVT.SYSTEM.*` 2단계 `Object.freeze` 구조. 평평한 이름 난립 방지 + 오타 silent failure 방지(import 시점 검출) | ✅ 완료 |
| **window 전역 오염 정리** | 레거시 4종(`__SCATTER_SEED__` · `__PERCENTILES__` · `__TOP_ROW_CLICK_INIT__` · `__SCATTER_IIFE_INITED__`) → **`window.__DEVFORGE__.apm.{scatter, percentiles}`** 통합 네임스페이스. 향후 `.cicd`/`.alerts` 등 확장 구조. 접근자 `apm/seed.js` (getSeedScatter · getPercentiles · updatePercentiles) 로 모듈이 window 직접 참조 안 함 | ✅ 완료 |
| **CSS 외부화** | `detail.html` 인라인 `<style>` 89줄 → `static/css/apm/detail.css`. Thymeleaf `<link rel="stylesheet" th:href="@{/css/apm/detail.css}">` 로 로드 | ✅ 완료 |
| **env-matrix.html CSS 변수 버그 수정** | CI/CD 환경 매트릭스 fragment 의 `hsl(var(--color-base-300))` 2건 → `var(--color-base-300)` 직접 참조. 기존 sticky 헤더 배경·하단 페이드 그래디언트가 invalid CSS 로 투명 폴백되던 문제 해결 | ✅ 완료 |
| **HTMX 생명주기 표준화** | 기존 `window.__SCATTER_IIFE_INITED__` 가드·중복 afterSwap 리스너 패턴 → `registerHtmxLifecycle({targetId, shouldActivate, init, teardown})` 1개 호출. 모든 탭·페이지 모듈이 동일 패턴 공유 | ✅ 완료 |
| **빌드 체인·외부 의존성 0 유지** | `package.json` / `node_modules` / `tsconfig` / esbuild / webpack 일절 없음. ES Modules 는 모던 브라우저 네이티브 지원. Spring Boot `src/main/resources/static/**` 자동 서빙. `./mvnw package` 단일 명령 그대로 | ✅ 완료 |

**검증**: Playwright 7 시나리오 회귀 테스트 통과 — (1) 모듈 로드 + HUD (2) seed 통합 구조 (3) Y-zoom 6단계 sweep (4) 드래그 → 모달 (mouseup 0.100ms) (5) Top 행 클릭 → 토폴로지 (6) 범례 토글 (7) SSE 실시간 갱신. 다른 페이지(/dashboard, /about/overview, /admin/connections) 스모크 통과 — 회귀 0건.

**Phase 1 Master Plan 의 선행조건 해결**: Task 4c/4d/4e (CI/CD Control Plane · SonarQube·Jenkins 실 연동) 진입 시 `static/js/cicd/` 폴더 신설 + shared/ 재사용으로 즉시 확장 가능.

상세 구현:
- Plan: `docs/superpowers/plans/2026-04-20-apm-js-modularization.md`
- 파일 구조: `static/js/{shared,apm}/*` · `static/css/apm/detail.css`

---

### v2.12 변경 사항 요약 (APM 산점 상호작용 & 성능 · UX 컨벤션 · 소개 메뉴)

> 📊 **구현 진행도 (2026-04-20)**: APM Scatter Y-zoom 6단계 + 양방향 동기 UI · 드래그/mouseup 성능 180× 개선 · Active Alerts 바 레이아웃 shift 제거 · 클릭 인디케이터 컨벤션 확립 · 소개 메뉴 3페이지 슬라이드덱 · 포털 버전 자동 표시 · DaisyUI v5 CSS 변수 패턴 정정.

| 항목 | 변경 내용 | 상태 |
|---|---|---|
| **Scatter Y축 zoom 6단계** | 하단 log 눈금 **50/100/300ms·1s 고정**, 상단 선형 **2s–10s / 3s–15s / 5s–25s / 10s–50s / 15s–75s / 20s–100s** 6단계. 기본 **3s–15s** (진입 시 항상 리셋). 우상단 HUD 옆 `<select>` 콤보박스 + 캔버스 우측 얇은 세로 슬라이더 **양방향 동기화**. 슬라이더는 투명 native `<input type=range>` + 커스텀 `y-zoom-track`/`y-zoom-marker` div 구조 — 브라우저별 `writing-mode` 썸 렌더 편차 우회 | ✅ 완료 |
| **드래그·mouseup 성능 180×** | `mousemove` → `draw()` 동기 호출 제거 (0.03ms). `mouseup` 핸들러는 상태 정리 후 즉시 리턴(0.10ms), 필터+모달은 `requestAnimationFrame` 콜백으로 위임. 점 타임스탬프 파싱 `p.__ts` 캐시, 필터 루프 `viewRange()` 1회 호출(기존 15K회). 기존 mouseup 동기 블록 18.20ms → 0.10ms | ✅ 완료 |
| **Active Alerts 바 레이아웃 고정** | 1초 polling 갱신 시 레이아웃 shift 방지 — 한 줄 고정 상태 바 + 클릭 시 `absolute` 드롭다운 오버레이. 오픈 상태는 outer wrapper `#active-alerts-wrap[data-alerts-open]` 속성(HTMX swap 범위 밖)에 보존. 이상 시: `Critical N · Warning M · (총 X건 · 클릭하여 상세)` 한 줄 | ✅ 완료 |
| **클릭 인디케이터 컨벤션** | **필터/선택** (DORA 4환경 카드·환경 탭) = border + ring 강조만, 별도 아이콘 없음. **외부 페이지 이동** (시스템 카드·알림 링크·승인 페이지) = 우상단 `↗` 아이콘. 이전: `○/●`·"상세 ↗" 혼재 → 조잡·의미 모호 | ✅ 완료 |
| **소개 메뉴 (3페이지 슬라이드덱)** | `/about/overview`·`/about/architecture`·`/about/conventions`. 개요(Dev+Forge 로고·4 핵심 가치·진입점·**Common F/W Core 9영역 + Starter 4종**·**Per-Domain Gateway 3타일**·11종 연동·모니터링 & 관찰성), 아키텍처(5계층: IDP → SCM → CI → CD → Observability(OTel+LGTM+DevForge APM) · 프로젝트 생성 Saga · 이미지 프로모션 4환경 흐름 · 관리 클러스터 SVG), 컨벤션(네이밍·브랜치→환경 매핑 TEST=warning/SIT=info·시스템 불변 규칙). PRD/프로젝트 소스 기반 검증. 진입 모드 라벨 `MODE A · 신규 구축` / `MODE B · 기존 환경 연결` — 영문만 `font-mono + uppercase + tracking-widest`, 한글 기본 폰트/간격 분리 (혼합 텍스트 어색함 해소) | ✅ 완료 |
| **Common F/W Core + Per-Domain Gateway 방향성 정립** | PRD `3-6. Common F/W Core` (제공 구성요소 9영역 · Starter 리포 4종 · 프로젝트 생성 플로우 · SemVer/Renovate) + `3-7. Per-Domain Gateway` (자동 생성 · routes.yml 규약 · Bot 자동 PR · Ingress/Mesh 책임 분리) 전용 섹션 신설. 소개 페이지에도 시각화 반영. **세부 구조(아티팩트 3개 분리·rate limit 위치·{{core_version}} 소싱·Core Contract) 는 Phase 2 진입 전 재논의 예정** | 📝 방향성만 확정 |
| **포털 버전 자동 표시** | 헤더 로고 하단 `v{N.N}` 자동 — `@ControllerAdvice SystemScopeAdvice` + `@ModelAttribute("prdVersion")` + `application.yml` `devforge.prd-version` property. PRD 버전 올라갈 때마다 property 한 줄 수정으로 전역 반영 | ✅ 완료 |
| **DaisyUI v5 CSS 변수 패턴 정정** | DaisyUI v5 변수(`--color-primary` 등)는 **`oklch(...)` 전체 값**으로 정의. 기존 `hsl(var(--color-primary))` 래핑은 `hsl(oklch(...))` → invalid CSS → `rgba(0,0,0,0)` 폴백. **정석 패턴**: `var(--color-*)` 직접 참조 + 알파는 `color-mix(in oklab, var(--color-*) N%, transparent)`. CLAUDE.md 프론트엔드 규칙 정정 | ✅ 완료 |
| **.gitignore 강화** | 이미지 산출물(*.png/jpg/jpeg/gif) · `/output/` · `.claude/memory/` · 에디터 백업 파일 제외. 동작에 필요한 프로젝트 소스만 추적 | ✅ 완료 |

상세 구현: `src/main/resources/templates/pages/dashboard/detail.html` (scatter), `src/main/resources/templates/fragments/dashboard/active-alerts.html` (alerts), `src/main/resources/templates/pages/about/{overview,architecture,conventions}.html`

---

### v2.11 변경 사항 요약 (대시보드 Stub 정합성 + 실시간화 + 시각 정제)

> 📊 **구현 진행도 (2026-04-19 밤)**: Phase A(프로젝트 이름 단일 소스) + Phase B(TPS→빌드→배포 파생 체인) + Phase C(전체 ↔ 개별 집계 일치) 완료 · 3대시보드 1초 실시간 업데이트 · APM KPI 6카드 부가정보 · 이상분석 토폴로지 뷰 클릭 · scatter 분포/Z-order/샘플링 정제 · PRD TPS 10,000 시나리오 검증 · 83/83 테스트 PASS.

| 항목 | 변경 내용 | 상태 |
|---|---|---|
| **DashboardStubSeed 중앙 관리** | 신규 `common/stub/DashboardStubSeed` — 10 프로젝트 이름·환경별 realTps·프로젝트×환경 건강상태를 단일 선언. 5개 Stub (APM/CI/CD/Security/CICD-Overview) 이 이 Seed 를 읽어 파생. 이전: 각 Stub 이 독자 프로젝트 목록(payment-service, order-service 등) 선언 → 대시보드 간 이름 불일치. 이후: payment-api(DEV) OUT_OF_SYNC 가 APM Circuit Breaker OPEN + CD Drift + CI failure 로 일관 표시 | ✅ 완료 |
| **APM 지표 파생 체인 정리** | `rate = realTps × jitter` + `totalCount = round(rate × 300)` 단일 baseline 에서 도출(검산 일치). `P50/P95/P99` = scatter 실측 퍼센타일 (카드 = scatter 선 1:1). `Top Calls` = totalCount × share(rank). `Top Errors` = totalCount × errorRate × errShare. `activeRequests` = Little's Law(rate × P50). 이전: 각 수치 독립 random 값으로 생성 → TPS × 300 ≠ totalCount 불일치 | ✅ 완료 |
| **Scatter 분포 현실화** | 실무 "잘 관리되는 시스템" 비율 — 93% normal-fast / 3% normal-slow / 2% slow7s / 1.5% biz_err / 0.5% sys_err. **비율 샘플링** (초기 seed = SSE 스트림 동일 분포) → 자연 cloud. **Z-order 렌더링** (normal → slow7s → biz_err → sys_err 순) → 에러 점 항상 맨 위 가시성. 에러 카테고리 반지름 1.5배 + alpha 1.8배. 이전: 바(bar) 형태 시각 왜곡 | ✅ 완료 |
| **3대시보드 1초 실시간 업데이트** | 전체 `/dashboard` — HTMX polling (CI 파이프라인 1초, ArgoCD 1초, 시스템 카드 2초, K8S 3초). APM 탭 — SSE 1초 (scatter + KPI + Top 호출/오류). CI/CD 탭 — HTMX 신규 fragment 엔드포인트 3개 (KPI 1초, 빌드 스트림 1초, 드리프트 2초). 이전: KPI 10초·Top 정적 | ✅ 완료 |
| **APM KPI 카드 6개 부가정보** | TPS (현재)/초당 트랜잭션, 응답 P95/P99 ≥1s 초 단위, 에러율/초당 건수(rate×errorRate/100), 총 Tx (5분)/누적, Active 요청/동시 처리 중, Apdex/등급(Excellent≥0.94/Good≥0.85/Fair≥0.70/Poor). SSE metrics 이벤트로 실시간 갱신. 집계 윈도우 5분 표준 | ✅ 완료 |
| **이상분석 토폴로지 뷰 연결** | Top 호출/오류/슬로우 TX 행 클릭 → `tx-modal` graph view 오픈, 엔드포인트 기반 서비스 체인 SVG 렌더. `.top-svc-row` 클래스 + `data-endpoint`/`data-kind`/`data-duration` 속성. 이벤트 위임 (document 레벨) — SSE 갱신 후에도 리스너 유지 | ✅ 완료 |
| **환경별 realTps 현실화** | 실무 대기업 IDP 비율 — **DEV 2 / TEST 5 / SIT 30 / PRD 10,000** (SIT≈PRD의 0.3%, TEST≈0.05%, DEV≈0.02%). Scatter seed = `min(realTps × 300, 15000)` — DEV/TEST/SIT 샘플링 없음, PRD 만 15K 캡. 샘플링 라벨 `샘플링 X.XX% (seed/total)` 또는 `전체 N 표시` | ✅ 완료 |
| **Phase 2 전환 계약** | `DashboardStubSeed` → `DashboardDataProvider` 인터페이스 승격 예정. DefaultImpl 이 Prometheus/ArgoCD/Jenkins Adapter 집계. Service/DTO/UI 변경 없음. 사용자간 공유 캐시(1초 TTL) 필수 | 📝 Phase 2 |

상세:
- `docs/superpowers/plans/2026-04-19-stub-data-consistency-phase-a.md`
- `docs/superpowers/plans/2026-04-19-stub-consistency-phase-b-c.md`

---

### v2.10 변경 사항 요약 (Phase 1 아키텍처 확정)

> ⚠️ **본 PRD 는 v2.10 에서 VM 지원을 제거했습니다. 과거 버전 변경 사항(v2.5~v2.8) 의 VM 관련 설명은 히스토리로 보존됨. 현행 구조는 K8s + ArgoCD GitOps 만 지원합니다.**

> 📊 **구현 진행도 (2026-04-19)**: Task 1(VM 제거)·1.5(구조 재구조화)·2(연동 확장)·2.5(SAST 연동 + 허브 UX 통합)·3(Keycloak SSO)·**4a/4b(CI/CD 서비스/DTO/Stub 3종 분리 + UI 렌더링)** **완료** · Task 4c(Control Plane POST)~Task 8(외부 웹훅 감사) 대기. **CI/CD 탭**: APM/CI/CD 2탭 체계 · Security 흡수 · 환경 매트릭스(10 프로젝트 · 8 표시 · 스크롤) · 페르소나별 수직 섹션. 상세: `docs/superpowers/plans/2026-04-18-phase1-finalize.md` · `docs/superpowers/specs/2026-04-19-cicd-tab-design.md`

| 항목 | 변경 내용 | 상태 |
|---|---|---|
| **VM/SSH 배포 지원 제거** | 완성도 우선 — K8s + ArgoCD GitOps 전용 IDP 로 확정. `DeployType.VM` enum 값, `SshDeployService`, VM 관련 Service 4종(`VmStatusService`·`VmPortRegistry`·`VmLogService`·`NginxConfigService`) + 구현체 8종, `VmDeployment` 엔티티, `SshAdapter` 등 **16개 파일 삭제 완료** + Cluster 엔티티 VM 필드 7개 제거 | ✅ 완료 |
| **프로젝트 구조 재구조화** | Package-by-Layer 유지 + 메뉴 단위 그룹핑. **Controller 25→19** (병합 7·분리 2·위치 이동 5·유지 5), **Service 86파일 → 8폴더 분산** (`dashboard/`·`project/`·`cicd/`·`admin/{system,user,cluster,connection}`·`common/`). `Service 인터페이스 + impl/{Stub, Default}` 패턴 유지 → Phase 2 교체 포인트 보존. `Adapter` 레이어 유지. `service/dto/` 폴더 제거 — DTO 는 최상단 `dto/` 로 통일 | ✅ 완료 |
| **연동 관리 확장 (1차 — 4종)** | 기존(SCM/CI/Registry/SonarQube/Notification/Cluster) + **신규 4종 엔티티** — `OtelCollectorConnection`(OTLP endpoint), `ArtifactRepoConnection`(Nexus/Artifactory), `AuthProviderConnection`(Keycloak OIDC + LDAP 통합), `PlatformBotConnection`(Gateway routes 자동 PR 용). 각각 Repository + Service + CRUD UI + 연결 테스트 | ✅ 완료 |
| **Keycloak SSO 로그인 복원** | Spring Security OIDC 플로우 + LOCAL 로그인 병존. Realm 은 **DB 기반 동적 설정** (`DynamicClientRegistrationConfig` 가 `AuthProviderConnection` 읽어 `ClientRegistration` 등록). `User.authSource` 필드(`LOCAL`/`KEYCLOAK`/`LDAP`) 추가. `CustomOidcUserService` 가 JIT 프로비저닝 + Role 매핑. `KeycloakRoleMapping` 테이블로 group→Role 규칙 관리. 로그인 페이지에 등록된 Keycloak 개수별 SSO 버튼 자동 표시. CSRF 토큰 만료(403) 시 `/login?expired` 자동 리다이렉트 | ✅ 완료 |
| **SAST 연동 추가 (2차 — 5종 완성)** | `SastConnection` + `SastType(SPARROW\|CODEEYES)` enum 추가. `SastService` Factory + Stub 2종(`StubSparrowSastService`/`StubCodeEyesSastService`). 인터페이스 4메서드: `checkRegistration` · `fetchLatestResult` · `fetchIssues` · `testConnection`. **SystemEntity @ManyToMany** — 시스템 레벨 다중 선택(Sparrow+CodeEyes 병행 가능). 플랫폼 연동 관리 허브에 CRUD UI. **SonarQube 는 기존 엔티티 유지** (빌드 인라인형 ≠ 배치형 SAST) | ✅ 완료 |
| **연동 관리 허브 UX 통합** | `/admin/connections` 가 **단일 진입점** — 신규 5종(OTel/Artifact/Auth/Bot/SAST) **인라인 CRUD** (한 페이지에서 리스트·등록·테스트·삭제) + 기존 6종(SCM/CI/Registry/Cluster/SonarQube/Notification) **카드 링크** (`#anchor` 로 설정 페이지 점프). 사이드바 "연동 관리" 추가. 기존 "도메인 관리" 메뉴 제거(시스템 상세 드릴다운으로 자연스럽게 통합) | ✅ 완료 |
| **대시보드 콤보박스 동기화** | `/dashboard` 진입 시 `session.currentSystemId` 제거 → "전체 시스템" 복귀. `/dashboard/systems/{id}` 진입 시 자동 저장 → 콤보박스 동기화. `switchSystem` POST 가 쿠키 `dfHealthEnv` 를 읽어 `?env={선택환경}&tab=apm` 으로 리다이렉트 (DORA 선택 환경 유지) | ✅ 완료 |
| **OpenTelemetry 3층 구조** | (1) Parent POM → SDK 의존성 (2) Dockerfile → Agent JAR + `JAVA_TOOL_OPTIONS` (3) K8s 매니페스트 → env var (`OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_SERVICE_NAME` 등) |
| **Common F/W Core** | Parent POM 상속 + BOM 패턴. **Core 리포 자체는 플랫폼팀이 별도 관리** (IDP 본체 스코프 외). IDP 는 Core 의존성을 Starter 템플릿에 포함 |
| **Starter 리포 4종** | `starter-java-gateway` / `starter-java-online` / `starter-java-batch` / `starter-java-worker`. 위치: `devforge-platform/starters/*` (SCM Group). `application.yml` 에서 URL 설정 |
| **프로젝트 생성 = 템플릿 복제** | Git Template clone + Jinja/Mustache 변수 치환(`{{project_name}}`, `{{package}}` 등) → 새 리포 push. **SAST 연동 체크** — 시스템 기본값 오버라이드 허용(하이브리드), 프로젝트 저장 후 Sparrow/CodeEyes API 로 등록 여부 확인, 미등록 시 `[보안팀 등록 요청]` CTA |
| **Per-Domain Gateway** | 도메인 생성 시 해당 도메인의 `{domain}-gateway` 자동 생성. Gateway 는 Spring Cloud Gateway 기반, 각 도메인팀이 소유 |
| **Gateway routes 자동 PR** | 프로젝트(Online/Worker) 생성 시 소속 도메인 Gateway 리포의 `routes.yml` 에 **Bot 계정**이 자동 PR 생성. 경로 규약 `/{domain}/{project}/**`. 초기 수동 merge, 신뢰 쌓이면 auto-merge |
| **Ingress / Service Mesh 책임 분리** | 인프라팀이 클러스터 수준에서 관리, IDP 는 관여 없음. K8s Service DNS + OTel + Resilience4j 로 Service Mesh 없이 E-W 통신 해결 |
| **시스템 마이그레이션 전략** | 단일 필드 변경 대신 "시스템 복제 + 프로젝트 점진 이관" Phase 2 마법사. 클라우드 전환 시나리오 (예: OKD→AKS) 대응 |
| **Renovate Bot 통합** | Phase 2. Core Parent POM 새 버전 배포 시 모든 기존 프로젝트에 자동 PR 생성. SemVer 엄수 |
| **CI/CD 통합 탭 요건 확정** (2026-04-19 최종) | 업계 리서치 기반(Backstage/Cortex/Port/Humanitec/GitLab/Datadog) — **CI/CD + Security 를 한 탭으로 통합**, 페르소나별 수직 섹션 배치. 10 위젯 + KPI 배너: ①환경 매트릭스 ②내 빌드 SSE ③드리프트 ④실패 Top5 ⑤배포 히트맵 ⑥DORA ⑦승인 큐 ⑧Security Posture(3도구 통합) ⑨Vuln 트렌드 ⑩Audit Timeline. Control Plane: ▶재빌드/+수동빌드/▶Sync/⏪Rollback/✅승인. 프로모션: DEV→TEST 자동 / TEST→SIT 수동 / SIT→PRD 수동+체크리스트. 환경 탭 활성 (매트릭스·DORA·드리프트만 반응). **Stale 데이터 메타 라벨 필수** |
| **서비스 레이어는 3개로 분리** | UI 는 단일 탭이지만 내부 `CIMonitoringService` + `CDMonitoringService` + `SecurityAggregationService` 3개 인터페이스. 데이터 축 직교(CI=프로젝트×시간, CD=환경×프로젝트) 대응. Stub/Default 패턴 유지 |
| **Audit Ledger — 외부 액션 웹훅** | GitLab/Jenkins/GitHub/ArgoCD 에서 **직접 수행된 CI/CD 액션**도 웹훅으로 수신 → 사용자 매핑(SSO email 우선, User 엔티티 `externalAccounts` 맵 보조) → `AuditLog(source=EXTERNAL)` 기록. DevForge = 조직 CI/CD 단일 감사 원장. ISMS/내부통제 대응 |
| **개별 시스템 뷰 탭 구성** | **APM / CI/CD 2탭 체계** — Security 는 CI/CD 내부 섹션으로 흡수. 환경 탭 적용 범위: APM·CI/CD(CD 부분만) |

### v2.9 변경 사항 요약 (환경 인식 대시보드)

| 항목 | 변경 내용 |
|---|---|
| 환경 탭 도입 | 전체/개별 뷰 모두에 **환경 탭(DEV/TEST/SIT/PRD)** 추가. 쿠키(`dfHealthEnv`, `dfLastEnv`)로 선택 유지, `envPreset` 기반 시스템별 동적 탭 |
| 개별 시스템 뷰 3탭 | 기존 2탭 → **3탭(APM / CI / CD)** 로 분리, 파이프라인 흐름 순서 |
| Scatter 하이브리드 Y축 | 선형 0~10s → **log(10ms~2s) 37.5% + linear(2s~10s) 62.5%** 2단 스케일. 눈금: 10ms/50ms/100ms/300ms/1s/2s · 2s/4s/6s/8s/10s |
| 퍼센타일 표시 | P50/P95/P99 **헤더 뱃지**(숫자) + **scatter 점선 기준선**(색상). SSE metrics 이벤트로 10초마다 갱신. B-MON 기준값(P50 92ms / P95 266ms / P99 2s) |
| 카테고리 범례 | 건수·비율 표시 + **클릭 토글 필터** (정상/정상(7초+)/비즈니스 에러/시스템 에러, 최소 1개 유지 가드) |
| Stub 데이터 현실화 | **log-normal 분포** scatter — 정상 92%(median 92ms) + 정상-slow 3%(median 700ms) + slow7s 3% + biz_err 1.5% + sys_err 0.5% |
| SSE 점 밀도 매칭 | `getNewTransactionPoints(env)` — `tpsForEnv(env)/300` 기반 (PRD 25pt/s 등) 으로 시간이 지나도 scatter 밀도 유지 |
| Zone 1 재구성 | "IDP 연동 서비스" — 파이프라인 4단계(SCM→CI→Registry→CD) + 보조(품질/알림) 2층 그룹핑 |
| DORA 4환경 카드 | 시스템 헬스 그리드의 **환경 필터** 역할 — 카드 클릭 시 `envPreset.contains(env)` 시스템만 표시 |
| 시스템 카드 | `envPreset` 뱃지(지원 환경) + SCM/CI/REG/CD 파이프라인 상태 4뱃지 |
| Environment.cssTone() | DEV=success/TEST=warning/SIT=info/PRD=primary — 환경별 색상 일관성 |
| SSE 안정성 | `AsyncRequestNotUsableException`/`ClientAbortException` 정상 종료 처리. HTMX swap 시 IIFE 가드 + ResizeObserver + FPS ticker canvas 동기화 |
| 상세 스펙 | `docs/superpowers/specs/2026-04-17-env-aware-dashboard-design.md` 참조 |

### v2.8 변경 사항 요약

| 항목 | 변경 내용 |
|---|---|
| 전체 시스템 뷰 리디자인 | 기존 요약 카드 4개 + 시스템 그리드 → **3-Zone 구조** (Zone 1: Platform Connection Status, Zone 2: Infrastructure Health, Zone 3: Activity & DORA) |
| 개별 시스템 뷰 리디자인 | 기존 5x2 그리드 10섹션 → **2-탭(APM + CI/CD) B-MON 스타일** (파이프라인 구성 바 공통) |
| APM 탭 | Canvas SSE 스캐터 차트 + 6개 요약 카드(TPS/P95/에러율/총Tx/Active/Apdex) + Top5/에러/스파이크/SlowTX/서비스 토폴로지 |
| CI/CD 탭 | 프로젝트별 파이프라인 테이블 + ArgoCD Sync 상태 + 승인 대기 + 프로모션 흐름 |
| 대시보드 라우팅 변경 | `?systemId=X` → **`/dashboard/systems/{systemId}`** PathVariable 방식 |
| SSE 스트리밍 | `/dashboard/systems/{id}/sse/apm` 실시간 트랜잭션 scatter + metrics 스트리밍 |
| 신규 서비스 | CICDMonitoringService, DoraMetricsService, PlatformHealthService 추가 |
| 신규 DTO | APMDetailDto, CICDMonitoringDto, CIPipelineOverviewDto, ArgocdSyncOverviewDto, DoraMetricsDto, PlatformConnectionStatusDto 등 12개 |
| 구현체 수 | ~51 → **56** (Phase 6 Stub 구현체 추가) |
| RegistryType enum | 신규 — Harbor/Nexus 타입 구분용 |

### v2.7 변경 사항 요약 (MVP)

| 항목 | 변경 내용 |
|---|---|
| 대시보드 개편 | 기존 단순 위젯 → **APM 스타일 모니터링 대시보드** (Phase 1: Stub 데이터, Phase 2: LGTM 연동) |
| 전체 시스템 뷰 | 신규 — **시스템 헬스 체크 센터** (연동 상태, 인프라 상태, 24h 메트릭, 알림) |
| 개별 시스템 뷰 | 신규 — **APM 모니터링** (RED 메트릭, Top 5 Services, Recent Errors, Spike Detection, Service Graph) |
| 대시보드 분기 | `systemId` null → 전체 시스템 뷰, 있으면 → 개별 시스템 APM 뷰 |
| Phase 1 (MVP) | Stub 데이터로 UI 먼저 구현 — SystemHealthService, APMMetricsService (Mock) |
| Phase 2 (향후) | Common F/W OpenTelemetry + LGTM 스택 연동 — 실제 메트릭 수집 |

### v2.6 변경 사항 요약

| 항목 | 변경 내용 |
|---|---|
| 클러스터 메트릭 | Low #20 완료 — K8s(kubectl top) / VM(SSH top) 리소스 사용량 수집 + Admin UI 상태 페이지 |
| API 문서화 | Low #21 완료 — SpringDoc OpenAPI 2.7.0 통합 + Swagger UI (/swagger-ui.html) + 3개 REST API 예시 |
| 전체 구현 | **21개 기능 100% 완료** (Critical 5 + High 5 + Medium 6 + Low 5) |

### v2.5 변경 사항 요약

| 항목 | 변경 내용 |
|---|---|
| SCM 경로 규칙 | SCM별 상이 → **GitLab 3단계 / GitHub 2단계** 경로 규칙 명시 |
| GitLab 경로 | `{system}/{domain}/{project}` (Group/Subgroup/Repo) |
| GitHub 경로 | `{system}/{domain}-{project}` (Organization/Repo, 도메인은 이름 prefix) |
| ProjectGroup | 미지원 → **groupName nullable 필드** (DevForge UI 전용 그룹핑, SCM 무관) |
| 조직 계층 | System → Domain → Project 3계층 → **System → Domain → [Group] → Project** (그룹 선택적) |
| Virtual Threads | 미적용 → **JDK 21 Virtual Threads 활성화** (`spring.threads.virtual.enabled: true`) |
| 동시성 | `synchronized` → **ReentrantLock** (Virtual Thread pinning 방지) |

### v2.4 변경 사항 요약

| 항목 | 변경 내용 |
|---|---|
| 설정 구조 | 플랫폼 글로벌 → **플랫폼 연동 등록 + 시스템별 선택** 2단계 구조 |
| Setup Wizard | 플랫폼 Setup Wizard 폐지 → **시스템 생성 Wizard** (9단계, 등록된 연동에서 드롭다운 선택) |
| 연동 관리 | `application.yml` 직접 설정 → **DB 저장 + Admin UI CRUD** (`/admin/settings` 연동 관리 탭) |
| 연동 타입 | ScmConnection, CiConnection, RegistryConnection, Cluster, SonarQubeConnection, NotificationConnection |
| 서비스 분기 | `@ConditionalOnProperty` → **Factory 패턴** (ScmServiceFactory, CiServiceFactory, ClusterCdFactory) |
| CI 연동 모델 | GitLab CI / GitHub Actions는 **SCM 내장 CI** (별도 CiConnection 불필요), Jenkins만 독립 연동 |
| 시스템 스코프 | 플랫폼 전역 설정 → **시스템별 SCM/CI/Registry/Cluster/SonarQube 독립 선택** |
| 불변 잠금 | 플랫폼 레벨 잠금 → **시스템별 첫 프로젝트 생성 시 잠금** |
| 역할 시야 | 전체 보기 → Admin/SA: **시스템 스위처**, Domain Leader/Developer: **소속 시스템 고정** |
| UI 레이아웃 | **상단 헤더 탭 + 사이드바 컨텍스트 하이브리드 레이아웃** (헤더: 메인 네비 5개 + 유틸리티, 사이드바: 시스템 스위처 + 컨텍스트 영역) |
| 클러스터 관리 | `application.yml clusters[]` → **DB 등록 + Admin UI** (`Cluster` 엔티티) |
| application.yml | 연동 정보 전면 제거, **인프라 + 플랫폼 정책만 유지** |
| 플랫폼 초기화 | Wizard 복귀 → **시스템/연동 초기화 + 대시보드 리다이렉트** (연동 보존 선택 가능) |
| F-12 | Admin 설정 1탭 → **2탭 (플랫폼 설정 + 연동 관리)** |
| 12절 | SetupController → **SystemWizardController** |
| Phase 2 | Setup Wizard 구현 → **연동 관리 UI + 시스템 생성 Wizard 구현** |

### v2.3 변경 사항 요약

| 항목 | 변경 내용 |
|---|---|
| SCM | GitLab 단일 → **GitLab + GitHub 동시 지원** |
| CI 엔진 | jenkins \| gitlab-ci → **jenkins \| gitlab-ci \| github-actions** |
| CD (K8s) | ArgoCD 직접 sync → **매니페스트 레포 분리 + ArgoCD GitOps Pull** |
| 아키텍처 | CI가 ArgoCD 호출 → CI가 매니페스트 레포 태그 업데이트 → ArgoCD 자동 감지 |
| VM 배포 | 포트 수동관리 → **자동 배정 + Nginx reverse proxy 자동 구성** |
| Frontend | Bootstrap 5 → **Tailwind CSS 4 + DaisyUI 5** (DaisyUI 시맨틱 테마: dracula 기본 ↔ cmyk 토글) |
| JDK | JDK 17 → **JDK 21** (Virtual Threads, Pattern Matching, Sequenced Collections) |
| 조직 구조 | 플랫(Domain→Project) → **System → Domain → Project 3계층** (v2.5: [Group] 추가) |
| 역할 체계 | Admin/Developer 2단 → **Admin/SA/Domain Leader/Developer 4단** |
| 네이밍 | domain 기준 → **system 접두사 추가** (namespace, ArgoCD App, Ingress) |
| 1-4 | CI 엔진 선택지 3가지로 확장 |
| 3절 | 전체 아키텍처 플로우 재설계 |
| 6-3 | 파이프라인 템플릿 조합 확장 (3 CI × 2 CD = 6가지) |
| 11 | 기술 스택 Frontend Tailwind CSS 4 + DaisyUI 5 변경 |
| 12 | 디렉토리 구조 SCM 서비스 분리 추가 |
| 13 | application.yml SCM + 매니페스트 레포 + Nginx 설정 추가 |
| F-05 | 프로젝트 생성 → **프로젝트 관리** (Master-Detail + 트리 뷰 UI) |
| 6-3 | CI 파이프라인에 **SonarQube 분석 스테이지** 추가 |
| SCM | 플랫폼 레벨 **글로벌 SCM 설정** 명시 |
| 불변 규칙 | SCM/CI/환경 프리셋/deploy-type **Setup Wizard 1회 확정, 변경 불가** 선언 |
| 환경 | `standard-4` (dev/test/sit/prd) · `standard-3` (dev/sit/prd) **프리셋 선택** |
| F-02 | Setup Wizard 8단계 → **9단계** (환경 프리셋 선택 + 잠금 규칙 추가) |
| 잠금 시점 | 불변 항목 잠금 시점: Wizard 완료 즉시 → **첫 프로젝트 생성 시** (Grace Period) |
| Admin 설정 | **Admin 설정 페이지** (`/admin/settings`) 신규 — 변경가능 항목 관리 + 나중에 추가 지원 |
| 플랫폼 초기화 | **플랫폼 초기화** 기능 신규 — Admin 전용, 전체 리셋 + Setup Wizard 재시작 |
| 변경가능 정책 | SonarQube·인증방식·알림 등 **운영 중에도 추가/변경 가능** 정책 명시 |
| 7절 | **UI 설계 전략** 확장 — 상단 대메뉴 + 컨텍스트 사이드바 **하이브리드 레이아웃** 추가 |
| 5-4~5-7 | **브랜치 전략** — Modified Git Flow + 이미지 태그 전략 + 버전 관리 자동화 + Hotfix 워크플로우 |
| 인증 | **LDAP/AD 인증** 추가 — Local + OAuth2 + SAML + LDAP/AD 4종 혼용 지원 |
| 역할 | **4단 역할 체계** — globalRole 3단 (ADMIN/SA/DEVELOPER) + DomainRole 2단 (DOMAIN_LEADER/DEVELOPER). 승인: sit/prd = Domain Leader(UserDomainRole) 이상, Hotfix/release 브랜치 생성 = Developer 이상 |

---

## 목차

1. [제품 개요](#1-제품-개요)
2. [목표 및 성공 지표](#2-목표-및-성공-지표)
3. [전체 아키텍처](#3-전체-아키텍처)
4. [클러스터 구성 설계](#4-클러스터-구성-설계)
5. [네이밍 컨벤션](#5-네이밍-컨벤션)
6. [환경 구분 설계](#6-환경-구분-설계)
7. [UI 설계 전략](#7-ui-설계-전략)
8. [사용자 정의 및 권한](#8-사용자-정의-및-권한)
9. [기능 요구사항](#9-기능-요구사항)
10. [비기능 요구사항](#10-비기능-요구사항)
11. [기술 스택](#11-기술-스택)
12. [프로젝트 디렉토리 구조](#12-프로젝트-디렉토리-구조)
13. [application.yml 전체 설계](#13-applicationyml-전체-설계)
14. [개발 단계 Milestone](#14-개발-단계-milestone)
15. [리스크 및 대응 방안](#15-리스크-및-대응-방안)
16. [향후 확장 방향](#16-향후-확장-방향)

---

## 1. 제품 개요

### 1-1. 한 줄 정의

관리 클러스터에서 운영되며, K8s 환경의 dev · test · sit · prd 전 환경을 단일 UI로 통제하는 셀프호스팅 내부 개발자 플랫폼(IDP)

### 1-2. 핵심 가치

| 가치 | 설명 |
|---|---|
| **원클릭 프로젝트 생성** | SCM 레포 + CI 파이프라인 + CD 앱 + 4환경이 자동 구성 |
| **이미지 프로모션** | 빌드된 이미지를 dev → test → sit → prd로 순서대로 승격 (재빌드 없음, Hotfix는 sit → prd 직행) |
| **K8s GitOps 전용** | ArgoCD 기반 Pull 배포. 매니페스트 레포를 단일 진실의 원천으로 하여 선언적 롤링 배포 + drift 감지 + 자동 롤백 |
| **prd 안전 배포** | Domain Leader 이상 수동 승인 후에만 prd 배포 허용, Slack + 이메일 알림 |
| **GitOps 표준 준수** | CI는 매니페스트 레포 태그만 업데이트, ArgoCD가 Pull 방식으로 배포 |
| **시스템별 조직 관리** | System → Domain → [Group] → Project 계층 구조로 멀티시스템 환경을 체계적으로 관리. Group은 DevForge UI 전용 (SCM 무관) |

### 1-3. 두 가지 진입점

| 모드 | 설명 |
|---|---|
| **신규 환경 구축** | `install.sh` 실행 → 관리 클러스터에 전체 스택 자동 설치 → 플랫폼 기동 |
| **기존 환경 연결** | 플랫폼 실행 후 Admin이 관리 설정에서 외부 연동 등록 → 시스템 생성 시 선택 |

### 1-4. 설정 레벨 구조

DevForge는 **플랫폼 연동 등록 → 시스템별 선택** 2단계 설정 구조를 사용한다. 플랫폼 Setup Wizard는 존재하지 않으며, Admin이 로컬 계정으로 로그인한 뒤 관리 설정에서 외부 연동을 등록한다.

#### 플랫폼 연동 등록 (관리 > 설정)

Admin/SA가 외부 시스템 연결 정보를 **인스턴스 단위**로 등록한다. 하나의 연동을 여러 시스템이 공유할 수 있다.

| 연동 유형 | 예시 | 등록 정보 | 비고 |
|---|---|---|---|
| SCM | GitLab A, GitHub B | URL + API Token | 복수 인스턴스 등록 가능 |
| CI (Jenkins) | Jenkins A, Jenkins B | URL + API Token | GitLab CI / GitHub Actions는 SCM 내장 — 별도 등록 불필요 |
| Registry | Harbor, Nexus | URL + 인증 정보 | Docker Registry 호환 |
| Cluster | nonprd-k8s, prd-k8s | kubeconfig + ArgoCD URL | K8s 전용 (v2.10~) |
| SonarQube | SQ-1 | URL + API Token | 선택 사항 |
| Notification | Slack workspace, SMTP | Token / 인증 정보 | 플랫폼 알림 + 시스템 알림 채널 |

> **연동 삭제 보호**: 프로젝트가 있는 시스템이 참조하는 연동은 삭제 불가. 연동 접속정보(URL, Token) 수정은 항상 가능 — 중앙 1곳 수정으로 참조하는 모든 시스템에 반영.

#### 플랫폼 설정 (관리 > 설정)

| 설정 | 변경 | 추가 | 비고 |
|---|---|---|---|
| 인증 방식 | ✅ | ✅ | Local(항상 유지) + OAuth2 · SAML · LDAP/AD 혼용 가능, 사후 추가 가능 |
| 사용자 관리 | ✅ | ✅ | 역할 부여, 시스템/도메인 소속 지정 |
| 플랫폼 알림 | ✅ | ✅ | 플랫폼 이벤트(사용자 등록, 시스템 생성, 클러스터 헬스, 보안) 알림 |
| 브랜치 정책 | ✅ | - | feature 수명 제한·경고 시점 (기본 5일/3일, 0 = 제한 없음) |

#### 시스템 설정 (시스템 생성 Wizard)

시스템 생성 시 등록된 연동을 드롭다운으로 선택한다. 각 시스템은 독립적인 SCM/CI/환경 설정을 가진다.

| Step | 설정 | 선택 방식 | 비고 |
|---|---|---|---|
| 1 | 시스템 정보 | 이름 + 표시명 + baseDomain + 설명 입력 | 시스템 기본 정보 |
| 2 | SCM | **유형 카드**(GitLab/GitHub) 선택 → **인스턴스 드롭다운** 필터링 | 같은 유형의 복수 인스턴스 지원 |
| 3 | CI 엔진 | **필 버튼** (SCM에 따라 필터링) + Jenkins 선택 시 인스턴스 드롭다운 | SCM 내장 CI 또는 Jenkins |
| 4 | Registry | **유형 카드**(Harbor/Nexus 등) 선택 → **인스턴스 드롭다운** 필터링 | 같은 유형의 복수 인스턴스 지원 |
| 5 | 환경 프리셋 | **카드 셀렉터** (standard-4/standard-3) + env-flow 시각화 | 프로모션 순서 결정 |
| 6 | 클러스터 배정 | 환경별 **클러스터 드롭다운** (등록된 K8s 클러스터 목록) | K8s 전용 — v2.10에서 배포 유형 선택 제거 |
| 7 | SonarQube | **토글** (활성화/비활성화) + 활성화 시 `[▼ 인스턴스 선택]` | 사후 추가 가능 |
| 8 | 알림 채널 | **토글** (활성화/비활성화) + 활성화 시 `[▼ 인스턴스 선택]` | 시스템 이벤트 알림, 사후 추가 가능 |
| 9 | 확인 | **요약 그리드** (🔒/✏️ 태그) + 불변 항목 상세 테이블 + 클러스터 배정 카드 → 시스템 생성 | - |

> **예시**: A 시스템은 GitLab + GitLab CI + Harbor + standard-4 + nonprd-k8s/prd-k8s, B 시스템은 GitHub + GitHub Actions + Nexus + standard-3 + nonprd-k8s/prd-k8s — 시스템마다 완전히 독립적인 조합이 가능하다 (모두 K8s + ArgoCD GitOps 기반).

#### SCM ↔ CI 제약 매트릭스

| SCM | 사용 가능한 CI 엔진 |
|---|---|
| GitLab | `gitlab-ci` ✅ · `jenkins` ✅ · `github-actions` ❌ |
| GitHub | `github-actions` ✅ · `jenkins` ✅ · `gitlab-ci` ❌ |

> GitLab CI는 GitLab 레포에서만, GitHub Actions는 GitHub 레포에서만 동작한다. Jenkins는 SCM에 무관하게 동작한다. 시스템 생성 Wizard Step 3에서 SCM 선택에 따라 불가능한 CI 엔진은 자동 비활성화된다.

#### CI ↔ CD 관계: 독립

CI 결과물은 Docker 이미지(Registry push)이며, CD는 이미지를 가져다 배포한다. 따라서 **어떤 CI 엔진이든 어떤 CD 방식이든 조합 가능**하다. 파이프라인 템플릿이 CI × CD 조합별로 생성된다 (6-3 참조).

#### 종속 규칙

| 항목 | 종속 대상 | 설명 |
|---|---|---|
| 소스 레포 | 시스템 SCM | 시스템이 선택한 SCM 인스턴스에 생성 |
| 매니페스트 레포 | 시스템 SCM | 소스 레포와 **동일한 SCM 인스턴스**에 생성 |
| Webhook 검증 | SCM 타입 | GitLab: `X-Gitlab-Token` / GitHub: `X-Hub-Signature-256` HMAC |
| 파이프라인 템플릿 | CI 엔진 | 시스템 CI 엔진으로 결정 (6-3 참조, v2.10~ K8s 단일 CD) |
| Namespace / ArgoCD App | 클러스터 설정 | shared 여부로 결정 (4절 참조) |
| SonarQube 스테이지 | 시스템 설정 | 시스템별 활성화. 활성화 시 해당 시스템의 모든 파이프라인에 포함 |

#### 환경 프리셋

| 프리셋 | 환경 | 프로모션 순서 | 브랜치 매핑 |
|---|---|---|---|
| `standard-4` | dev, test, sit, prd | dev → test → sit → prd | `develop`→dev, `test/*`→test, `release/*`→sit, `main`→prd |
| `standard-3` | dev, sit, prd | dev → sit → prd | `develop`→dev, `release/*`→sit, `main`→prd |

> **test 환경 배포 경로 (standard-4):** test 환경의 **주 배포 경로는 프로모션**(dev→test 이미지 승격)이다. `test/*` 브랜치 push 시에도 CI 빌드 + test 환경 자동 배포가 수행되지만, 이는 QA 팀이 특정 기능을 test 환경에서 독립 검증할 때 사용하는 **보조 경로**이다. `test/*` 브랜치 생성은 Developer 이상이 DevForge UI에서 수행한다 (develop에서 분기).
> `standard-3`에서 `test/*` 브랜치는 **빌드만** 수행하고 배포하지 않는다 (DeployHistory 미기록, CI 빌드 결과만 Webhook 콜백으로 기록). `feature/*` 브랜치는 두 프리셋 모두 빌드만 수행한다 (DeployHistory 미기록).

#### 시스템 불변 규칙 (Immutable Rules)

**잠금 시점**: 시스템 내 **첫 번째 프로젝트가 생성되는 순간** 해당 시스템의 불변 항목이 확정된다.

| 시스템 상태 | 불변 항목 | 변경가능 항목 | 가능한 조치 |
|---|---|---|---|
| Wizard 진행 중 | ← 이전 버튼으로 수정 | 자유 수정 | - |
| **Wizard 완료, 프로젝트 0개 (Grace Period)** | **재설정 가능** | 자유 수정 | "재설정" 버튼 → 시스템 Wizard 재시작 |
| 프로젝트 1개 이상 | 🔒 잠금 확정 | 시스템 설정에서 수정 | - |

> **Grace Period 근거**: 프로젝트가 0개이면 SCM/CI 선택이 아직 외부 시스템에 반영되지 않아 변경해도 부작용이 없다.

**불변 항목 (시스템 내 첫 프로젝트 생성 후 변경 불가)**

| 항목 | 범위 | 규칙 | 변경 시 영향 |
|---|---|---|---|
| SCM 연동 | 시스템 | 시스템 생성 시 확정 | 전체 레포 + 매니페스트 + Webhook 이관 필요 |
| CI 엔진 | 시스템 | 시스템 생성 시 확정 | 전체 파이프라인 파일 재생성 + 빌드 이력 소실 |
| 환경 프리셋 | 시스템 | 시스템 생성 시 확정 | 프로모션 흐름 + 인프라 + 기존 프로젝트 전부 변경 필요 |
| 클러스터 배정 | 시스템 | 등록된 K8s 클러스터 중 재배정 가능 (Admin/SA 전용) | 다른 클러스터로 이전 시 ArgoCD App 재등록 필요 |
| 프로모션 순서 | 시스템 | 프리셋에 따라 고정, 건너뜀 불가 (**Hotfix 예외**: sit→prd 직행) | 이미지 검증 순서 보장 |
| 브랜치 → 환경 매핑 | 시스템 | 프리셋에 따라 고정 | 환경 자동 결정 기반 |

**변경가능 항목**

| 범위 | 항목 | 변경 | 추가 | 삭제 | 비고 |
|---|---|---|---|---|---|
| 플랫폼 | 연동 접속정보 (URL, Token) | ✅ | - | - | 중앙 1곳 수정 → 참조 시스템 전체 반영 |
| 플랫폼 | 연동 인스턴스 | - | ✅ | ⚠️ | 새 인스턴스 추가 가능. 참조 시스템이 있으면 삭제 불가 |
| 플랫폼 | 인증 방식 | ✅ | ✅ | - | OAuth2·SAML·LDAP/AD 사후 추가 (Local 항상 유지) |
| 시스템 | Registry 연동 | ✅ | - | - | 배포 이력이 있는 프로젝트가 존재하면 변경 차단 (경고 표시). 프로젝트 0개 시스템만 변경 가능 |
| 시스템 | 클러스터 배정 | ✅ | - | - | 등록된 K8s 클러스터 중 재배정 가능 (Admin/SA 전용) |
| 시스템 | SonarQube 연동 | ✅ | ✅ | - | 비활성→활성 전환, 연동 변경 가능 |
| 시스템 | 알림 채널 | ✅ | ✅ | ✅ | 채널/수신자 변경, 사후 추가 가능 |

> 시스템 불변 항목 변경이 필요한 경우: 시스템 삭제 후 재생성 또는 **플랫폼 초기화** (F-13).

### 1-5. 해결하는 문제

새 프로젝트마다 SCM 레포 + 매니페스트 레포 생성 → 파이프라인 설정 → 4개 환경 배포 설정을 수동으로 반복한다. 환경 간 설정 불일치, 이미지 버전 불일치, 온보딩 비용이 지속 발생한다.

---

## 2. 목표 및 성공 지표

| 목표 | 측정 지표 |
|---|---|
| 프로젝트 셋업 시간 단축 | 수동 2~3시간 → 자동 5분 이내 |
| 환경 설정 일관성 확보 | 4개 환경 파이프라인 표준화율 100% |
| 이미지 일관성 보장 | dev 빌드 이미지 = prd 배포 이미지 |
| 빠른 환경 구축 | install.sh 실행 후 30분 이내 전체 스택 기동 |
| prd 배포 안전성 | 수동 승인 없이 prd 자동 배포 0건 |
| 실시간 상태 조회 | 배포 로그 SSE 스트리밍, 상태 5~15초 갱신 |

---

## 3. 전체 아키텍처

### 3-1. 모던 CI/CD 4계층 구조

```
┌─────────────────────────────────────────────────────────────────┐
│  1. SCM — 단일 진실의 원천 (Source of Truth)                     │
│     GitLab: {system}/{domain}/{project}     (Group/Subgroup/Repo) │
│     GitHub: {system}/{domain}-{project}     (Org/Repo)           │
│     └── infra/{project}-manifests           K8s 매니페스트 레포   │
└────────────────────────┬────────────────────────────────────────┘
                         │ Push / Webhook
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. Control Plane — DevForge (이 시스템)                         │
│     IDP · 프로젝트 생성 · 배포 승인 · 상태 모니터링              │
│     Spring Boot + Thymeleaf + HTMX + SSE                        │
└──────┬──────────────────────────────────────────────────────────┘
       │ REST API / Webhook Trigger
       ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. CI — 빌드 & 테스트 (Data Plane)                              │
│     Jenkins · GitLab CI · GitHub Actions (조직 환경에 따라 선택)  │
│                                                                 │
│  공통 CI 흐름:                                                   │
│  소스 빌드 → 테스트 → Docker Build → Registry Push              │
│     → 매니페스트 레포 image.tag 업데이트 → Git Push             │
│     → DevForge Webhook 콜백 (빌드 완료 + 이미지 태그 기록)       │
└──────┬──────────────────────────────────────────────────────────┘
       │ 매니페스트 레포 변경 감지 (Pull)
       ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. CD — K8s GitOps (ArgoCD)                                    │
│                                                                 │
│     클러스터 내부에서 매니페스트 레포 변경 감지                   │
│     → 선언적 롤링 배포 · drift 감지 · 자동 롤백                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3-2. 관리 클러스터 + 워크로드 클러스터 구조

```
┌──────────────────────────────────────────────────────────────┐
│                 관리 클러스터 (Management Cluster)             │
│                                                              │
│  GitLab / GitHub   소스코드 + 매니페스트 레포 관리            │
│  Jenkins           CI 빌드 (SCM 무관)                        │
│  Registry          이미지 저장소 (Nexus / Harbor 무관)        │
│  ArgoCD            GitOps 오케스트레이션 · 멀티 클러스터      │
│  DevForge          개발 플랫폼 (이 시스템)                    │
└──────────────┬──────────────┬───────────────────────────────────┘
               │ ArgoCD Pull  │
               ↓              ↓
      ┌──────────────┐ ┌────────────┐
      │ non-prd      │ │ prd-app    │
      │ 클러스터     │ │ 클러스터   │
      │ shared: true │ │shared:false│
      │ dev/test/sit │ │ prd 서비스 │
      └──────────────┘ └────────────┘
```

### 3-3. 프로젝트 생성 → 배포 전체 플로우

```
개발자가 DevForge에서 프로젝트 생성
    ↓
① ScmService (시스템 SCM 연동에 따라 Factory 분기)
   [gitlab] → GitLab API: {domain}/{project} 레포 생성
              + infra/{project}-manifests 매니페스트 레포 생성
              + Webhook 등록 (→ DevForge /webhook/gitlab)
   [github] → GitHub API: {org}/{project} 레포 생성
              + {org}/{project}-manifests 매니페스트 레포 생성
              + Webhook 등록

② CiService (시스템 CI 엔진에 따라 Factory 분기)
   [jenkins]        → Multibranch Pipeline 생성 + Jenkinsfile 주입
   [gitlab-ci]      → .gitlab-ci.yml 초기 커밋
   [github-actions] → .github/workflows/*.yml 초기 커밋

③ CdService (K8s ArgoCD 단일 구현)
   → ArgoCD Application 등록 (dev/test/sit/prd)
     매니페스트 레포 + 브랜치 연결
     ignoreDifferences: Secret /data 제외

④ 완료
   - 소스 레포 · 매니페스트 레포 · CI · CD 바로가기 링크 제공
   - 대시보드 프로젝트 카드 표시
```

### 3-4. 이미지 프로모션 플로우 (K8s GitOps)

```
[정상 릴리스 플로우]
develop 브랜치 Push
    ↓
CI 빌드 (dev 환경용)
    → 이미지: develop-158-e4f5g6h → Registry Push
    → 매니페스트 레포 values-dev.yaml image.tag 업데이트
    → Git Push → ArgoCD 감지 → dev 환경 자동 배포
    → DevForge Webhook 콜백 → DeployHistory 기록
    ↓
[test 프로모션] 클릭 (standard-4만)
    → DeployHistory에서 dev imageTag 조회
    → 매니페스트 레포 values-test.yaml image.tag: develop-158-e4f5g6h 업데이트
    → ArgoCD 감지 → test 환경 배포 (동일 이미지, 재빌드 없음)
    ↓
release/1.2.0 브랜치 생성 → CI 빌드
    → 이미지: 1.2.0-rc.1 → Registry Push
    → values-sit.yaml image.tag: 1.2.0-rc.1 → ArgoCD → sit 배포
    ↓
[prd 배포 요청]
    → Domain Leader (또는 Admin/SA) Slack + 이메일 승인 요청
    → Domain Leader (또는 Admin/SA) DevForge UI [승인]
    → values-prd.yaml image.tag: 1.2.0-rc.N 업데이트 → ArgoCD 배포
    → main 머지 → Git Tag v1.2.0 → 이미지 re-tag 1.2.0

[Hotfix 플로우]
main에서 hotfix/1.2.1 브랜치 생성
    → CI 빌드: 1.2.1-hotfix.1 → sit 직접 배포 (dev/test 건너뜀)
    → [prd 승인] → 동일 이미지 prd 배포
    → main 머지 → Git Tag v1.2.1 → develop 역머지
```

### 3-5. (삭제됨 — v2.10 VM 배포 플로우 제거)

> **원칙:** 환경 건너뜀 불가 (dev → prd 직접 불가). 서버사이드 순서 검증.

---

### 3-6. Common F/W Core — 일관성 보장 프로젝트 뼈대

**목적**: 프로젝트 생성 시 Core Parent POM 을 상속받아, 조직 표준이 **기본 탑재된 상태**에서 출발. 개발자는 비즈니스 로직에만 집중하고, 로깅/헤더/모니터링/보안/에러 처리/패턴 구조는 Core 가 보장.

**소유 경계**: Common F/W Core 리포 = **플랫폼팀이 독립 관리** (IDP 본체 스코프 외). DevForge 는 Core 의존성을 Starter 템플릿에 포함 → 프로젝트 생성 시 Starter 복제 → Parent 상속 구조로 연결.

#### 3-6-1. 제공 구성요소

| 영역 | 내용 | 강제성 |
|---|---|---|
| **로깅 표준** | JSON 포맷 + Logback 공통 encoder · MDC `traceId`/`spanId` 자동 주입 · 민감정보 마스킹 필터 (Token/Password/JWT/주민번호 패턴) · `[서비스명] 메시지: key={}` 규약 | 필수 |
| **HTTP 헤더 규약** | `X-Request-Id` (요청 추적) · `X-Trace-Id` (OTel traceId) · W3C TraceContext (`traceparent`/`tracestate`) 자동 propagation · `X-User-Id` / `X-Tenant-Id` 표준화 | 필수 |
| **OTel 모니터링** | `opentelemetry-javaagent.jar` Dockerfile 탑재 (`JAVA_TOOL_OPTIONS=-javaagent:...`) + OTel SDK API 의존성 (수동 계측용) · Auto-instrumentation (Spring MVC/WebClient/JDBC/Kafka/Redis) · `OTEL_EXPORTER_OTLP_ENDPOINT` env 만 설정하면 Tempo/Prometheus 로 직송 | 필수 |
| **라이브러리 의존성 (BOM)** | Spring Boot BOM + OTel BOM + 조직 표준 라이브러리(Resilience4j · Jackson · MapStruct · Lombok · AWS SDK 등) 버전 **단일 출처**에서 관리 · 프로젝트별 버전 드리프트 방지 | 필수 |
| **보안 공통** | `SecurityFilterChain` 기본 구성 (CSRF · CORS · Rate limit) · JWT 검증 필터 (Keycloak OIDC 대응) · `AesEncryptConverter` (민감 DB 컬럼 @Convert 용) · 감사 로깅 AOP | 필수 |
| **에러 처리** | `@ControllerAdvice GlobalExceptionHandler` · RFC 7807 `ProblemDetail` 응답 표준 · `traceId` 자동 포함 · `BusinessException` 기본 계층 | 필수 |
| **패턴 구조 (CQRS · Factory · Adapter)** | **Command Service (상태 변경)** / **Query Service (조회 전용)** 분리 · **외부 연동 Factory** 패턴 스캐폴드 · **Adapter 레이어** 분리 (`adapter/` 패키지 강제) · Controller → Service → Repository 단방향 의존 ArchUnit 테스트 내장 | 필수 |
| **Resilience 기본값** | Resilience4j CircuitBreaker · Retry · TimeLimiter · Bulkhead 기본 설정 · 공통 Fallback Handler | 권장 |
| **Actuator 표준** | `/actuator/health` (liveness/readiness) · `/actuator/prometheus` · `/actuator/info` (빌드 메타: Git SHA, 이미지 태그) 기본 노출 | 필수 |

#### 3-6-2. Starter 리포 4종 — 패턴별 기본값 튜닝

| Starter | 용도 | 기본 탑재 |
|---|---|---|
| `starter-java-gateway` | API Gateway · 도메인 진입점 | Spring Cloud Gateway + RouteLocator · `routes.yml` 파일 구조 · 공통 Filter (Auth · RateLimit · Log) |
| `starter-java-online` | 동기 API 서비스 (기본형) | Spring MVC · CQRS 스캐폴드 (`command/` + `query/`) · OpenAPI 문서 · JPA 설정 |
| `starter-java-batch` | 배치 작업 (스케줄러) | Spring Batch · Quartz · Job 실행 로그 표준 · 중단/재시작 정책 |
| `starter-java-worker` | 비동기 워커 (Kafka/Queue 소비) | Spring Kafka · Idempotency · DLQ 설정 · backpressure |

**위치**: `devforge-platform/starters/*` SCM Group. `application.yml` 에서 `devforge.starters.{gateway,online,batch,worker}.url` 로 참조.

#### 3-6-3. 프로젝트 생성 플로우 (Starter 복제 + 변수 치환)

```
[DevForge UI: 프로젝트 생성]
    → 사용자 선택: 시스템 / 도메인 / 그룹 / 프로젝트명 / 패턴(Online|Batch|Worker|Gateway)
    → SCM Group 하위에 Starter 템플릿 복제 (Git clone + 새 리포 push)
    → Jinja/Mustache 변수 치환
        · {{project_name}}   → 실제 프로젝트명
        · {{package}}        → com.{system}.{domain}.{project}
        · {{system_code}}    → 시스템 식별자
        · {{artifact_id}}    → maven artifactId
        · {{core_version}}   → Common F/W Core 최신 버전 (BOM 으로 고정)
    → pom.xml: <parent> 에 Common F/W Core Parent POM 자동 주입
    → CI 파이프라인 생성 (Jenkins Job / GitLab CI / GitHub Actions)
    → ArgoCD Application 4환경(DEV/TEST/SIT/PRD) 자동 등록
    → (Online/Worker) 도메인 Gateway routes.yml 자동 PR (§3-7)
```

#### 3-6-4. 버전 정책 & 자동 업데이트 (Phase 2)

- **SemVer 엄수**: Core 는 MAJOR.MINOR.PATCH. breaking change 는 MAJOR.
- **배포 메타**: Release Note 자동 생성 (플랫폼팀 책임), 전체 프로젝트 대상 브로드캐스트 공지.
- **Renovate Bot (Phase 2)**: Core 새 버전 배포 시 기존 모든 프로젝트 리포에 자동 PR 생성. 개발팀은 PR 리뷰 → merge 만 하면 끝. SemVer 규칙 위반 시 자동 PR 차단.

---

### 3-7. Per-Domain Gateway — 도메인 진입 라우팅

**개념**: 각 도메인이 자체 Gateway 를 소유. 외부 요청은 도메인 Gateway 를 통해서만 해당 도메인 내 프로젝트에 도달.

**자동 생성**:
- 도메인 생성 시 DevForge 가 `{domain}-gateway` 프로젝트를 `starter-java-gateway` 기반으로 자동 생성
- 각 도메인팀이 **소유권** 보유 (routes 정책 · 인증 · rate limit 커스터마이징 가능)

**routes.yml 규약**:
```yaml
# 도메인 Gateway 리포의 routes.yml
routes:
  - id: payment-api
    path: /{domain}/payment/**     # 경로 규약: /{domain}/{project}/**
    service: payment-api.{domain}.svc.cluster.local
    filters:
      - AuthRequired
      - RateLimit=100/s
```

**자동 PR 플로우 (Bot 계정)**:
- 프로젝트(Online/Worker) 생성 시 DevForge 의 **Platform Bot 계정**이 해당 도메인 Gateway 리포에 PR 생성
  - `PlatformBotConnection` 엔티티로 Bot 토큰 관리 (Admin 설정 페이지)
  - 초기: **도메인 리더 수동 merge** (권한 위임 필요)
  - 신뢰 축적 후: `auto-merge` 라벨 적용 시 자동 머지
- PR 본문에 생성 메타데이터 포함 (프로젝트명 · 담당자 · 경로 · 패턴)

**Ingress / Service Mesh 책임 분리**:
- 인프라팀 영역: Ingress Controller (NGINX/Traefik/Istio Gateway) — 클러스터 수준 TLS/도메인 라우팅
- DevForge 영역: Per-Domain Gateway — 애플리케이션 수준 인증/rate limit/경로 규약
- **Service Mesh 미사용** — K8s Service DNS + Common F/W OTel + Resilience4j 로 E-W 통신 해결 (Phase 1)

---

## 4. 클러스터 구성 설계

### 4-1. 클러스터 타입

| 타입 | shared | 설명 | K8s namespace 규칙 |
|---|---|---|---|
| **통합 클러스터** | true | dev/test/sit 공유 | `{system}-{domain}-{env}` |
| **전용 클러스터** | false | 환경 전용 (주로 prd) | `{system}-{domain}` |

### 4-2. 클러스터 등록 (플랫폼 관리 설정)

클러스터는 `application.yml`이 아닌 **DB**에 등록한다. Admin/SA가 관리 설정에서 등록하며, 시스템 생성 시 환경별 클러스터를 배정한다.

```
[플랫폼 클러스터 등록 예시]

| 이름             | shared | 접속정보                          | 대상 환경      |
|-----------------|--------|----------------------------------|---------------|
| nonprd-k8s      | true   | kubeconfig + ArgoCD URL          | dev/test/sit  |
| prd-k8s         | false  | kubeconfig + ArgoCD URL          | prd 전용       |

[시스템 A 클러스터 배정 예시 (standard-4)]

| 환경  | 클러스터     |
|------|-------------|
| dev  | nonprd-k8s  |
| test | nonprd-k8s  |
| sit  | nonprd-k8s  |
| prd  | prd-k8s     |

[시스템 B 클러스터 배정 예시 (standard-3)]

| 환경  | 클러스터     |
|------|-------------|
| dev  | nonprd-k8s  |
| sit  | nonprd-k8s  |
| prd  | prd-k8s     |
```

> **K8s 전용 (v2.10~)**: 시스템 생성 Wizard Step 6에서는 환경별로 등록된 K8s 클러스터를 배정한다. 플랫폼 전체가 K8s + ArgoCD GitOps 로 통일되어 있어 배포 유형 선택 자체가 사라졌다.

### 4-3. ClusterClientRegistry — 멀티 K8s 클러스터 관리

DB에 등록된 K8s 클러스터 정보를 기반으로 K8s 클라이언트를 관리한다.

```java
@Component
public class ClusterClientRegistry {

    private final Map<String, ApiClient> k8sClientMap = new ConcurrentHashMap<>();
    private final Map<String, CoreV1Api> coreApiMap   = new ConcurrentHashMap<>();

    private final ClusterRepository clusterRepo;

    // DB에서 클러스터 정보를 로드하여 K8s 클라이언트 초기화
    @PostConstruct
    public void init() throws IOException {
        for (Cluster cluster : clusterRepo.findAll()) {
            ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(
                    new StringReader(cluster.getKubeconfig())))
                .build();
            client.setReadTimeout(0);
            k8sClientMap.put(cluster.getName(), client);
            coreApiMap.put(cluster.getName(), new CoreV1Api(client));
        }
    }

    // 클러스터 등록/수정 시 클라이언트 갱신
    public void refresh(Cluster cluster) { ... }

    public ApiClient getK8sClient(String clusterName) { ... }
    public CoreV1Api getCoreApi(String clusterName) { ... }
}
```

### 4-4. namespace 자동 결정

```java
// K8s namespace
public String resolveNamespace(Cluster cluster, String system, String domain, String env) {
    return cluster.isShared() ? system + "-" + domain + "-" + env : system + "-" + domain;
}
```

---

## 5. 네이밍 컨벤션

### 5-1. 전체 네이밍 규칙

| 항목 | 규칙 | 예시 (System: shopping, Domain: payment) |
|---|---|---|
| **GitLab SCM 경로** | Group(`{system}`) → Subgroup(`{domain}`) → Repo(`{project}`) | `shopping/payment/user-service` |
| **GitHub SCM 경로** | Org(`{system}`) → Repo(`{domain}-{project}`) | `shopping/payment-user-service` |
| 매니페스트 레포 (GitLab) | `{system}/infra/{project}-manifests` | `shopping/infra/user-service-manifests` |
| 매니페스트 레포 (GitHub) | `{system}/infra-{project}-manifests` | `shopping/infra-user-service-manifests` |
| K8s namespace (통합) | `{system}-{domain}-{env}` | `shopping-payment-dev` |
| K8s namespace (전용) | `{system}-{domain}` | `shopping-payment` |
| ArgoCD App (통합) | `{system}-{project}-{env}` | `shopping-user-service-dev` |
| ArgoCD App (전용) | `{system}-{project}` | `shopping-user-service` |
| 이미지 태그 | `{registry}/{system}/{domain}/{project}:{ver}` | `registry/shopping/payment/user-service:1.0.0` |
| Ingress/URL (non-prd) | `{env}.{domain}.{project}.{baseDomain}` | `dev.payment.user-service.shopping.co.kr` |
| Ingress/URL (prd) | `{domain}.{project}.{baseDomain}` | `payment.user-service.shopping.co.kr` |

> **baseDomain**은 System 엔티티의 속성이다. Ingress URL에서 System 접두사 대신 baseDomain이 base로 사용되므로 URL이 자연스럽다.

#### SCM 경로 규칙 (GitLab vs GitHub)

GitHub는 Organization/Repository 2단계 플랫 구조이므로, GitLab의 Group/Subgroup/Repo 3단계와 동일한 계층을 표현할 수 없다. 대신 **도메인명을 repo 이름 prefix**로 사용하여 논리적 계층을 유지한다.

```
┌─────────────────────────────────────────────────────────────┐
│ GitLab (3단계: Group / Subgroup / Repo)                      │
│  shopping (Group = System)                                   │
│  ├── payment (Subgroup = Domain)                             │
│  │   ├── payment-api        (Repo = Project)                 │
│  │   └── payment-batch      (Repo = Project)                 │
│  ├── order (Subgroup = Domain)                               │
│  │   └── order-api          (Repo = Project)                 │
│  └── infra (Subgroup = 매니페스트)                            │
│      ├── payment-api-manifests                               │
│      └── order-api-manifests                                 │
│                                                              │
│ GitHub (2단계: Organization / Repo)                           │
│  shopping (Organization = System)                            │
│  ├── payment-api            (Repo, domain prefix)            │
│  ├── payment-batch          (Repo, domain prefix)            │
│  ├── order-api              (Repo, domain prefix)            │
│  ├── infra-payment-api-manifests  (Repo, 매니페스트)          │
│  └── infra-order-api-manifests    (Repo, 매니페스트)          │
└─────────────────────────────────────────────────────────────┘
```

| 계층 | GitLab | GitHub | 비고 |
|------|--------|--------|------|
| System | Group | Organization | 1:1 매핑 |
| Domain | Subgroup | repo 이름 prefix (`{domain}-`) | GitHub는 구조적 중첩 불가 |
| Project | Repository | Repository | GitLab: `{project}`, GitHub: `{domain}-{project}` |

> **DevForge가 SCM repo 생성을 전담**하므로 이 네이밍 규칙은 자동 적용된다. 사용자는 SCM 경로를 직접 관리할 필요 없다.

#### ProjectGroup (UI 전용 그룹핑)

도메인 내 프로젝트가 많을 때 **DevForge UI에서만** 논리적으로 묶어 표시하는 기능. SCM 경로에는 반영하지 않는다.

- **구현**: Project 엔티티에 `groupName` nullable 필드 추가
- **SCM 영향**: 없음 — 그룹 변경 시 DB UPDATE만 (repo 이동/rename 불필요)
- **입력 방식**: 프로젝트 생성/수정 폼에서 autocomplete combobox (기존 그룹명 제안 + 신규 입력 가능)
- **그룹 없는 프로젝트**: 도메인 직속으로 표시 (그룹 사용은 선택적)

```
DevForge UI 트리:
  payment (도메인) — 12개 프로젝트
  ├── 📁 core (3)              ← groupName = "core"
  │   ├── payment-api
  │   ├── payment-batch
  │   └── payment-domain
  ├── 📁 settlement (2)        ← groupName = "settlement"
  │   ├── settlement-api
  │   └── settlement-batch
  ├── payment-gateway           ← groupName = null (도메인 직속)
  ├── payment-notify
  └── payment-admin

SCM에서는 (그룹 반영 없음):
  GitLab:  shopping/payment/payment-api
  GitHub:  shopping/payment-api
```

> **설계 근거**: SCM 경로에 그룹을 반영하면 그룹 변경 시 repo URL 변경 → CI/CD 설정, webhook, 개발자 git remote 전부 갱신 필요. UI 전용이면 DB UPDATE 한 줄로 조직 변경 완료.
> **엔티티 승격 시점**: 그룹 단위 기능(담당자 지정, 그룹 단위 배포 승인 등)이 필요해지면 별도 `ProjectGroup` 엔티티로 분리.

### 5-2. 매니페스트 레포 구조

```
infra/user-service-manifests/
├── Chart.yaml
├── values.yaml              # 공통 기본값
├── values-dev.yaml          # dev 환경 오버라이드 (image.tag 포함)
├── values-test.yaml
├── values-sit.yaml
└── values-prd.yaml

# values-dev.yaml 예시
image:
  repository: registry.company.com/payment/user-service
  tag: "1.0.0"              ← CI 빌드 후 자동 업데이트되는 필드
replicaCount: 1
```

### 5-3. 브랜치 → 환경 매핑 (프리셋 기반)

**`standard-4`**

| 브랜치 | 환경 | 동작 |
|---|---|---|
| `main` | prd | Domain Leader 이상 승인 후 배포 |
| `develop` | dev | CI 빌드 + 매니페스트 자동 업데이트 |
| `release/*` | sit | CI 빌드 + 매니페스트 자동 업데이트 |
| `test/*` | test | CI 빌드 + 매니페스트 자동 업데이트 |
| `feature/*` | - | 빌드만 (매니페스트 업데이트 없음) |

**`standard-3`**

| 브랜치 | 환경 | 동작 |
|---|---|---|
| `main` | prd | Domain Leader 이상 승인 후 배포 |
| `develop` | dev | CI 빌드 + 매니페스트 자동 업데이트 |
| `release/*` | sit | CI 빌드 + 매니페스트 자동 업데이트 |
| `test/*` | - | 빌드만 (매니페스트 업데이트 없음) |
| `feature/*` | - | 빌드만 (매니페스트 업데이트 없음) |

> 브랜치 → 환경 매핑은 `EnvironmentPreset`에 의해 코드 레벨에서 고정된다. 플랫폼 불변 규칙 (1-4 참조).

### 5-4. 브랜치 전략 (Modified Git Flow)

엔터프라이즈 다환경 순차 승격에 최적화된 Modified Git Flow를 채택한다. Semantic Versioning(semver.org) + Immutable Image Promotion을 결합한 글로벌 표준 방식.

#### 브랜치 유형

| 브랜치 | 유형 | 수명 | 생성 기점 | 머지 대상 | 용도 |
|---|---|---|---|---|---|
| `main` | 영구 | 영구 | - | - | 운영 릴리스 이력. prd 배포 완료된 코드만 존재 |
| `develop` | 영구 | 영구 | - | - | 개발 통합. 다음 릴리스 준비. dev 환경 자동 배포 |
| `feature/*` | 임시 | **Admin 설정** (기본 5일) | develop | develop | 기능 개발. 장기 브랜치 금지 → 큰 기능은 Feature Flag로 분리 |
| `release/{version}` | 중기 | 릴리스 완료까지 | develop | main + develop | 릴리스 후보. sit 환경 배포. RC 이미지 생성 |
| `hotfix/{version}` | 임시 | 긴급 수정 완료까지 | **main** | main + develop | 운영 긴급 수정. dev/test 건너뛰고 sit → prd 직행 |

> **feature 브랜치 수명 제한**: 장기 브랜치는 머지 충돌 폭발의 원인. Admin이 `/admin/settings` → 브랜치 정책에서 최대 수명을 설정한다 (기본 5일, 0 = 제한 없음). 경고 시점도 설정 가능 (기본 3일). 초과 시 DevForge UI에 경고 배너 표시 + 알림 발송. Admin/SA/Domain Leader가 UI에서 강제 삭제 가능.

```yaml
devforge:
  branch-policy:
    feature-max-days: 5          # 0 = 제한 없음. Admin이 /admin/settings에서 변경 가능
    feature-warn-days: 3         # 경고 시작 시점 (max-days 이전). max-days=0이면 warn도 비활성화
```

#### 개발자 워크플로우

```
1. develop에서 feature/JIRA-123 브랜치 생성
2. 코드 작성 + 커밋 + Push
3. CI 빌드 자동 실행 (이미지 생성, 배포 없음)
4. MR/PR → develop 머지 (코드 리뷰 필수)
5. develop → dev 환경 자동 배포
```

#### 릴리스 워크플로우

```
1. develop에서 release/1.2.0 브랜치 생성 (Developer 이상 — DevForge UI에서 생성)
2. pom.xml 버전: 1.2.0-SNAPSHOT → 1.2.0 (CI 자동 또는 수동)
3. CI 빌드 → 이미지 태그: 1.2.0-rc.1 → sit 환경 배포
4. 버그 수정 시 release 브랜치에 직접 커밋 → 1.2.0-rc.2, rc.3 ...
5. sit 검증 완료 → prd 승인 요청
6. Domain Leader 승인 → prd 배포 (Admin/SA override 가능)
7. release/1.2.0 → main 머지 → Git Tag v1.2.0 자동 생성
8. main → develop 역머지 → develop pom.xml을 1.3.0-SNAPSHOT으로 bump
```

#### 다수 개발자 협업 규칙

| 규칙 | 설명 |
|---|---|
| develop 직접 커밋 금지 | 반드시 feature 브랜치 → MR/PR 경유 |
| feature 브랜치 수명 제한 | Admin 설정 (기본 5일). 초과 시 경고 배너 + 알림. 큰 기능은 분할 또는 Feature Flag |
| release 브랜치 생성 권한 | Developer 이상 가능 (DevForge UI에서 생성) |
| release 브랜치 동시 1개 | 프로젝트당 활성 release 브랜치 최대 1개 |
| main 직접 커밋 금지 | release 또는 hotfix 머지만 허용 |

### 5-5. 이미지 태그 전략

빌드(BUILD)와 승격(PROMOTION)을 명확히 구분한다. **빌드는 새 코드 → 새 이미지**, **승격은 동일 이미지를 다음 환경으로 이동**(재빌드 없음).

#### 빌드 vs 승격 구분

| 구분 | 트리거 | 동작 | 이미지 |
|---|---|---|---|
| **빌드 (BUILD)** | 브랜치 Push (develop, release/*, hotfix/*) | CI 파이프라인 전체 실행 → Docker 이미지 생성 + Registry Push | **새 이미지** |
| **승격 (PROMOTION)** | DevForge UI "프로모션" 버튼 | 매니페스트 레포 image.tag 업데이트 → ArgoCD 자동 감지 | **동일 이미지** |

#### 브랜치별 이미지 태그 패턴

| 브랜치 | 이미지 태그 패턴 | 예시 | 배포 환경 |
|---|---|---|---|
| `feature/*` | `feature-{buildNo}-{shortHash}` | `feature-42-a1b2c3d` | 없음 (빌드만) |
| `develop` | `develop-{buildNo}-{shortHash}` | `develop-158-e4f5g6h` | dev |
| `test/*` | `test-{buildNo}-{shortHash}` | `test-12-b3c4d5e` | test (standard-4만) |
| `release/{ver}` | `{version}-rc.{n}` | `1.2.0-rc.3` | sit |
| `hotfix/{ver}` | `{version}-hotfix.{n}` | `1.2.1-hotfix.2` | sit (dev/test 건너뜀) |
| `main` (태그) | `{version}` | `1.2.0` | prd |

> `{buildNo}`는 CI 빌드 번호, `{shortHash}`는 Git 커밋 7자리, `{n}`은 RC/hotfix 빌드 순번.

#### 프로모션 경로와 이미지 태그 흐름

**정상 릴리스 (standard-4):**
```
develop push → [BUILD] develop-158-e4f5g6h → dev 배포
                → [PROMOTION] 동일 이미지 → test 배포
release/1.2.0 생성 → [BUILD] 1.2.0-rc.1 → sit 배포
                → [PROMOTION] 동일 이미지 → prd 배포 (승인 후)
main 머지 → [TAG] 1.2.0 (이미지 re-tag, 재빌드 아님)
```

**Hotfix:**
```
main에서 hotfix/1.2.1 생성 → [BUILD] 1.2.1-hotfix.1 → sit 배포 (dev/test 건너뜀)
                → [PROMOTION] 동일 이미지 → prd 배포 (승인 후)
main 머지 → [TAG] 1.2.1
```

### 5-6. 버전 관리 자동화

#### 버전 원천 (Source of Truth)

| 항목 | 위치 | 관리 주체 |
|---|---|---|
| 현재 개발 버전 | `develop` 브랜치 `pom.xml` (예: 1.3.0-SNAPSHOT) | CI 자동 (main 머지 후 역머지 시 bump) |
| 릴리스 버전 | `release/{version}` 브랜치 이름 + `pom.xml` | Developer 이상이 release 브랜치 생성 시 결정 |
| 운영 버전 | `main` 브랜치 Git Tag (예: v1.2.0) | CI 자동 (main 머지 시 태그 생성) |
| Hotfix 버전 | `hotfix/{version}` 브랜치 이름 + `pom.xml` | Developer 이상이 hotfix 생성 시 결정 |

#### CI 자동화 단계

```
[release 브랜치 생성]
  → CI: pom.xml 버전 SNAPSHOT 제거 (1.2.0-SNAPSHOT → 1.2.0)
  → CI: 이미지 빌드 + 태그 1.2.0-rc.1

[main 머지 감지]
  → CI: pom.xml에서 버전 읽기 (1.2.0)
  → CI: Git Tag v1.2.0 생성
  → CI: Docker 이미지 re-tag (1.2.0-rc.N → 1.2.0)
  → CI: develop에 역머지 + pom.xml을 1.3.0-SNAPSHOT으로 bump
```

#### DevForge UI 연동

| 화면 | 기능 |
|---|---|
| 프로젝트 상세 | 현재 각 환경의 배포 이미지 태그 + 운영 버전(Git Tag) + 진행 중 릴리스(release 브랜치) 표시 |
| 릴리스 생성 | 버전 번호 입력 → release 브랜치 자동 생성 + pom.xml 수정 |
| Hotfix 생성 | main 기반 hotfix 브랜치 자동 생성 + 버전 입력 |
| 프로모션 버튼 | 이전 환경의 이미지 태그를 자동 조회하여 다음 환경에 적용 |
| 브랜치 현황 | 활성 feature 브랜치 목록 + 수명 경고 배너 (설정 초과 시 강조 표시) |

> **릴리스 버전 별도 관리 불필요**: Git Tag(운영 버전 이력) + DeployHistory(환경별 이미지 태그) + 활성 브랜치 조회(진행 중 릴리스)로 자동 추적된다. 별도의 "버전 관리 DB 테이블"이나 "릴리스 관리 화면"을 만들 필요 없음.

### 5-7. Hotfix 워크플로우

운영 장애 긴급 대응을 위한 예외 경로. **정상 프로모션 순서(dev→test→sit→prd)를 건너뛰고 sit→prd만 수행**.

#### Hotfix 조건

| 항목 | 규칙 |
|---|---|
| 브랜치 생성 기점 | **main** (현재 운영 코드) |
| 브랜치 네이밍 | `hotfix/{version}` (예: hotfix/1.2.1) |
| 건너뛰는 환경 | dev, test (standard-4 기준) / dev (standard-3 기준) |
| 필수 경유 환경 | **sit** (최소 1개 환경에서 검증 필수) |
| prd 배포 | Domain Leader 이상 승인 필수 (정상 릴리스와 동일) |

#### Hotfix 프로모션 경로

**standard-4:**
```
hotfix/1.2.1 → [BUILD] 1.2.1-hotfix.1 → sit 직접 배포 (dev, test 건너뜀)
             → [PROMOTION] 동일 이미지 → prd (Domain Leader 승인)
```

**standard-3:**
```
hotfix/1.2.1 → [BUILD] 1.2.1-hotfix.1 → sit 직접 배포 (dev 건너뜀)
             → [PROMOTION] 동일 이미지 → prd (Domain Leader 승인)
```

#### Hotfix 후처리

```
1. main 머지 → Git Tag v1.2.1 생성
2. develop에 역머지 (hotfix 수정 사항 통합)
3. 활성 release 브랜치가 있으면 release에도 cherry-pick 또는 머지
```

**역머지 충돌 처리:**

| 단계 | 동작 | 주체 |
|------|------|------|
| 1. 자동 머지 시도 | SCM API로 main → develop 머지 요청 | DevForge (자동) |
| 2. 충돌 감지 | 머지 실패 응답 (HTTP 409/422) 수신 | DevForge (자동) |
| 3. 알림 발송 | Slack + 이메일로 "수동 머지 필요" 알림 (충돌 파일 목록 포함) | DevForge (자동) |
| 4. 상태 표시 | 시스템 대시보드에 "역머지 충돌 — 수동 해소 필요" 배너 표시 | DevForge UI |
| 5. 수동 해소 | Developer가 로컬에서 충돌 해소 후 MR/PR 생성 → develop 머지 | Developer |
| 6. 상태 갱신 | Webhook 콜백으로 develop 머지 확인 시 배너 자동 제거 | DevForge (자동) |

> 역머지 충돌은 hotfix와 develop의 동일 파일 수정이 겹칠 때 발생한다. 자동 해소가 불가능하므로 Developer가 로컬에서 처리하고, DevForge는 상태 추적과 알림만 담당한다.

#### 서버사이드 검증 로직

```java
// ImagePromotionService.java
public void promote(String projectName, String fromEnv, String toEnv) {
    // 1. 프로모션 순서 검증
    if (isHotfix(imageTag)) {
        // Hotfix: sit → prd만 허용
        validateHotfixPromotion(fromEnv, toEnv);
    } else {
        // 정상: 순차 프로모션만 허용 (건너뜀 불가)
        environmentPreset.validatePromotionOrder(fromEnv, toEnv);
    }
    // 2. prd 승인 검증
    if (toEnv == Environment.PRD) {
        requireApproval(projectName, imageTag);  // Domain Leader 이상 승인
    }
    // 3. 이미지 존재 확인
    registryService.verifyImageExists(imageTag);
    // 4. 매니페스트 업데이트 (K8s GitOps)
    deploy(projectName, toEnv, imageTag);
}
```

---

## 6. 환경 구분 설계

### 6-1. 환경 정의 (프리셋 기반)

시스템 생성 Wizard에서 선택한 `env-preset`에 따라 해당 시스템의 환경이 결정된다. **시스템별 독립 설정이며, 첫 프로젝트 생성 후 변경 불가.**

**`standard-4` (기본값)**

| 환경 | 자동 배포 | 브랜치 | 비고 |
|---|---|---|---|
| dev | true | `develop` | 개발 통합 테스트 |
| test | true | `test/*` | QA 기능 검증 |
| sit | false | `release/*` | 시스템 연동 테스트 (Domain Leader 이상 수동 승인 필수) |
| prd | false | `main` | 운영 (Domain Leader 이상 수동 승인 필수) |

**`standard-3`**

| 환경 | 자동 배포 | 브랜치 | 비고 |
|---|---|---|---|
| dev | true | `develop` | 개발 + QA 통합 |
| sit | false | `release/*` | 릴리즈 후보 검증 (Domain Leader 이상 수동 승인 필수) |
| prd | false | `main` | 운영 (Domain Leader 이상 수동 승인 필수) |

> `standard-3`에서 `test/*` 브랜치는 빌드만 수행하고 배포하지 않는다 (DeployHistory 미기록). `feature/*` 브랜치는 두 프리셋 모두 빌드만 수행한다 (DeployHistory 미기록).

```java
public enum EnvironmentPreset {
    STANDARD_4(List.of(Environment.DEV, Environment.TEST, Environment.SIT, Environment.PRD)),
    STANDARD_3(List.of(Environment.DEV, Environment.SIT, Environment.PRD));

    // 프로모션 시 다음 환경 결정
    public Environment nextOf(Environment current) { ... }
    // 브랜치로 환경 결정
    public Optional<Environment> resolveByBranch(String branch) { ... }
}
```

### 6-2. SCM / CI / CD 서비스 분기 — Factory 패턴

시스템마다 SCM/CI 설정이 다르므로, `@ConditionalOnProperty` 대신 **Factory 패턴**으로 런타임 분기한다. 모든 구현체가 동시에 Bean으로 등록되고, Factory가 시스템 설정에 따라 적절한 구현체를 반환한다.

#### SCM 서비스 — 시스템 SCM 연동 기준 Factory

```java
public interface ScmService {
    void createSourceRepo(Project project);
    void createManifestRepo(Project project);      // ★ 매니페스트 레포 생성
    void registerWebhook(Project project);
    void updateManifestTag(Project project,
                           String env, String imageTag); // ★ image.tag 업데이트
    void deleteRepo(Project project);
    String getScmType();                            // "gitlab" | "github"
}

@Service
public class GitLabScmService implements ScmService {
    @Override public String getScmType() { return "gitlab"; }
}

@Service
public class GitHubScmService implements ScmService {
    @Override public String getScmType() { return "github"; }
}

// 시스템 SCM 연동에 따라 구현체 반환
@Component
public class ScmServiceFactory {
    private final Map<String, ScmService> serviceMap;

    public ScmService get(SystemEntity system) {
        String scmType = system.getScmConnection().getScmType(); // "gitlab" | "github"
        return serviceMap.get(scmType);
    }
}
```

#### CI 서비스 — 시스템 CI 엔진 기준 Factory

```java
public interface CiService {
    void createPipeline(Project project);
    void triggerBuild(Project project, String branch);
    void deletePipeline(Project project);
    String getCiEngine();                           // "jenkins" | "gitlab-ci" | "github-actions"
}

@Service
public class JenkinsCiService implements CiService {
    @Override public String getCiEngine() { return "jenkins"; }
}

@Service
public class GitLabCiService implements CiService {
    @Override public String getCiEngine() { return "gitlab-ci"; }
}

@Service
public class GitHubActionsCiService implements CiService {
    @Override public String getCiEngine() { return "github-actions"; }
}

// 시스템 CI 설정에 따라 구현체 반환
@Component
public class CiServiceFactory {
    private final Map<String, CiService> serviceMap;

    public CiService get(SystemEntity system) {
        return serviceMap.get(system.getCiEngine()); // "jenkins" | "gitlab-ci" | "github-actions"
    }
}
```

> GitLab CI / GitHub Actions는 SCM 내장이므로 별도 `CiConnection`이 불필요하다. `system.getCiEngine()`이 `"gitlab-ci"`이면 `system.getScmConnection()`의 접속정보를 그대로 사용한다. Jenkins만 별도 `CiConnection` FK를 참조한다.

#### CD 서비스 — K8s ArgoCD 단일 구현 (v2.10~)

```java
public interface CdService {
    void registerApp(Project project, String env);   // 초기 등록
    void deploy(DeployRequest request);              // 매니페스트 태그 업데이트 후 sync
    void promote(String projectName, String fromEnv, String toEnv);
    void rollback(String projectName, String env, String version);
    void delete(Project project);
}

// K8s ArgoCD 단일 구현
@Service("argoCdService")
public class ArgoCDService implements CdService {
    // 프로모션 시: ScmService.updateManifestTag() → ArgoCD 자동 감지
}

// 시스템의 환경별 클러스터 배정 → ArgoCDService 반환 (단일 구현)
@Component
public class ClusterCdFactory {
    public CdService get(SystemEntity system, String env) {
        Cluster cluster = system.getClusterAssignment(env);
        return argoCdService; // v2.10~ K8s 전용
    }
}
```

> v2.10에서 `SshDeployService`/`ClusterDeployType.VM` 은 제거되었다. Factory 는 구현체 선택이 아니라 환경별 클러스터 컨텍스트(kubeconfig, ArgoCD endpoint)만 전달하는 얇은 래퍼로 남는다.

### 6-3. 파이프라인 템플릿 조합 (3 CI × K8s = 3가지)

| ci-engine | 템플릿 파일 | CD 단계 |
|---|---|---|
| jenkins | java-jenkins-k8s.Jenkinsfile | 매니페스트 레포 tag 업데이트 → git push |
| gitlab-ci | java-gitlab-k8s.gitlab-ci.yml | 매니페스트 레포 tag 업데이트 → git push |
| github-actions | java-github-k8s.yml | 매니페스트 레포 tag 업데이트 → git push |

**CI 파이프라인 공통 스테이지 순서:**

```
Build → Test → [SonarQube Analysis] → Docker Build → Push → CD 단계
```

> SonarQube 스테이지는 해당 시스템에 SonarQube 연동이 설정되어 있을 때만 파이프라인 템플릿에 포함된다.
> Quality Gate 실패 시 파이프라인을 중단하거나 경고만 표시할지는 프로젝트별 설정 가능.

**SonarQube 사후 활성화/비활성화 시 파이프라인 재생성:**

SonarQube 연동을 사후에 변경하면 기존 프로젝트의 파이프라인 파일에 SonarQube 블록 추가/제거가 필요하다. `PipelineTemplateFactory.regeneratePipelineContent()`가 변경된 context로 파이프라인을 재생성하고, SCM API로 파이프라인 파일을 업데이트 커밋한다.

| 재생성 방식 | 설명 | UI 위치 |
|---|---|---|
| **일괄 갱신** | 시스템 내 모든 프로젝트 파이프라인 재생성 | 시스템 설정 > SonarQube 토글 변경 시 확인 다이얼로그 |
| **개별 갱신** | 특정 프로젝트만 재생성 | 프로젝트 설정 > "파이프라인 재생성" 버튼 |
| **새 프로젝트만** | 기존 프로젝트는 그대로, 새 프로젝트부터 적용 | 시스템 설정 > SonarQube 토글 변경 시 확인 다이얼로그 |

> 재생성 시 SCM 커밋 메시지: `[DevForge] SonarQube {활성화|비활성화} — 파이프라인 재생성`

**SonarQube 분석 단계 (공통 패턴):**

```bash
# SonarQube 코드 분석 (시스템 SonarQube 연동 활성화 시)
sonar-scanner \
  -Dsonar.host.url=${SONARQUBE_URL} \
  -Dsonar.login=${SONARQUBE_TOKEN} \
  -Dsonar.projectKey=${SYSTEM_NAME}-${DOMAIN_NAME}-${PROJECT_NAME} \
  -Dsonar.sources=src/main \
  -Dsonar.tests=src/test \
  -Dsonar.java.binaries=target/classes

# Quality Gate 결과 확인
# PASSED → 다음 단계 진행
# FAILED → 파이프라인 중단 or 경고 (프로젝트 설정에 따라)
```

---

**K8s CI 파이프라인 CD 단계 (공통 패턴):**

```bash
# CI 완료 후 매니페스트 레포 image.tag 업데이트
git clone ${MANIFEST_REPO_URL}
cd ${PROJECT}-manifests
# values-{env}.yaml의 image.tag 업데이트
sed -i "s/tag: .*/tag: \"${IMAGE_TAG}\"/" values-${ENV}.yaml
git add .
git commit -m "ci: update ${PROJECT} image to ${IMAGE_TAG} [${ENV}]"
git push
# ArgoCD가 변경을 감지하여 자동 배포 (별도 sync 명령 불필요)
```

**DevForge Webhook 콜백 (SCM별 엔드포인트):**

CI 빌드 완료 콜백은 SCM별 Webhook 엔드포인트로 수신한다.
- GitLab: `POST /webhook/gitlab` (GitLab System Hook / Project Hook)
- GitHub: `POST /webhook/github` (GitHub Webhook)

```bash
# GitLab 예시
curl -X POST ${DEVFORGE_URL}/webhook/gitlab \
  -H "X-Gitlab-Token: ${DEVFORGE_WEBHOOK_SECRET}" \
  -H "Content-Type: application/json" \
  -d '{ "object_kind": "pipeline", "object_attributes": { "status": "success" }, ... }'

# GitHub 예시
curl -X POST ${DEVFORGE_URL}/webhook/github \
  -H "X-Hub-Signature-256: sha256=..." \
  -H "Content-Type: application/json" \
  -d '{ "action": "completed", "workflow_run": { "conclusion": "success" }, ... }'
```

### 6-4. GitOps / Secret 경계 정책

ArgoCD Application 생성 시 DevForge가 관리하는 Secret이 OutOfSync로 감지되지 않도록 `ignoreDifferences` 자동 설정.

```yaml
spec:
  ignoreDifferences:
    - group: ""
      kind: Secret
      namespace: payment-dev
      jsonPointers:
        - /data
```

> v2.0 확장: Sealed Secrets 또는 External Secrets Operator 도입으로 완전한 GitOps Secret 관리 전환.

### 6-5. (삭제됨 — v2.10 VM Nginx 자동 구성 제거)

### 6-6. Webhook 수신 + 이미지 태그 추적

```java
@RestController
public class WebhookController {

    @PostMapping("/webhook/gitlab")
    public ResponseEntity<Void> gitlabWebhook(
            @RequestHeader("X-Gitlab-Token") String token,
            @RequestBody Map<String, Object> payload) {
        webhookAuthService.verifyGitlab(token);
        // pipeline 완료, push, MR 등 object_kind로 분기
        gitlabEventService.handle(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook/github")
    public ResponseEntity<Void> githubWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {
        webhookAuthService.verifyGithub(signature, payload);
        // workflow_run, push, pull_request 등 이벤트 타입으로 분기
        githubEventService.handle(payload);
        return ResponseEntity.ok().build();
    }
}
```

---

## 7. UI 설계 전략

### 7-1. 전체 레이아웃 — 상단 헤더 탭 + 사이드바 컨텍스트 + 시스템 스코프

**상단 헤더 탭 + 사이드바 컨텍스트 하이브리드 레이아웃**을 채택한다. 상단 헤더에 메인 네비게이션 5개 탭과 유틸리티(테마/알림/사용자)를 배치하고, 좌측 사이드바에 시스템 전환 + 컨텍스트 영역을 배치한다.

```
┌─ 사이드바 ──────┬─── 헤더 ──────────────────────────────────┐
│ [DF] DevForge   │ [대시보드][도메인][프로젝트][CI/CD][관리]  🌙 🔔 👤 │
│ [▼ shopping]    ├──────────────────────────────────────────┤
│─────────────────│                                          │
│ (컨텍스트 영역)  │                                          │
│  HTMX 동적 교체  │          콘텐츠 영역                      │
│ ▶ payment       │                                          │
│   ▶ payment-api │                                          │
│                 │                                          │
│─────────────────│                                          │
│ [US] admin      │                                          │
└─────────────────┴──────────────────────────────────────────┘
```

**상단 헤더 (메인 네비 + 유틸리티, 고정)**

| 요소 | 위치 | 설명 |
|---|---|---|
| 메인 네비 탭 | 좌측 | 대시보드 / 도메인 / 프로젝트 / CI·CD / 관리 (역할별 표시/숨김) |
| 테마 토글 | 우측 | dracula ↔ cmyk 전환 |
| 알림 아이콘 | 우측 | prd 승인 대기 건수 뱃지 |
| 사용자 메뉴 | 우측 | 프로필, 비밀번호 변경, 로그아웃 |

**사이드바 구조**

| 영역 | 내용 | 비고 |
|---|---|---|
| 로고 | DevForge 로고 + 이름 | 클릭 시 대시보드 |
| 시스템 전환 | `[▼ shopping]` 드롭다운 (Admin/SA만) | Domain Leader/Developer는 시스템명만 표시 (전환 불가) |
| 컨텍스트 영역 | 헤더 탭 선택에 따라 HTMX 동적 교체 | 프로젝트 트리, CI/CD 메뉴 등 |
| 사용자 정보 | 사용자 아바타 + 이름 + 역할 | 하단 고정 |

**역할별 시스템 스코프:**

| 역할 | 시스템 전환 | 보이는 범위 | 트리 깊이 |
|---|---|---|---|
| Admin | ✅ `[▼ 전체 / shopping / banking]` | 모든 시스템 | System → Domain → [Group] → Project |
| SA | ✅ `[▼ 전체 / shopping / banking]` | 모든 시스템 | System → Domain → [Group] → Project |
| Domain Leader | ❌ 시스템명 고정 표시 | 소속 시스템만 | Domain → [Group] → Project |
| Developer | ❌ 시스템명 고정 표시 | 소속 시스템·도메인만 | Domain → [Group] → Project |

> **Admin/SA "전체" 선택 시**: 글로벌 대시보드 (전 시스템 요약) + 관리 메뉴 표시. **특정 시스템 선택 시**: 해당 시스템 스코프 대시보드 + 프로젝트/CI·CD 메뉴 활성화.

**헤더 탭별 사이드바 컨텍스트**

| 헤더 탭 | 사이드바 컨텍스트 | 접근 역할 |
|---|---|---|
| **대시보드** | 컨텍스트 최소화 (현재 시스템 요약) | 전체 |
| **도메인** | Domain 목록 트리 (프로젝트 없는 조직 구조) | 전체 |
| **프로젝트** | Domain > [Group] > Project **트리 뷰** + 검색 + [+ 프로젝트 생성] | 전체 (역할별 필터링) |
| **CI/CD** | CI/CD 메뉴(파이프라인 현황/승인 관리/배포 이력) + 프로젝트 트리(검색 필터, 클릭 → `/cicd/pipeline/{id}`) | 전체 |
| **관리** | 시스템 설정 / 도메인 관리 / 연동 관리 / 사용자 / 설정 | Admin/SA (전체) / Domain Leader (소속 도메인) |

> **"프로젝트" 탭 선택 시** 사이드바 컨텍스트 영역이 F-05의 Master-Detail 트리 뷰가 된다.

**레이아웃 선택 근거:**
- 상단 헤더 탭으로 메인 메뉴를 한눈에 파악 가능 — 5개 메뉴에 적합한 수평 배치
- 사이드바를 컨텍스트 전용으로 확보하여 프로젝트 트리 뷰 등의 활용 공간 극대화
- 시스템 전환 UI를 사이드바 상단에 자연스럽게 배치 가능
- 헤더 탭 클릭으로 사이드바 컨텍스트만 HTMX 교체 — 간결한 네비게이션 흐름
- Tailwind + DaisyUI 기본 구성으로 구현 가능 (drawer 불필요)

**HTMX 적용:**
- 헤더 탭 클릭: 페이지 이동 + `hx-get="/fragments/sidebar/{menu}"` + `hx-target="#sidebar"` 로 사이드바 컨텍스트 영역 교체
- 사이드바 트리/메뉴 클릭: `hx-get` 으로 콘텐츠 영역만 교체 (사이드바 유지)
- 시스템 전환: 전체 페이지 새로고침 — 시스템 스코프가 변경되므로
- 상단 헤더는 **정적** (HTMX 교체 대상 아님)

**역할별 메뉴 표시:**

| 메뉴 | Admin | SA | Domain Leader | Developer |
|---|---|---|---|---|
| 대시보드 | ✅ | ✅ | ✅ | ✅ |
| 도메인 | ✅ | ✅ | ✅ | ✅ |
| 프로젝트 | ✅ | ✅ | ✅ | ✅ |
| CI/CD | ✅ | ✅ | ✅ | ✅ |
| 관리 | ✅ (전체) | ✅ (전체) | ✅ (소속 도메인) | ❌ |

---

### 7-2. 실시간 UI 원칙 (HTMX + SSE)

| 기술 | 역할 | 적용 화면 |
|---|---|---|
| **Thymeleaf** | 정적 렌더링 | 페이지 초기 로딩, 폼, 설정 화면 |
| **Tailwind CSS 4 + DaisyUI 5** | UI 프레임워크 + 컴포넌트 | CDN 3줄 (base + themes.css + Tailwind), CSS-only 컴포넌트, DaisyUI 시맨틱 테마 (dracula 기본 ↔ cmyk 토글) |
| **HTMX polling** | 주기적 상태 갱신 | 대시보드, 배포 상태, 클러스터 헬스 |
| **HTMX 액션** | 버튼 클릭 비동기 처리 | 프로모션, 승인, 롤백, 빌드 트리거 |
| **SSE (SseEmitter)** | 서버 → 클라이언트 push | 배포 로그, 파이프라인 진행률 |
| **WebSocket / WebFlux** | 사용 안 함 | 양방향 불필요, Spring MVC로 충분 |

### 7-3. HTMX + DaisyUI 공존 원칙

> **핵심: DaisyUI는 CSS-only 컴포넌트이므로 HTMX swap 후 JS 재초기화가 불필요하다.**

DaisyUI의 인터랙티브 컴포넌트는 JavaScript 이벤트 리스너 없이 동작한다:
- **드롭다운**: CSS `:focus` 기반 — HTMX swap 후에도 즉시 동작
- **모달**: `<dialog>` 태그 또는 `<input type="checkbox" class="modal-toggle">` — 네이티브 HTML
- **토글/체크박스**: `<input type="checkbox">` — 네이티브 HTML
- **아코디언/트리**: DaisyUI `collapse` 또는 `<details>` 태그 — 네이티브 HTML

따라서 HTMX polling/swap 영역에서도 DaisyUI의 드롭다운, 뱃지, 프로그레스바, 모달 등을 제한 없이 사용할 수 있다.

| 영역 | 교체 방식 | DaisyUI 컴포넌트 |
|---|---|---|
| 상단 헤더 (메인 네비 탭 + 유틸리티: 테마, 알림, 사용자 메뉴) | **정적** (HTMX 교체 안 함) | ✅ 전체 사용 가능 |
| 사이드바 컨텍스트 영역 (메뉴 전환 시) | **HTMX swap** (메뉴 클릭 시 1회) | ✅ 전체 사용 가능 (CSS-only) |
| 빌드/배포 상태 (5초) | **HTMX polling** | ✅ 뱃지, 프로그레스바, 상태 아이콘 |
| 프로젝트 카드 (10초) | **HTMX polling** | ✅ 카드, 뱃지, 드롭다운 |
| 클러스터 헬스 (15초) | **HTMX polling** | ✅ 상태 표시, 알림 |
| 배포 로그 (SSE) | **SSE append** | ✅ 텍스트, 뱃지 |
| 프로모션/승인 버튼 | **hx-post + hx-confirm** | ✅ 버튼, 모달 (CSS-only) |

### 7-4. polling 간격

| 대상 | 간격 | 데이터 소스 |
|---|---|---|
| 빌드 / 배포 상태 | **5초** | DB: deploy_history |
| 프로젝트 카드 전체 | **10초** | DB: deploy_history |
| 클러스터 헬스체크 | **15초** | K8s API |

### 7-5. HTMX 구현 예시

```html
<!-- 빌드/배포 상태: 5초 갱신 -->
<div id="build-status"
     hx-get="/dashboard/status"
     hx-trigger="every 5s"
     hx-swap="innerHTML">
</div>

<!-- 프로모션 버튼 -->
<button hx-post="/projects/user-service/promote/test"
        hx-target="#project-card-user-service"
        hx-swap="outerHTML"
        hx-confirm="test 환경으로 프로모션 하시겠습니까?">
  test 프로모션
</button>
```

### 7-6. SSE 구현 (Spring MVC SseEmitter)

```java
@GetMapping(value = "/deploy/logs/{appName}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamLogs(@PathVariable String appName,
                              @PathVariable String namespace,
                              @PathVariable String podName) {
    SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
    CompletableFuture.runAsync(() ->
        k8sLogService.streamPodLogs(clusterName, namespace, podName, emitter));
    return emitter;
}
```

### 7-7. 배포 로그 소스 — K8s Pod Logs API

```java
// K8s: CoreV1Api.readNamespacedPodLog(follow=true)
public void streamPodLogs(String clusterName, String namespace,
                            String podName, SseEmitter emitter) {
    CoreV1Api coreApi = clusterClientRegistry.getCoreApi(clusterName);
    try (InputStream logStream = coreApi.readNamespacedPodLog(
            podName, namespace, null, true, null, null,
            null, null, null, 100, null).execute()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(logStream));
        String line;
        while ((line = reader.readLine()) != null) {
            emitter.send(SseEmitter.event().data(line).name("log"));
        }
        emitter.send(SseEmitter.event().data("done").name("complete"));
        emitter.complete();
    } catch (Exception e) { emitter.completeWithError(e); }
}
```

---

## 8. 사용자 정의 및 권한

### 8-1. 역할 구분 (4단 계층 — 글로벌 3단 + 도메인 2단)

역할 체계는 **글로벌 역할**(User.globalRole)과 **도메인 역할**(UserDomainRole.role)로 분리된다.

**글로벌 역할 (`Role` enum — 3단)**: 플랫폼 전체 권한 결정.

| globalRole | 소속 단위 | 권한 |
|---|---|---|
| **ADMIN** | 전체 | 시스템 생성 Wizard, 연동 등록/관리, 클러스터 등록, 플랫폼 설정, 전체 사용자 관리, 도메인 CRUD, sit/prd 배포 승인 + **플랫폼 초기화** |
| **SA** (System Admin) | 전체 | 시스템 생성 Wizard, 연동 등록/관리, 클러스터 등록, 플랫폼 설정, 전체 사용자 관리, 도메인 CRUD, sit/prd 배포 승인 |
| **DEVELOPER** | Domain | 소속 도메인 내 프로젝트 생성, feature/hotfix 브랜치 생성, dev/test 배포, 설정값 관리 |

> **Admin ≈ SA**: 운영 단계에서 Admin과 SA는 동일 권한. Admin만의 고유 권한은 플랫폼 초기화뿐.

**도메인 역할 (`DomainRole` enum — 2단)**: 특정 도메인 내 추가 권한. `UserDomainRole` 엔티티로 관리.

| domainRole | 소속 단위 | 추가 권한 |
|---|---|---|
| **DOMAIN_LEADER** | 특정 Domain | 소속 시스템 내 도메인 CRUD, 소속 도메인 멤버 관리, 프로젝트 관리, sit/prd 배포 승인 |
| **DEVELOPER** | 특정 Domain | 도메인 소속 표시 (globalRole=DEVELOPER와 동일 수준) |

> **Domain Leader의 globalRole은 DEVELOPER**이다. DL 여부는 `UserDomainRole` 테이블에서 해당 도메인에 `DOMAIN_LEADER` 역할이 할당되었는지로 판별한다. 한 사용자가 여러 도메인의 Leader가 될 수 있으며, 도메인 A에서는 Leader, 도메인 B에서는 Developer일 수 있다.

### 8-2. 역할별 접근 범위

| 대상 | Admin | SA | Domain Leader | Developer |
|---|---|---|---|---|
| 플랫폼 초기화 | ✅ | ❌ | ❌ | ❌ |
| 시스템 생성 Wizard | ✅ | ✅ | ❌ | ❌ |
| 연동 등록/관리 (SCM, CI, Registry, Cluster, SonarQube, Notification) | ✅ | ✅ | ❌ | ❌ |
| 시스템 설정 변경 (변경가능 항목) | ✅ | ✅ | ❌ | ❌ |
| 플랫폼 설정 (인증, 사용자, 플랫폼 알림) | ✅ | ✅ | ❌ | ❌ |
| 사용자 관리 (전체) | ✅ | ✅ | ❌ | ❌ |
| Domain CRUD | ✅ | ✅ | ✅ (소속 시스템) | ❌ |
| 멤버 관리 | ✅ (전체) | ✅ (전체) | ✅ (소속 도메인) | ❌ |
| 프로젝트 CRUD | ✅ | ✅ | ✅ (소속 도메인) | ✅ (소속 도메인) |
| feature 브랜치 생성 | ✅ | ✅ | ✅ | ✅ |
| release 브랜치 생성 | ✅ | ✅ | ✅ | ✅ |
| hotfix 브랜치 생성 | ✅ | ✅ | ✅ | ✅ |
| dev/test 배포 | ✅ | ✅ | ✅ | ✅ (소속 도메인) |
| sit 배포 승인 | ✅ | ✅ | ✅ (소속 도메인) | ❌ |
| prd 배포 승인 | ✅ | ✅ | ✅ (소속 도메인) | ❌ |
| 설정값 관리 | ✅ | ✅ | ✅ (소속 도메인) | ✅ (소속 도메인) |

### 8-3. 소속 관계

```
[글로벌 역할 — User.globalRole (Role enum)]
ADMIN (글로벌 — 플랫폼 초기화 고유 권한)
SA    (글로벌 — Admin과 동급, 플랫폼 초기화 제외)
DEVELOPER (글로벌 — 기본 역할)

[도메인 역할 — UserDomainRole.role (DomainRole enum)]
├── DOMAIN_LEADER ← Admin/SA가 특정 Domain에 지정 (globalRole=DEVELOPER인 사용자에게 부여)
│   └── DEVELOPER ← Admin/SA/Domain Leader가 해당 Domain에 할당
```

- Admin과 SA는 모든 System/Domain에 암묵적 접근 권한을 갖는다 (globalRole로 판별)
- 한 사용자가 여러 Domain의 Leader가 될 수 있다 (DomainRole로 판별)
- 한 Developer는 여러 Domain에 소속될 수 있다
- Domain Leader 임명은 Admin/SA만 가능 (권한 상승 방지)
- sit/prd 배포 승인 권한 검사: Admin/SA는 globalRole로, Domain Leader는 UserDomainRole로 판별

### 8-4. 인증 방식 (혼용 지원)

local 인증은 항상 활성화 (Admin 폴백 보장). SSO(OAuth2/SAML) 및 LDAP/AD는 선택 활성화하여 혼용 가능.

| 인증 방식 | 프로토콜 | 주요 대상 | 특징 |
|---|---|---|---|
| **Local** | 내장 DB | 소규모 팀, Admin 폴백 | 항상 활성화, 비활성화 불가 |
| **OAuth2** | OpenID Connect | Keycloak, Google, GitHub SSO | 웹 기반 SSO, 토큰 인증 |
| **SAML** | SAML 2.0 | 기업 IdP (Okta, Azure AD 등) | XML 기반 SSO, 엔터프라이즈 표준 |
| **LDAP/AD** | LDAP v3 | Active Directory, OpenLDAP | 기업 디렉토리 서비스 직접 연동 |

**인증 설정 저장 위치 구분:**

인증 활성화/비활성화 토글은 `PlatformSetting` DB 테이블에 저장하여 Admin UI에서 **서버 재시작 없이 런타임 변경**이 가능하다. `application.yml`에는 IdP 접속에 필요한 **인프라 URL/인증정보만** 유지한다.

| 설정 | 저장 위치 | 변경 방법 | 재시작 |
|---|---|---|---|
| `auth.oauth2.enabled` | `PlatformSetting` DB | Admin UI 토글 | 불필요 |
| `auth.saml.enabled` | `PlatformSetting` DB | Admin UI 토글 | 불필요 |
| `auth.ldap.enabled` | `PlatformSetting` DB | Admin UI 토글 | 불필요 |
| `auth.jit-provisioning` | `PlatformSetting` DB | Admin UI 토글 | 불필요 |
| `auth.default-role` | `PlatformSetting` DB | Admin UI 선택 | 불필요 |
| OAuth2 client-id/secret, issuer-uri | `application.yml` (Jasypt 암호화) | yml 수정 | 필요 |
| SAML entity-id, metadata-url | `application.yml` | yml 수정 | 필요 |
| LDAP url, base-dn, bind-dn/password | `application.yml` (Jasypt 암호화) | yml 수정 | 필요 |

> **설계 근거**: 불변 규칙 테이블에서 인증방식이 "✏️ 사후추가 가능"이므로, 활성화 토글은 DB에서 런타임 제어해야 일관성이 유지된다. IdP URL 등 인프라 설정은 변경 빈도가 낮고 Spring Security 초기화에 필요하므로 yml에 유지한다.

```yaml
# application.yml — 인프라 접속 정보만 유지 (활성화 토글은 PlatformSetting DB)
devforge:
  auth:
    local:
      enabled: true              # 항상 true (비활성화 불가, yml 고정)
    oauth2:
      # enabled는 PlatformSetting DB에서 관리 (Admin UI 토글)
      provider: keycloak         # keycloak | google | github | custom
      client-id: ENC(...)
      client-secret: ENC(...)
      issuer-uri: https://sso.company.com/realms/devforge
    saml:
      # enabled는 PlatformSetting DB에서 관리 (Admin UI 토글)
      entity-id: devforge
      metadata-url: https://idp.company.com/metadata
    ldap:
      # enabled는 PlatformSetting DB에서 관리 (Admin UI 토글)
      url: ldap://ldap.company.com:389
      base-dn: dc=company,dc=com
      user-search-base: ou=users
      user-search-filter: (sAMAccountName={0})  # AD 기본 / OpenLDAP: (uid={0})
      bind-dn: cn=readonly,dc=company,dc=com
      bind-password: ENC(암호화된값)
      group-search-base: ou=groups               # 그룹 기반 역할 매핑 (선택)
      group-search-filter: (member={0})
    # jit-provisioning, default-role은 PlatformSetting DB에서 관리
```

### 8-5. 사용자 등록 경로

| 경로 | 설명 | auth.type |
|------|------|-----------|
| **Admin 직접 생성** | Admin이 ID/PW 입력하여 local 계정 생성 | local |
| **Admin 사전 등록** | Admin이 이메일 + 역할 + 소속을 미리 세팅. SSO/LDAP 첫 로그인 시 매칭되어 즉시 권한 부여 | oauth2 / saml / ldap |
| **JIT Provisioning** | SSO/LDAP 첫 로그인 시 DevForge에 자동 등록. 역할 미할당 상태 → Admin/SA가 이후 할당 | oauth2 / saml / ldap |

### 8-6. 사용자 관리 메뉴 (Admin / SA)

| 기능 | local 사용자 | SSO 사용자 | LDAP/AD 사용자 |
|------|------------|-----------|---------------|
| 사용자 생성 | ✅ (ID/PW 입력) | ✅ (이메일 사전 등록, PW 불필요) | ✅ (이메일 사전 등록, PW는 LDAP 관리) |
| 사용자 목록/검색 | ✅ | ✅ | ✅ |
| 비밀번호 초기화 | ✅ | ❌ (IdP에서 관리) | ❌ (LDAP/AD에서 관리) |
| 비활성화/활성화 | ✅ | ✅ | ✅ |
| 소속 현황 조회 | ✅ | ✅ | ✅ |
| 인증 방식 표시 | local | oauth2 / saml | ldap |

### 8-7. 계정 초기화

- 최초 설치 시 Admin 계정 자동 생성 (local, 초기 비밀번호 콘솔 출력)
- 첫 로그인 후 비밀번호 강제 변경
- SSO/LDAP 환경에서도 Admin local 계정은 폴백으로 유지 (SSO/LDAP 장애 시 접근 보장)

---

## 9. 기능 요구사항

### F-01. 설치 자동화 (install.sh)

> **v2.4 변경**: SCM/CI/Registry는 시스템별 독립 설정으로 전환되었으므로, 설치 스크립트에서 SCM/CI 컴포넌트를 직접 설치하지 않는다. 외부 연동(GitLab, Jenkins 등)은 별도 인프라로 사전 준비하고, DevForge 기동 후 Admin이 관리 설정(`/admin/settings`)에서 연동을 등록한다.

**설치 순서**

| 단계 | 작업 | 조건 |
|---|---|---|
| 1 | 시스템 요구사항 체크 (Docker, kubectl, helm 등) | 항상 |
| 2 | k3s 설치 + kubeconfig 설정 | full 모드 |
| 3 | Helm 설치 | 항상 |
| 4 | ArgoCD helm install | 항상 |
| 5 | PostgreSQL helm install (외부 DB 미사용 시) | db-mode=embedded |
| 6 | DevForge Platform helm install | 항상 |
| 7 | application.yml 자동 생성 + Jasypt 암호화 | 항상 |
| 8 | Admin 초기 비밀번호 생성 + 콘솔 출력 | 항상 |
| 9 | 접속 URL + 초기 설정 안내 출력 | 항상 |

> **설치 후 안내**: "Admin 계정으로 로그인 → 관리 설정에서 SCM/CI/Registry/Cluster 연동 등록 → 시스템 생성 Wizard 실행"

---

### F-02. 시스템 생성 Wizard (9단계)

시스템 생성 시 Admin/SA가 실행한다. 플랫폼에 등록된 연동을 **유형 카드 + 인스턴스 드롭다운** 또는 **토글 + 드롭다운**으로 선택하는 방식이므로, **사전에 관리 설정에서 연동이 1개 이상 등록되어 있어야 한다.** 수평 스테퍼(wizard-stepper 프래그먼트)로 진행 상황을 시각화한다.

| 단계 | 내용 | UX 패턴 | 잠금 |
|---|---|---|---|
| Step 1 | **시스템 정보**: 시스템명(slug) + 표시명 + baseDomain + 설명 | 텍스트 입력 폼 | 🔒 불변 |
| Step 2 | **SCM 선택**: 유형(GitLab/GitHub) 선택 → 인스턴스 드롭다운 필터링 | **유형 카드** `.sel-card` → **인스턴스 드롭다운** (같은 유형 복수 인스턴스 지원) | 🔒 불변 |
| Step 3 | **CI 엔진 선택**: SCM에 따라 필터링된 CI 옵션 + Jenkins 시 인스턴스 드롭다운 | **필 버튼** `.pill` (비활성 옵션 `.disabled`) + 조건부 드롭다운 | 🔒 불변 |
| Step 4 | **Registry 선택**: 유형(Harbor/Nexus 등) 선택 → 인스턴스 드롭다운 필터링 | **유형 카드** `.sel-card` → **인스턴스 드롭다운** (같은 유형 복수 인스턴스 지원) | ✏️ 변경 가능 |
| Step 5 | **환경 프리셋**: standard-4 / standard-3 선택 + 환경 플로우 시각화 | **카드 셀렉터** `.sel-card` + **env-flow** (DEV→TEST→SIT→PRD 시각화) | 🔒 불변 |
| Step 6 | **클러스터 배정** (K8s 전용): 환경별 클러스터 드롭다운 (등록된 K8s 클러스터 목록) | 환경별 **클러스터 드롭다운** (환경 뱃지 `.pip-*`) | 🔒 불변 |
| Step 7 | **SonarQube (선택)**: 활성화 토글 → 인스턴스 선택 — 건너뛰기 가능 | DaisyUI **toggle** + 조건부 드롭다운 | ✏️ 변경 가능 |
| Step 8 | **알림 채널 (선택)**: 활성화 토글 → 인스턴스 선택 — 건너뛰기 가능 | DaisyUI **toggle** + 조건부 드롭다운 | ✏️ 변경 가능 |
| Step 9 | **확인**: 설정 요약 + 불변 항목 확인 경고 → 시스템 생성 | **요약 그리드** `.summary-grid` (🔒/✏️ 태그) + 불변 항목 상세 테이블 + 클러스터 배정 카드 | - |

**잠금 규칙:**
- **불변** (🔒): 시스템 내 **첫 프로젝트 생성 시** 잠금 확정. UI에서 비활성화 + 잠금 아이콘 표시.
  - Grace Period: Wizard 완료 후 프로젝트 0개 상태에서는 "재설정" 버튼으로 시스템 Wizard 재시작 가능.
  - 시스템 대시보드 배너: "설정을 확인하세요. 첫 프로젝트 생성 시 핵심 설정이 잠금됩니다."
- **변경 가능** (✏️): 시스템 설정에서 수정 가능. SonarQube·알림은 **사후 추가도 가능** (Wizard에서 건너뛰어도 나중에 활성화).
- 불변 항목 변경 필요 시: 시스템 삭제 후 재생성 (소속 프로젝트 존재 시 삭제 불가) 또는 플랫폼 초기화(F-13).

**연동 등록이 없을 때:**
- Wizard Step 2~6에서 선택할 연동이 없으면 "관리 설정에서 먼저 등록하세요" 링크 안내.
- 시스템 생성 전 최소 요구: SCM 1개 + Registry 1개 + Cluster 1개 이상 등록.

---

### F-03. 시스템 관리 (Admin / SA)

| 기능 | 설명 |
|---|---|
| 시스템 생성 | **시스템 생성 Wizard (F-02)** 실행 → SCM/CI/Registry/클러스터/환경 프리셋 선택 → SCM 최상위 그룹 자동 생성 |
| 시스템 목록 | 전체 시스템 및 소속 도메인 수 / 프로젝트 수 / 연동 요약 조회 |
| 시스템 설정 | 변경가능 항목 수정 (SonarQube 연동, 알림 채널). Grace Period 중 불변 항목 "재설정" |
| 시스템 수정 | baseDomain, 설명 수정 (시스템명은 변경 불가 — 네이밍 규칙 종속) |
| 시스템 삭제 | 소속 도메인/프로젝트 없을 때만 삭제 가능. 외부 시스템 리소스 정리 포함 |

---

### F-04. 도메인 관리 (Admin / SA / Domain Leader)

| 기능 | 설명 |
|---|---|
| 도메인 생성 | 소속 시스템 선택 + 도메인명(slug) 입력 → SCM 서브그룹 자동 생성 |
| 도메인 목록 | 소속 시스템 내 도메인 및 프로젝트 수 조회 (Domain Leader는 소속 시스템만) |
| 도메인 수정 | 설명 수정 (도메인 slug는 변경 불가 — 네이밍 규칙 종속) |
| 도메인 삭제 | 소속 프로젝트 없을 때만 삭제 가능 |
| 배포 환경 확인 | 시스템 생성 시 배정된 클러스터/환경 프리셋 확인 (읽기 전용 — 변경은 시스템 설정에서) |
| Domain Leader 지정 | 사용자를 해당 도메인의 Domain Leader로 지정/해제 — Admin/SA만 |
| 멤버 할당 | 사용자를 해당 도메인의 Developer로 할당/해제 (Domain Leader도 가능) |

---

### F-05. 프로젝트 관리

**화면 구성: 헤더 "프로젝트" 탭 → 사이드바 컨텍스트 트리 + 콘텐츠 Detail** (7-1 레이아웃 참조)

헤더에서 "프로젝트" 탭을 선택하면 사이드바 컨텍스트 영역이 트리 뷰로 변경되고, 콘텐츠 영역에 상세/생성 폼이 표시된다.

```
┌─ 사이드바 ──────┬─── 헤더 ──────────────────────────────────┐
│ [DF] DevForge   │ [대시보드][도메인][프로젝트][CI/CD][관리]  🌙 🔔 👤 │
│ [▼ shopping]    ├──────────────────────────────────────────┤
│─────────────────│                                          │
│ 🔍 검색          │  [프로젝트 상세]                          │
│ v 결제           │  payment-api                             │
│   * pay-api     │  기본정보 + 연동정보 단일 뷰              │
│   * pay-batch   │  (소스 레포, 매니페스트 레포, CI Job 링크)  │
│ > 주문           │                                          │
│ > 회원           │  [CI·CD 파이프라인 →]                     │
│ [+ 프로젝트]     │                                          │
│─────────────────│                                          │
│ [US] developer  │                                          │
└─────────────────┴──────────────────────────────────────────┘
```

| 기능 | 설명 |
|---|---|
| 컨텍스트 트리 | 시스템 스코프 기준: Admin/SA는 Domain > Project (시스템 전환으로 스코프 변경), Domain Leader/Developer는 소속 Domain > Project (2 레벨) |
| 트리 노드 클릭 | HTMX `hx-get`으로 **콘텐츠 영역만** 교체 (사이드바 컨텍스트 유지) |
| 역할별 필터링 | Admin/SA: 선택한 시스템의 전체 트리 / Domain Leader: 소속 도메인만 / Developer: 소속 도메인만 |
| 프로젝트 생성 | 트리에서 도메인 선택 후 "+ 프로젝트" 클릭 → 콘텐츠에 생성 폼 로딩 (시스템/도메인 자동 세팅) |
| 프로젝트 상세 | 프로젝트 노드 클릭 → 콘텐츠에 기본정보 + 연동정보 단일 뷰 (탭 없음). CI·CD 파이프라인 바로가기 링크 → `/cicd/pipeline/{id}` |
| 트리 펼침/접힘 | DaisyUI `collapse` 또는 `<details>` 태그로 처리 (CSS-only, JS 불필요) |
| 검색/필터 | 컨텍스트 영역 상단에 프로젝트명 검색, 도메인 필터, 상태 필터 |

> 도메인 관리는 헤더 "관리" 탭 → 사이드바 컨텍스트 "도메인 관리"로 분리. 프로젝트 관리(Developer 접근 가능)와 도메인 관리(Admin/SA/Domain Leader)는 헤더 탭 수준에서 분리된다.

**자동 생성 결과물**

```
SCM (GitLab or GitHub)
├── {system}/{domain}/{project}           소스 레포
│   ├── main / develop 브랜치 보호
│   ├── Webhook 등록 (→ DevForge /webhook/gitlab 또는 /webhook/github)
│   └── 초기 커밋 (README, Dockerfile, CI 파이프라인 파일)
└── {system}/infra/{project}-manifests    매니페스트 레포
    ├── Chart.yaml
    ├── values.yaml
    ├── values-dev.yaml    (image.tag: latest)
    ├── values-test.yaml
    ├── values-sit.yaml
    └── values-prd.yaml

CI
├── [jenkins]         Multibranch Pipeline + Jenkinsfile 템플릿
├── [gitlab-ci]       .gitlab-ci.yml 커밋
└── [github-actions]  .github/workflows/ci.yml 커밋

CD (K8s GitOps)
├── ArgoCD App: {system}-{project}-dev   → nonprd-cluster, values-dev.yaml
├── ArgoCD App: {system}-{project}-test  → nonprd-cluster, values-test.yaml
├── ArgoCD App: {system}-{project}-sit   → nonprd-cluster, values-sit.yaml
└── ArgoCD App: {system}-{project}       → prd-cluster,    values-prd.yaml
    └── ignoreDifferences: Secret /data 제외
```

---

### F-06. 이미지 프로모션

**K8s 프로모션 흐름 (단일 구현):**
1. DevForge → `ScmService.updateManifestTag(project, toEnv, imageTag)`
2. 매니페스트 레포 `values-{env}.yaml` image.tag 업데이트 → Git Push
3. ArgoCD 자동 감지 → 배포 (DevForge가 ArgoCD sync API 직접 호출 불필요)
4. `DeployHistory` 기록

**프로모션 순서 강제 (서버사이드 검증):**
`EnvironmentPreset`에 정의된 순서만 허용. 건너뜀 불가. **단, Hotfix는 예외.**

| 유형 | standard-4 | standard-3 |
|---|---|---|
| 정상 릴리스 | dev → test → sit → prd (건너뜀 불가) | dev → sit → prd (건너뜀 불가) |
| **Hotfix** | sit → prd (dev, test 건너뜀) | sit → prd (dev 건너뜀) |

> Hotfix 판별: 이미지 태그에 `-hotfix.` 패턴 포함 또는 `hotfix/*` 브랜치 기원. sit은 반드시 경유 (최소 1환경 검증 보장).

---

### F-07. (삭제됨 — v2.10 VM 배포 현황 관리 제거)

---

### F-08. Config / Secret 관리

| 기능 | 설명 |
|---|---|
| 환경별 Key-Value 입력 | 환경별 설정값 입력 |
| Secret 암호화 | DB 저장 시 AES-256 암호화 (`JPA AttributeConverter`) |
| K8s Secret 자동 주입 | `ignoreDifferences` 설정으로 ArgoCD OutOfSync 방지 |
| 변경 이력 | 감사 로그 |

---

### F-09. 롤백

| 환경 | 동작 |
|---|---|
| K8s | 매니페스트 레포 이전 image.tag로 되돌리기 → Git Push → ArgoCD 자동 배포 |

---

### F-10. 대시보드 ★ REDESIGNED (IDP 플랫폼 + APM + CI/CD 모니터링)

**2-Level 대시보드**: 플랫폼 헬스 체크 센터 (Level 1) + 개별 시스템 APM·CI/CD 모니터링 (Level 2, 2-탭)

**라우팅**: `/dashboard` → 전체 시스템 뷰, `/dashboard/systems/{systemId}` → 개별 시스템 뷰

**구현 전략**: Phase 1 (MVP) — Stub 데이터로 UI 먼저 구현, Phase 2 (향후) — LGTM + OpenTelemetry 실제 연동

---

#### 뷰 1: IDP 플랫폼 헬스 체크 센터 (`/dashboard`)

**대상**: Admin, SA, Domain Leader (소속 시스템만)

**레이아웃**: 3-Zone 구조

```
┌─────────────────────────────────────────────────────────┐
│ Zone 1: Platform Connection Status Bar                   │
│ [SCM 2/2 ✓] [CI 3/3 ✓] [Registry 1/1 ✓] [CD 2/2 ✓]   │
│ [Cluster 4/4 ✓] [SonarQube 1/1 ✓] [Notification 2/2 ✓] │
├─────────────────────────────────────────────────────────┤
│ Zone 2: Infrastructure Health                            │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │
│ │ K8s 클러스터 │ │ ArgoCD Sync  │ │ CI Pipeline  │      │
│ │ (CPU/MEM/Pod)│ │ (정상/비정상)│ │ (성공/실패)  │      │
│ └──────────────┘ └──────────────┘ └──────────────┘      │
├─────────────────────────────────────────────────────────┤
│ Zone 3: Activity & DORA                                  │
│ ┌────────────┐ ┌────────────┐ ┌────────────┐            │
│ │DORA Metrics│ │최근 배포   │ │승인 대기   │            │
│ └────────────┘ └────────────┘ └────────────┘            │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐                  │
│ │시스템 A  │ │시스템 B  │ │시스템 C  │  (시스템 그리드) │
│ └──────────┘ └──────────┘ └──────────┘                  │
└─────────────────────────────────────────────────────────┘
```

**Zone 1: Platform Connection Status** (PlatformHealthService)

| 항목 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| 연동 유형별 상태 | SCM/CI/Registry/CD/Cluster/SonarQube/Notification | Mock: 정상/비정상 랜덤 | API health check |
| 표시 형식 | `[유형 N/M ✓]` 뱃지 (정상/전체 개수) | Mock 데이터 | 실시간 헬스체크 |
| 갱신 주기 | 30초 (HTMX polling) | - | - |

**Zone 2: Infrastructure Health** (ClusterMonitoringService, CICDMonitoringService)

| 섹션 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| K8s 클러스터 | 클러스터별 CPU/Memory 사용률, Node 수, Pod 수 | Mock: 랜덤 메트릭 | kube-state-metrics + node_exporter |
| ArgoCD Sync 요약 | Synced/OutOfSync/Degraded 앱 수 | Mock: 랜덤 상태 | ArgoCD API |
| CI Pipeline 요약 | 성공/실패/진행중 빌드 수 (24h) | Mock: 랜덤 카운트 | CI API + DeployHistory |
| 갱신 주기 | 15초 (HTMX polling) | - | - |

**Zone 3: Activity & DORA** (DoraMetricsService, DeployQueryService, SystemHealthService)

| 섹션 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| DORA 메트릭 | 배포 빈도, 리드 타임, MTTR, 변경 실패율 | Mock: 랜덤 메트릭 | DeployHistory 기반 계산 |
| 최근 배포 | 최근 10건 배포 이력 (프로젝트명, 환경, 상태, 시각) | Mock: 랜덤 이력 | DeployHistory |
| 승인 대기 | 미처리 prd 승인 요청 목록 | Mock: 0~3건 | ApprovalRequest |
| 시스템 그리드 | 시스템별 연동 상태 + 인프라 요약 + 24h 메트릭 + 알림 | Mock: 랜덤 | API health check + Prometheus |
| 갱신 주기 | 10초~60초 (섹션별 차등 HTMX polling) | - | - |

---

#### 뷰 2: 개별 시스템 모니터링 (`/dashboard/systems/{systemId}`)

**대상**: 전체 (시스템 선택 시 접근)

**레이아웃**: 파이프라인 구성 바 (공통, 상단 고정) + 2-탭 (APM / CI·CD)

```
┌─────────────────────────────────────────────────────────┐
│ 파이프라인 구성 바 (양 탭 공통, 상단 고정)                │
│ [GitLab] → [Jenkins] → [Harbor] → [ArgoCD]              │
├─────────────────────────────────────────────────────────┤
│ [APM 모니터] [CI/CD 모니터]          ← 탭 전환 (HTMX)   │
├─────────────────────────────────────────────────────────┤
│ (탭 콘텐츠)                                              │
└─────────────────────────────────────────────────────────┘
```

**탭 전환**: `hx-get="/dashboard/systems/{id}?tab=apm|cicd"` + `hx-target="#tab-content"` + `hx-push-url="true"`

---

##### APM 모니터 탭 (B-MON 스타일)

```
[6개 요약 카드: TPS / 응답P95 / 에러율 / 총 Tx / Active / Apdex]
[트랜잭션 스캐터 차트 (전폭, Canvas SSE 실시간)]
[호출 Top5 | 에러 Top5 | 최근 오류]          ← 3열
[스파이크 감지 | 슬로우 TX Top5]              ← 2열
[서비스 의존성 토폴로지 (전폭)]
```

**6개 요약 카드** (APMDetailDto.REDMetrics)

| 카드 | 데이터 | 색상 임계 |
|---|---|---|
| TPS | `redMetrics.tps` | 기본 green |
| 응답 P95 | `redMetrics.p95ResponseMs` | >3000ms amber |
| 에러율 | `redMetrics.errorRate` | >1% amber, >5% red |
| 총 트랜잭션 | `redMetrics.totalCount` | 정보성 |
| Active 요청 | `redMetrics.activeRequests` | 정보성 |
| Apdex | `redMetrics.apdex` | <0.7 amber, <0.5 red |

**트랜잭션 스캐터 차트** (Canvas 기반, SSE 실시간)

| 요소 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| X축 | 최근 5분, 30초 단위 | Mock timeline | Tempo timestamp |
| Y축 | 응답시간 (ms) | Mock: 10~2000ms | Tempo trace duration |
| 점 색상 | 초록(정상), 파랑(7s+ 지연), 노랑(4xx), 빨강(5xx/timeout) | Mock: 랜덤 점 | Tempo traces |
| 렌더링 | **순수 JavaScript Canvas** (외부 차트 라이브러리 없음) | - | - |
| 실시간 | SSE `/dashboard/systems/{id}/sse/apm` — 매초 scatter 점 1~3개 + 10초마다 metrics 갱신 | Stub 랜덤 | Tempo + Prometheus |

**나머지 섹션**

| 섹션 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| Top 5 호출 | 최다 호출 엔드포인트 | Mock: /api/payment 등 | Prometheus topk() |
| Top 5 에러 | 최다 에러 엔드포인트 | Mock: /api/external-call 등 | Prometheus topk() |
| 최근 오류 | 타임스탬프 + 에러 메시지 + HTTP 코드 (최근 10건) | Mock 데이터 | Loki query |
| 스파이크 감지 | 호출/오류 급상승 (vs 1h 전 대비 %) | Mock: +320% 등 | Prometheus delta() |
| 슬로우 TX Top5 | 응답 시간이 가장 긴 트랜잭션 5개 | Mock 데이터 | Tempo traces |
| 서비스 토폴로지 | 서비스 의존성 그래프 (노드 + 엣지 + latency) | Mock: 토폴로지 | Tempo service graph |

**갱신 주기**:
- Transaction scatter: SSE 실시간 (1초 간격)
- RED 메트릭: SSE 연동 (10초 간격)
- Top 5, Recent Errors, Spike, SlowTX: 5초 (HTMX polling)
- Service Topology: 10초 (HTMX polling)

---

##### CI/CD 모니터 탭

| 섹션 | 내용 | Phase 1 (Stub) | Phase 2 (실제) |
|---|---|---|---|
| 프로젝트별 파이프라인 테이블 | 프로젝트명, 최근 빌드 상태, 이미지 태그, 환경별 배포 버전 | Mock 데이터 | CI API + DeployHistory |
| ArgoCD Sync 상태 | 환경별 ArgoCD App 상태 (Synced/OutOfSync/Degraded) | Mock 상태 | ArgoCD API |
| 승인 대기 | 미처리 승인 요청 목록 (프로젝트, 환경, 요청자, 시각) | Mock 데이터 | ApprovalRequest |
| 프로모션 흐름 | dev→test→sit→prd 프로모션 진행 현황 시각화 | Mock 데이터 | DeployHistory |
| 빌드 실패 목록 | 최근 실패 빌드 목록 (프로젝트, 브랜치, 에러 요약) | Mock 데이터 | CI API |

**갱신 주기**: 10초 (HTMX polling)

**HTMX Fragment 엔드포인트**:
- `/dashboard/systems/{id}/fragments/pipeline-table`
- `/dashboard/systems/{id}/fragments/argocd-status`
- `/dashboard/systems/{id}/fragments/pending-approvals`

---

#### Phase 1 구현 (MVP — Stub 데이터)

**신규 Service (Mock 구현체)**

| 클래스 | 역할 | 주요 메서드 |
|---|---|---|
| SystemHealthService | 전체 시스템 헬스 데이터 | getSystemHealthSummary(), getSystemHealthCards() |
| APMMetricsService | 개별 시스템 APM 데이터 | getAPMDashboard(), getAPMDetail(), getNewTransactionPoints() |
| PlatformHealthService | 플랫폼 연동 헬스 | getPlatformConnectionStatus() |
| DoraMetricsService | DORA 메트릭 | getDoraMetrics() |
| CICDMonitoringService | CI/CD 모니터링 데이터 | getCICDMonitoring(), getArgocdSyncSummary(), getCIPipelineSummary(), getPipelineStatus() |

**DashboardController 라우팅**

```java
// 전체 시스템 헬스 체크 센터 (3-Zone 구조)
@GetMapping("/dashboard")
public String apmOverview(Model model, HttpServletRequest request) {
    // Zone 1: 플랫폼 연동 헬스 상태
    model.addAttribute("platformStatus", platformHealthService.getPlatformConnectionStatus());
    // Zone 2: 인프라 헬스
    model.addAttribute("clusterHealthList", clusterMonitoringService.getAllClusterHealth());
    model.addAttribute("argocdSync", cicdMonitoringService.getArgocdSyncSummary());
    model.addAttribute("ciPipelineSummary", cicdMonitoringService.getCIPipelineSummary());
    // Zone 3: Activity & DORA
    model.addAttribute("doraMetrics", doraMetricsService.getDoraMetrics());
    model.addAttribute("recentDeploys", deployQueryService.findRecentDeploys());
    model.addAttribute("pendingApprovals", deployQueryService.findPendingApprovals());
    model.addAttribute("systems", systemHealthService.getSystemHealthCards(currentUser));
    return "pages/dashboard/overview";
}

// 개별 시스템 모니터링 (2-탭: APM + CI/CD)
@GetMapping("/dashboard/systems/{systemId}")
public String apmSystemDetail(@PathVariable Long systemId,
                               @RequestParam(defaultValue = "apm") String tab,
                               Model model) {
    model.addAttribute("system", systemService.findById(systemId));
    model.addAttribute("currentTab", tab);
    model.addAttribute("pipelineStatus", cicdMonitoringService.getPipelineStatus(systemId));
    if ("apm".equals(tab)) {
        model.addAttribute("apmDetail", apmMetricsService.getAPMDetail(systemId));
    } else if ("cicd".equals(tab)) {
        model.addAttribute("cicd", cicdMonitoringService.getCICDMonitoring(systemId));
    }
    return "pages/dashboard/detail";
}

// SSE: 실시간 트랜잭션 scatter + metrics 스트리밍 (5분 타임아웃)
@GetMapping(value = "/dashboard/systems/{systemId}/sse/apm",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamApmData(@PathVariable Long systemId) { ... }
```

**Thymeleaf Templates**

| 템플릿 | 경로 | 역할 |
|---|---|---|
| overview.html | pages/dashboard/overview.html | 전체 시스템 뷰 (3-Zone 구조) |
| detail.html | pages/dashboard/detail.html | 개별 시스템 뷰 (파이프라인 바 + 2-탭) |
| system-cards.html | fragments/dashboard/system-cards.html | 시스템 카드 그리드 fragment |
| platform-connections.html | fragments/dashboard/platform-connections.html | Zone 1 연동 상태 fragment |
| cluster-health.html | fragments/dashboard/cluster-health.html | Zone 2 클러스터 헬스 fragment |
| dora-metrics.html | fragments/dashboard/dora-metrics.html | Zone 3 DORA 메트릭 fragment |
| apm-sections/*.html | fragments/dashboard/apm-sections/ | APM 탭 섹션별 fragment (9개) |
| system-detail/*.html | fragments/dashboard/system-detail/ | CI/CD 탭 fragment (pipeline-table, argocd-status, pending-approvals) |

---

#### Phase 2 연동 (향후 — LGTM + OpenTelemetry)

**Common Framework 통합**

| 항목 | 구현 |
|---|---|
| OpenTelemetry SDK | Common Framework parent POM에 opentelemetry-javaagent 추가 |
| Auto-instrumentation | Spring Boot, JDBC, HTTP Client 자동 계측 |
| Exporter | OTLP Exporter → Tempo (traces) + Prometheus (metrics) |

**LGTM 스택 설치 (관리 클러스터)**

| 컴포넌트 | 역할 | 수집 |
|---|---|---|
| Tempo | Distributed Tracing | OpenTelemetry traces (OTLP) |
| Prometheus | Metrics 수집 | OpenTelemetry metrics (OTLP) + node_exporter + kube-state-metrics |
| Loki | 로그 수집 | Promtail → application logs |
| Grafana | 시각화 (선택, DevForge 자체 UI 사용) | Tempo/Prometheus/Loki 데이터소스 |
| Mimir | 장기 메트릭 저장 (선택) | Prometheus remote write |

**Service 구현체 교체**

| Phase 1 (Mock) | Phase 2 (실제) |
|---|---|
| SystemHealthServiceStub | SystemHealthServiceDefault (API health check) |
| APMMetricsServiceStub | APMMetricsServiceDefault (Tempo/Prometheus/Loki 쿼리) |

**API 연동 예시**

```java
// APMMetricsServiceDefault.java (Phase 2)
public TransactionScatterData getTransactionScatter(Long systemId) {
    // Tempo Query API: GET /api/search?q={service.name="payment-api"}
    List<Trace> traces = tempoClient.queryTraces(systemId, Duration.ofMinutes(5));
    return traces.stream()
        .map(trace -> new TransactionPoint(
            trace.getTimestamp(),
            trace.getDuration(),
            trace.getStatus() == "error" ? "red" : trace.getDuration() > 1000 ? "yellow" : "green"
        ))
        .collect(Collectors.toList());
}

public REDMetrics getREDMetrics(Long systemId) {
    // Prometheus Query API: POST /api/v1/query
    String rateQuery = "rate(http_requests_total{system=\"" + systemId + "\"}[1m])";
    String errorQuery = "rate(http_errors_total{system=\"" + systemId + "\"}[1m]) / rate(http_requests_total{system=\"" + systemId + "\"}[1m])";
    String durationQuery = "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{system=\"" + systemId + "\"}[5m]))";

    return new REDMetrics(
        prometheusClient.query(rateQuery).getValue(),
        prometheusClient.query(errorQuery).getValue(),
        prometheusClient.query(durationQuery).getValue()
    );
}
```

---

#### 역할별 접근

| 역할 | 전체 시스템 뷰 | 개별 시스템 뷰 |
|---|---|---|
| Admin / SA | 모든 시스템 | 모든 시스템 |
| Domain Leader | 소속 시스템만 | 소속 시스템만 |
| Developer | 소속 시스템만 | 소속 시스템만 |

---

### F-11. 알림 연동

| 이벤트 | Slack | 이메일 |
|---|---|---|
| 빌드 실패 | ✅ | ✅ |
| 배포 완료 | ✅ | - |
| 배포 실패 | ✅ | ✅ |
| prd 승인 요청 | ✅ | ✅ |
| prd 배포 완료 | ✅ | ✅ |
| 롤백 실행 | ✅ | ✅ |
| feature 브랜치 수명 초과 | ✅ | - |

---

### F-12. 관리 설정 페이지 (`/admin/settings`) ★ REDESIGNED

**2개 탭**: 플랫폼 설정 + 연동 관리. 시스템별 설정은 시스템 관리(F-03)에서 처리.

#### 플랫폼 설정 탭

| 섹션 | 항목 | 수정 | 추가 | 비고 |
|---|---|---|---|---|
| 인증 방식 | OAuth2, SAML, LDAP/AD | ✅ | ✅ | Local은 항상 유지. 사후 추가 가능 |
| 플랫폼 알림 | Slack 채널, Email 수신자 | ✅ | ✅ | 플랫폼 이벤트(사용자 등록, 시스템 생성, 보안) 알림 |
| 브랜치 정책 | feature 수명 제한, 경고 시점 | ✅ | - | 기본 5일/3일. 0 = 제한 없음 |

#### 연동 관리 탭

외부 시스템 연결을 인스턴스 단위로 등록/수정/삭제한다. 시스템 생성 Wizard에서 여기 등록된 연동을 선택한다.

| 연동 유형 | 등록 | 수정 | 삭제 | 비고 |
|---|---|---|---|---|
| SCM (GitLab / GitHub) | ✅ | ✅ (URL, Token) | ⚠️ 참조 없을 때만 | 연결 테스트 포함, 복수 인스턴스 |
| CI (Jenkins) | ✅ | ✅ (URL, Token) | ⚠️ 참조 없을 때만 | GitLab CI/GitHub Actions는 SCM 내장 |
| Registry (Harbor / Nexus) | ✅ | ✅ (URL, 인증) | ⚠️ 참조 없을 때만 | 연결 테스트 포함 |
| Cluster (K8s) | ✅ | ✅ (kubeconfig, ArgoCD URL) | ⚠️ 참조 없을 때만 | K8s 전용 (v2.10~) |
| SonarQube | ✅ | ✅ (URL, Token) | ⚠️ 참조 없을 때만 | 연결 테스트 포함 |
| Notification (Slack / SMTP) | ✅ | ✅ (Token, 인증) | ⚠️ 참조 없을 때만 | 테스트 발송 포함 |

> **참조 보호**: 프로젝트가 있는 시스템이 참조하는 연동은 삭제 불가. 참조 시스템 목록을 삭제 시도 시 표시.

**UI 구성:**
- 연동 유형별 카드 또는 리스트 형태
- 각 연동에 "참조 시스템 N개" 뱃지 표시
- 등록/수정 시 Jasypt AES-256 암호화 + 연결 테스트 자동 실행
- 변경 이력 감사 로그 기록

**인증 방식 사후 추가 시:**
1. **OAuth2/SAML**: IdP 정보 입력 (Authorization URL, Client ID/Secret 등)
2. **LDAP/AD**: 서버 URL, Base DN, 검색 필터, Bind 계정 입력 + 연결 테스트
3. JIT Provisioning 활성화 여부 선택
4. 기존 Local 사용자의 SSO/LDAP 계정 매핑은 이메일 기준 자동 연결
5. LDAP 그룹 → DevForge 역할 매핑 설정 (선택, Admin이 그룹-역할 매핑 테이블 구성)

---

### F-13. 플랫폼 초기화 (Admin 전용) ★ NEW

불변 항목을 잘못 설정했거나, 플랫폼을 처음부터 재구성해야 할 때 사용하는 **최후 수단**.

**접근 경로:** `/admin/settings` → 하단 "위험 영역" → 플랫폼 초기화

**3단계 확인 절차:**
1. Admin 비밀번호 재입력
2. "플랫폼 초기화" 문구 직접 타이핑
3. 최종 확인 모달 (삭제 대상 목록 표시)

**초기화 범위:**

| 대상 | 동작 | 선택 |
|---|---|---|
| DevForge 내부 데이터 | 시스템·도메인·프로젝트·배포이력·승인요청 전체 삭제 | **필수** (항상 삭제) |
| 시스템 설정 | 모든 시스템 + 시스템별 연동 선택 초기화 | **필수** |
| 연동 등록 정보 | 등록된 SCM/CI/Registry/Cluster/SonarQube/Notification 연동 삭제 | ☐ 선택 (기본: 유지) |
| 외부 SCM (GitLab 레포) | 생성된 소스/매니페스트 레포 삭제 | ☐ 선택 (기본: 미삭제) |
| 외부 CI (Jenkins Job) | 생성된 빌드 잡 삭제 | ☐ 선택 (기본: 미삭제) |
| 외부 CD (ArgoCD App) | 생성된 앱 삭제 | ☐ 선택 (기본: 미삭제) |
| 사용자 계정 | Admin 외 모든 사용자 삭제 | ☐ 선택 (기본: 미삭제) |

**초기화 후 상태:**
- DB 테이블 초기화 (DDL 유지, 데이터만 삭제)
- Admin 계정 1개 유지 (비밀번호 변경 없음)
- 연동 등록 유지 선택 시 기존 외부 연동 정보 보존 (시스템만 삭제)
- 브라우저에서 대시보드로 리다이렉트
- 감사 로그에 초기화 실행 이력 기록 (초기화 전 마지막 기록)

---

## 10. 비기능 요구사항

| 항목 | 요구사항 |
|---|---|
| 보안 (설정 파일) | Jasypt AES-256 — `application.yml` 프로퍼티 암호화 |
| 보안 (DB 컬럼) | `JPA AttributeConverter` AES-256 — Secret 값 컬럼 암호화 |
| Webhook 보안 | GitLab: `X-Gitlab-Token` / GitHub: `X-Hub-Signature-256` HMAC / DevForge: `X-DevForge-Token` HMAC |
| 역할 기반 접근 | Admin / SA / Domain Leader / Developer 4단 역할, sit/prd 승인은 Domain Leader 이상 |
| 응답 속도 | 프로젝트 생성 API 60초 이내 |
| 실시간 UI | 상태 5~15초 갱신, 배포 로그 SSE 스트리밍 |
| 감사 로그 | 배포, 설정변경, 승인, **플랫폼 초기화** 주요 액션 DB 기록 |
| 브라우저 | Chrome / Edge 최신 버전 |
| 최소 사양 | CPU 4core, RAM 8GB, Disk 50GB |

---

## 11. 기술 스택

| 레이어 | 기술 | 비고 |
|---|---|---|
| Backend | Spring Boot 3.5.x, JDK 21 | 3.x 마지막 minor, Virtual Threads 활용, Temurin 기준 ~2029 지원 |
| Frontend | Thymeleaf + Tailwind CSS 4 + DaisyUI 5 | 서버사이드 렌더링, DaisyUI 시맨틱 테마 (dracula 기본 ↔ cmyk 토글), CDN 3줄 로딩 (커스텀 색상 없음) |
| 실시간 UI | HTMX + SSE (`SseEmitter`) | WebFlux 불필요 |
| API 문서화 | SpringDoc OpenAPI 2.7.0 | Swagger UI (`/swagger-ui.html`), OpenAPI JSON (`/v3/api-docs`) |
| DB | H2 (개발) / PostgreSQL (운영) | |
| 인증 | Spring Security + OAuth2 / SAML2 | |
| SCM (GitLab) | GitLab REST API v4 | 시스템별 ScmConnection 선택 |
| SCM (GitHub) | GitHub REST API v3 (octokit) | 시스템별 ScmConnection 선택 |
| CI (Jenkins) | jenkins-client 라이브러리 | XML Config POST |
| CI (GitLab CI) | GitLab API | `.gitlab-ci.yml` 커밋 |
| CI (GitHub Actions) | GitHub API | `.github/workflows/*.yml` 커밋 |
| CD | ArgoCD REST API + 매니페스트 레포 git push | K8s GitOps Pull 방식 (v2.10~ 단일 구현) |
| 로그 스트리밍 | K8s Java Client `CoreV1Api.readNamespacedPodLog` | |
| K8s 연동 | Kubernetes Java Client (멀티 kubeconfig) | |
| 암호화 (설정) | Jasypt AES-256 | yml 프로퍼티 |
| 암호화 (DB) | JPA `AttributeConverter` AES-256 | Secret 컬럼 |
| HTTP 클라이언트 | Spring WebClient | |
| 알림 | Slack Webhook + Spring Mail | |
| 설치 자동화 | Shell Script + Helm + k3s | |

### 11.1 아키텍처 패턴

**Factory 패턴** — 런타임 분기
- ScmServiceFactory, CiServiceFactory, ClusterCdFactory, RegistryServiceFactory, NotificationServiceFactory
- 시스템별 설정(scmType, ciEngine 등)에 따라 적절한 구현체 선택 (ClusterCdFactory는 v2.10~ K8s 단일 구현의 얇은 래퍼)
- 모든 구현체가 동시에 Spring Bean으로 등록되며, Factory가 런타임에 선택

**Stub/Default 분리** — 프로필 기반 Mock
- `@Profile("!test")`: Default 구현체 → 실제 외부 API 호출
- `@Profile("test")`: Stub 구현체 → Mock 데이터 반환, 외부 의존성 없음
- 총 17개 서비스에 Stub/Default 쌍 적용 (Phase 6 대시보드 서비스 포함)

**Saga 패턴** — 분산 트랜잭션 롤백
- ProjectOrchestratorService: Git → CI → CD 순차 생성
- 중간 실패 시 역순 보상 작업 (CD 삭제 → CI 삭제 → Git 삭제)
- SagaExecutor, SagaStep, SagaContext 구조

**CQRS 패턴** — Command/Query 분리 (부분 적용)
- **Query Service**: 읽기 전용, 복잡한 조회 로직 분리
  - ProjectQueryService: 프로젝트 조회 (검색, 필터링, 페이징)
  - DeployQueryService: 배포 이력 조회 (통계, 최근 배포, 승인 대기)
  - UserActivityQueryService: 사용자 활동 조회 (로그인 이력, 배포 통계)
- **Command Service**: 쓰기 작업, 상태 변경
  - ProjectOrchestratorService: 프로젝트 생성 (Saga)
  - ProjectDeployService: 프로모션, 롤백
  - ImagePromotionService: 승인 워크플로우
- **혼용**: SystemService, DomainService, ClusterService 등은 CRUD 통합 (단순 도메인)

---

## 12. 프로젝트 디렉토리 구조

```
devforge/
├── src/main/java/com/devforge/
│   ├── config/
│   │   ├── DevForgeProperties.java
│   │   ├── SecurityConfig.java
│   │   ├── WebClientConfig.java
│   │   ├── JasyptConfig.java                # Jasypt AES-256 암호화 설정
│   │   ├── OpenApiConfig.java               # SpringDoc OpenAPI 2.7.0 설정
│   │   ├── ClusterClientRegistry.java       # K8s ApiClient 풀 관리
│   │   └── SystemScopeAdvice.java           # 시스템 스코프 ControllerAdvice (세션 기반 현재 시스템)
│   │
│   ├── controller/
│   │   ├── SystemWizardController.java       # 시스템 생성 Wizard Step 1~9
│   │   ├── DomainController.java
│   │   ├── ProjectController.java
│   │   ├── DeployController.java            # 프로모션, 롤백, 승인
│   │   ├── DeployLogController.java         # SSE (K8s Pod Logs)
│   │   ├── DashboardController.java
│   │   ├── ConfigController.java
│   │   ├── AdminController.java
│   │   ├── AdminSettingsController.java     # 플랫폼 설정 + 초기화 (Admin 전용)
│   │   ├── SystemController.java            # 시스템 관리 (Admin/SA)
│   │   ├── LoginController.java             # 로그인 페이지
│   │   ├── SidebarController.java           # HTMX 사이드바 fragment
│   │   ├── CiController.java                # CI 빌드 관리 + 빌드 트리거
│   │   ├── CiCdController.java              # CI/CD 현황, 프로젝트별 파이프라인(/cicd/pipeline/{id}), 승인 관리, 배포 이력
│   │   ├── BranchController.java            # 브랜치 관리 UI (생성, 보호, 삭제)
│   │   ├── BuildProgressController.java     # 빌드 진행 현황 SSE 스트리밍
│   │   ├── DomainBrowseController.java      # 조직 구조 브라우저 (읽기 전용)
│   │   ├── SystemSwitchController.java      # 시스템 스코프 전환 (세션)
│   │   ├── HtmxHelper.java                  # HTMX 헤더/속성 유틸리티
│   │   ├── CustomErrorController.java       # 에러 페이지 핸들링
│   │   └── WebhookController.java           # CI 콜백 + GitLab/GitHub 이벤트
│   │
│   ├── adapter/                             # ★ 외부 시스템 API 래퍼 (비즈니스 로직 없음)
│   │   ├── GitLabAdapter.java               # GitLab REST API v4 (WebClient + PRIVATE-TOKEN)
│   │   ├── GitHubAdapter.java               # GitHub REST API v3 (WebClient + Bearer Token)
│   │   ├── JenkinsAdapter.java              # Jenkins REST API (WebClient + Basic Auth)
│   │   ├── ArgoCDAdapter.java               # ArgoCD REST API (WebClient + Bearer Token)
│   │   ├── KubectlAdapter.java              # kubectl CLI 래퍼 (ProcessBuilder)
│   │   └── SlackAdapter.java                # Slack Incoming Webhook (WebClient)
│   │
│   ├── service/                             # ★ 인터페이스 + 팩토리만 (서브패키지 없음)
│   │   ├── ScmService.java                  # 소스+매니페스트 레포 관리
│   │   ├── CiService.java                   # CI 빌드 잡 관리
│   │   ├── CdService.java                   # CD 배포 관리
│   │   ├── RegistryService.java             # 컨테이너 레지스트리 관리 (이미지 조회, 태그 검증)
│   │   ├── NamespaceService.java            # K8s Namespace 관리
│   │   ├── SecretService.java               # K8s Secret 관리
│   │   ├── K8sLogService.java               # K8s Pod 로그 스트리밍
│   │   ├── NotificationService.java         # 알림 (Slack/Email)
│   │   ├── BuildEventService.java           # 빌드 완료 → DeployHistory
│   │   ├── WebhookAuthService.java          # Webhook HMAC 검증
│   │   ├── SystemService.java               # 시스템 CRUD
│   │   ├── DomainService.java               # 도메인 CRUD
│   │   ├── ClusterService.java              # 클러스터 CRUD
│   │   ├── UserService.java                 # 사용자 CRUD
│   │   ├── ImagePromotionService.java       # 프로모션 + 승인 + 순서 검증
│   │   ├── ProjectOrchestratorService.java  # Saga 오케스트레이션
│   │   ├── ProjectDeployService.java        # 프로젝트 배포 Command 처리 (프로모션 실행, 롤백)
│   │   ├── ProjectQueryService.java         # 프로젝트 조회 Query 전용 (CQRS 패턴)
│   │   ├── DeployQueryService.java          # 배포 이력 조회 Query 전용 (CQRS 패턴)
│   │   ├── UserActivityQueryService.java    # 사용자 활동 조회 Query 전용
│   │   ├── ConfigEntryService.java          # Config/Secret CRUD
│   │   ├── PlatformSettingService.java      # 플랫폼 설정 CRUD
│   │   ├── ClusterMetricsService.java       # 클러스터 메트릭 조회 (CPU, Memory, Pod 수)
│   │   ├── ClusterMonitoringService.java    # 클러스터 모니터링 상태 관리
│   │   ├── BranchLifecycleService.java      # 브랜치 생명주기 관리 (자동 삭제, 보호 규칙)
│   │   ├── AuditLogService.java             # 감사 로그
│   │   ├── IntegrationService.java          # 연동 CRUD + 연결 테스트 + 참조 보호
│   │   ├── PlatformResetService.java        # 플랫폼 초기화 (선택적 삭제)
│   │   ├── APMMetricsService.java           # ★ 개별 시스템 APM 데이터 (scatter, RED, Top5)
│   │   ├── SystemHealthService.java         # ★ 전체 시스템 헬스 데이터
│   │   ├── PlatformHealthService.java       # ★ 플랫폼 연동 헬스 상태 (Zone 1)
│   │   ├── DoraMetricsService.java          # ★ DORA 메트릭 (배포 빈도, 리드 타임, MTTR)
│   │   ├── CICDMonitoringService.java       # ★ CI/CD 모니터링 (ArgoCD Sync, Pipeline, CICD 탭)
│   │   ├── ScmServiceFactory.java           # 시스템 SCM 연동 → ScmService 선택
│   │   ├── CiServiceFactory.java            # 시스템 CI 엔진 → CiService 선택
│   │   ├── ClusterCdFactory.java            # 환경별 K8s 클러스터 컨텍스트 제공 (v2.10~ K8s 단일)
│   │   ├── RegistryServiceFactory.java      # 레지스트리 유형 → RegistryService 선택 (Harbor/Nexus)
│   │   ├── NotificationServiceFactory.java  # 알림 유형 → NotificationService 선택 (Slack/Email)
│   │   └── PipelineTemplateFactory.java     # ci-engine 기반 템플릿 선택 (K8s 전용)
│   │
│   │   impl/                                # ★ 모든 구현체 (~56개, 동시 빈 등록)
│   │   ├── GitLabScmService.java            # Factory 선택 (scmType=gitlab) → GitLabAdapter
│   │   ├── GitHubScmService.java            # Factory 선택 (scmType=github)
│   │   ├── StubScmService.java              # @Profile("test") Mock SCM
│   │   ├── JenkinsCiService.java            # Factory 선택 (ciEngine=jenkins) → JenkinsAdapter
│   │   ├── GitLabCiService.java             # Factory 선택 (ciEngine=gitlab-ci) → GitLabAdapter
│   │   ├── GitHubActionsCiService.java      # Factory 선택 (ciEngine=github-actions)
│   │   ├── StubCiService.java               # @Profile("test") Mock CI
│   │   ├── ArgoCDService.java               # K8s 단일 CD 구현체 → ArgoCDAdapter
│   │   ├── StubCdService.java               # @Profile("test") Mock CD
│   │   ├── HarborRegistryService.java       # Factory 선택 (registryType=harbor)
│   │   ├── NexusRegistryService.java        # Factory 선택 (registryType=nexus)
│   │   ├── StubRegistryService.java         # @Profile("test") Mock Registry
│   │   ├── SlackNotificationService.java    # Factory 선택 (notificationType=slack)
│   │   ├── EmailNotificationService.java    # Factory 선택 (notificationType=email)
│   │   ├── StubNotificationService.java     # @Profile("test") Mock Notification
│   │   ├── DefaultNamespaceService.java     # @Profile("!test") → KubectlAdapter
│   │   ├── StubNamespaceService.java        # @Profile("test")
│   │   ├── DefaultSecretService.java        # @Profile("!test") → KubectlAdapter
│   │   ├── StubSecretService.java           # @Profile("test")
│   │   ├── DefaultK8sLogService.java        # @Profile("!test") → KubectlAdapter
│   │   ├── StubK8sLogService.java           # @Profile("test")
│   │   ├── DefaultSystemService.java        # @Profile("!test")
│   │   ├── StubSystemService.java           # @Profile("test")
│   │   ├── DefaultDomainService.java        # @Profile("!test")
│   │   ├── StubDomainService.java           # @Profile("test")
│   │   ├── DefaultClusterService.java       # @Profile("!test")
│   │   ├── StubClusterService.java          # @Profile("test")
│   │   ├── DefaultUserService.java          # @Profile("!test")
│   │   ├── StubUserService.java             # @Profile("test")
│   │   ├── DefaultImagePromotionService.java# @Profile("!test")
│   │   ├── StubImagePromotionService.java   # @Profile("test")
│   │   ├── DefaultProjectOrchestratorService.java # @Profile("!test")
│   │   ├── StubProjectOrchestratorService.java # @Profile("test")
│   │   ├── DefaultProjectDeployService.java # @Profile("!test")
│   │   ├── StubProjectDeployService.java    # @Profile("test")
│   │   ├── DefaultBuildEventService.java    # @Profile("!test")
│   │   ├── StubBuildEventService.java       # @Profile("test")
│   │   ├── DefaultWebhookAuthService.java   # @Profile("!test")
│   │   ├── StubWebhookAuthService.java      # @Profile("test")
│   │   ├── DefaultAuditLogService.java      # @Profile("!test")
│   │   ├── StubAPMMetricsService.java       # @Profile("test") ★ Phase 6 APM Mock
│   │   ├── StubCICDMonitoringService.java   # @Profile("test") ★ Phase 6 CI/CD Mock
│   │   ├── StubDoraMetricsService.java      # @Profile("test") ★ Phase 6 DORA Mock
│   │   └── StubPlatformHealthService.java   # @Profile("test") ★ Phase 6 연동 헬스 Mock
│   │
│   ├── orchestrator/saga/
│   │   ├── SagaStep.java                    # Saga 단일 작업 인터페이스
│   │   ├── SagaContext.java                 # Saga 실행 컨텍스트
│   │   ├── SagaExecutor.java               # Saga 코디네이터 (순방향 + 보상 롤백)
│   │   └── SagaException.java              # Saga 전용 예외
│   │
│   ├── domain/
│   │   ├── SystemEntity.java                # 시스템 (baseDomain 보유, SCM/CI/Registry/Cluster 선택)
│   │   ├── Domain.java                      # 도메인 (System 하위 비즈니스 도메인)
│   │   ├── Project.java                     # 프로젝트 (배포 가능 단위)
│   │   ├── Cluster.java                     # kubeconfig, ArgoCD URL (K8s 전용)
│   │   ├── DeployHistory.java               # imageTag, pipelineId 포함
│   │   ├── ApprovalRequest.java             # 프로모션 승인 요청
│   │   ├── User.java                        # globalRole 보유
│   │   ├── UserDomainRole.java              # 도메인 레벨 역할 할당 (N:1 User, N:1 Domain)
│   │   ├── UserActivity.java                # ★ 사용자 활동 추적 (로그인, 배포, 승인 등)
│   │   ├── ConfigEntry.java                 # 환경별 Config/Secret Key-Value
│   │   ├── PlatformSetting.java             # 변경가능 플랫폼 설정 (Admin 설정 페이지)
│   │   ├── AuditLog.java                    # 감사 로그 (변경 이력 추적)
│   │   ├── ScmConnection.java               # ★ SCM 연동 (GitLab/GitHub URL, Token)
│   │   ├── CiConnection.java                # ★ CI 연동 (Jenkins URL, Token — SCM 내장 CI 불필요)
│   │   ├── RegistryConnection.java          # ★ Registry 연동 (URL, 인증)
│   │   ├── SystemClusterAssignment.java     # ★ 시스템-환경-클러스터 매핑
│   │   ├── SonarQubeConnection.java         # ★ SonarQube 연동 (URL, Token)
│   │   ├── NotificationConnection.java      # ★ Notification 연동 (Slack/Email)
│   │   └── enums/
│   │       ├── Role.java                    # ADMIN, SA, DEVELOPER (글로벌 역할)
│   │       ├── DomainRole.java              # DOMAIN_LEADER, DEVELOPER (도메인 역할)
│   │       ├── Environment.java             # DEV, TEST, SIT, PRD
│   │       ├── EnvironmentPreset.java       # STANDARD_4, STANDARD_3
│   │       ├── ScmType.java                 # GITLAB, GITHUB
│   │       ├── CiEngine.java               # JENKINS, GITLAB_CI, GITHUB_ACTIONS
│   │       ├── DeployStatus.java            # PENDING, IN_PROGRESS, SUCCESS, FAILED, ROLLED_BACK
│   │       ├── ApprovalStatus.java          # PENDING, APPROVED, REJECTED, EXPIRED
│   │       ├── AuthProvider.java            # LOCAL, OAUTH2, SAML, LDAP
│   │       └── RegistryType.java            # ★ HARBOR, NEXUS (Registry 유형 구분)
│   │
│   ├── dto/                                 # ★ 데이터 전송 객체 (Phase 6 대시보드)
│   │   ├── APMDashboardDto.java             # APM 대시보드 전체 데이터
│   │   ├── APMDetailDto.java                # APM 탭 상세 데이터 (REDMetrics, scatter, top5)
│   │   ├── ArgocdSyncOverviewDto.java       # ArgoCD Sync 상태 요약
│   │   ├── BranchDto.java                   # 브랜치 정보
│   │   ├── CICDMonitoringDto.java           # CI/CD 탭 데이터 (파이프라인, 승인, 프로모션)
│   │   ├── CIPipelineOverviewDto.java       # CI 파이프라인 요약
│   │   ├── ClusterHealthDto.java            # 클러스터 헬스 상태
│   │   ├── ClusterMetricsDto.java           # 클러스터 메트릭 (CPU, Memory, Pod)
│   │   ├── DoraMetricsDto.java              # DORA 메트릭 (배포빈도, 리드타임, MTTR, 변경실패율)
│   │   ├── PlatformConnectionStatusDto.java # 플랫폼 연동 헬스 상태
│   │   ├── SystemHealthCardDto.java         # 시스템 카드 데이터
│   │   └── SystemHealthSummaryDto.java      # 전체 시스템 헬스 요약
│   │
│   ├── repository/
│   │   ├── SystemRepository.java
│   │   ├── DomainRepository.java
│   │   ├── ProjectRepository.java
│   │   ├── ClusterRepository.java
│   │   ├── DeployHistoryRepository.java
│   │   ├── ApprovalRequestRepository.java
│   │   ├── UserRepository.java
│   │   ├── UserDomainRoleRepository.java
│   │   ├── UserActivityRepository.java      # ★ 사용자 활동 조회 (최근 활동, 통계)
│   │   ├── ConfigEntryRepository.java
│   │   ├── PlatformSettingRepository.java
│   │   ├── AuditLogRepository.java
│   │   ├── ScmConnectionRepository.java
│   │   ├── CiConnectionRepository.java
│   │   ├── RegistryConnectionRepository.java
│   │   ├── SystemClusterAssignmentRepository.java
│   │   ├── SonarQubeConnectionRepository.java
│   │   └── NotificationConnectionRepository.java
│   │
│   └── security/
│       ├── AesEncryptConverter.java
│       └── CustomUserDetailsService.java    # Spring Security UserDetailsService 구현
│
├── src/main/resources/
│   ├── pipeline-templates/                  # CI 파이프라인 템플릿 (K8s 전용)
│   │   ├── java-jenkins-k8s.Jenkinsfile
│   │   ├── java-gitlab-k8s.gitlab-ci.yml
│   │   └── java-github-k8s.yml             # GitHub Actions + K8s
│   ├── mock-data/                           # Stub 서비스 Mock 데이터 (인메모리 생성)
│   ├── static/css/devforge.css              # 커스텀 레이아웃 스타일 (DaisyUI 테마 색상 오버라이드 없음)
│   ├── static/js/devforge.js                # ★ 클라이언트 JS (Canvas scatter 차트, SSE 핸들링)
│   ├── application.yml                      # 기본 설정 (test 프로필 기본)
│   ├── application-test.yml                 # Stub 구현체 활성화
│   ├── application-prod.yml                 # PostgreSQL + 실 API 연동
│   └── data.sql                             # 초기 시드 데이터
│
└── install/                                 # (Phase 7 — 미구현)
    ├── install.sh
    ├── scripts/
    │   ├── check-requirements.sh
    │   ├── install-k3s.sh
    │   ├── install-helm.sh
    │   ├── install-gitlab.sh
    │   ├── install-jenkins.sh
    │   └── install-argocd.sh
    └── helm/
        ├── gitlab-values.yaml
        ├── jenkins-values.yaml
        └── argocd-values.yaml
```

---

## 13. application.yml 전체 설계

외부 연동 정보(SCM, CI, Registry, Cluster, SonarQube, Notification)는 DB에 저장하며 Admin이 관리 설정 UI에서 CRUD한다.
시스템별 설정(SCM 선택, CI 엔진, 환경 프리셋, 클러스터 배정 등)은 `SystemEntity`에 저장하며 시스템 생성 Wizard에서 설정한다.
`application.yml`에는 인프라 설정과 플랫폼 정책만 남긴다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devforge
    username: devforge
    password: ENC(암호화된값)
  jpa:
    hibernate:
      ddl-auto: validate

devforge:
  # ──────────────────────────────────────────────────────────
  # DB 관리 항목 (application.yml에 작성하지 않음)
  # ──────────────────────────────────────────────────────────
  # 아래 항목들은 Admin이 관리 설정 UI(`/admin/settings`)에서 등록·관리한다.
  # - SCM 연동 (GitLab/GitHub URL, Token) → ScmConnection 엔티티
  # - CI 연동 (Jenkins URL, Token) → CiConnection 엔티티
  # - Registry 연동 (URL, 인증) → RegistryConnection 엔티티
  # - Cluster 연동 (kubeconfig + ArgoCD URL, K8s 전용) → Cluster 엔티티
  # - SonarQube 연동 (URL, Token) → SonarQubeConnection 엔티티
  # - Notification 연동 (Slack/Email) → NotificationConnection 엔티티
  #
  # 시스템별 설정은 시스템 생성 Wizard에서 설정한다.
  # - SCM 선택, CI 엔진, Registry, 환경 프리셋, 클러스터 배정 → SystemEntity
  # ──────────────────────────────────────────────────────────

  # 인증 (혼용 지원, Admin이 관리 설정에서 사후 추가 가능) ──
  auth:
    local:
      enabled: true              # 항상 true (비활성화 불가, Admin 폴백)
    oauth2:
      enabled: false             # true 시 OAuth2 로그인 활성화
      provider: keycloak         # keycloak | google | github | custom
      client-id: ENC(암호화된값)
      client-secret: ENC(암호화된값)
      issuer-uri: https://sso.company.com/realms/devforge
    saml:
      enabled: false             # true 시 SAML 로그인 활성화
      entity-id: devforge
      metadata-url: https://idp.company.com/metadata
    ldap:
      enabled: false             # true 시 LDAP/AD 로그인 활성화
      url: ldap://ldap.company.com:389
      base-dn: dc=company,dc=com
      user-search-filter: (uid={0})
      bind-dn: cn=admin,dc=company,dc=com
      bind-password: ENC(암호화된값)
    jit-provisioning: true       # SSO/LDAP 첫 로그인 시 자동 등록
    default-role: NONE           # JIT 등록 시 기본 역할

  # Webhook 보안 ────────────────────────────────────────────
  webhook:
    secret: ENC(암호화된값)     # DevForge Webhook HMAC 키

  # 브랜치 정책 (Admin 설정에서 변경 가능) ──────────────────
  branch-policy:
    feature-max-days: 5            # feature 브랜치 최대 수명 (0 = 제한 없음, warn도 비활성화)
    feature-warn-days: 3           # 경고 시작 시점 (max-days 이전)

  # Ingress URL 패턴 (플랫폼 공통, baseDomain은 시스템별) ──
  ingress:
    nonprd-format: "{env}.{domain}.{project}.{base-domain}"
    prd-format: "{domain}.{project}.{base-domain}"
    allow-custom: true

  # UI polling 간격 (초) ────────────────────────────────────
  ui:
    polling:
      build-status-sec: 5
      project-card-sec: 10
      cluster-health-sec: 15

  # DB 컬럼 암호화 (JPA AttributeConverter) ────────────────
  encryption:
    aes-key: ${AES_ENCRYPTION_KEY}

jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD}
```

---

## 14. 개발 단계 Milestone

### Phase 0 — 프로젝트 초기 세팅 (1~2일)
- [ ] 전체 디렉토리 구조 생성
- [ ] `DevForgeProperties` 바인딩 (SCM, CI, 클러스터 config)
- [ ] Spring Security 기본 구조
- [ ] Thymeleaf 공통 레이아웃 (HTMX CDN)
- [ ] Jasypt + AES Converter + PostgreSQL

### Phase 1a — 외부 도구 연동 (인터페이스 + 대표 구현체) (2~3주)
- [ ] 전체 서비스 인터페이스 + Stub 구현체 (test 프로필용)
- [ ] `ClusterClientRegistry` (K8s ApiClient 풀)
- [ ] `GitLabScmService` (소스 + 매니페스트 레포 관리) — 대표 SCM 구현체
- [ ] `GitLabCiService` — 대표 CI 구현체
- [ ] `ArgoCDService` (매니페스트 레포 tag 업데이트 + ArgoCD App 등록) — 대표 CD 구현체
- [ ] `ClusterCdFactory`
- [ ] `K8sLogService`

### Phase 1b — 나머지 구현체 (2~3주)
- [ ] `GitHubScmService` (소스 + 매니페스트 레포 관리)
- [ ] `JenkinsCiService` + `GitHubActionsCiService`

### Phase 2 — 시스템 생성 Wizard + 관리 설정 + Webhook (2~3주)
- [ ] 연동 관리 UI (SCM/CI/Registry/Cluster/SonarQube/Notification CRUD)
- [ ] 시스템 생성 Wizard Step 1~9 화면 (등록된 연동에서 선택)
- [ ] Grace Period 로직 (시스템 내 프로젝트 0개 → 재설정 가능)
- [ ] `/admin/settings` — 플랫폼 설정 + 연동 관리 (F-12)
- [ ] 플랫폼 초기화 기능 (F-13, Admin 전용 3단계 확인)
- [ ] `WebhookController` (GitLab / GitHub / DevForge 콜백 분리)
- [ ] `BuildEventService` + `DeployHistory` 저장

### Phase 3 — 도메인 + 프로젝트 관리 (2~3주)
- [ ] 프로젝트 관리 화면 (Master-Detail + System > Domain > [Group] > Project 트리 뷰)
- [ ] `PipelineTemplateFactory` (3가지 CI × K8s, DevForge 콜백 단계 + SonarQube 조건부 포함)
- [ ] `ProjectCreationSaga` (소스 레포 + 매니페스트 레포 + CI + CD Saga)

### Phase 4 — 이미지 프로모션 + 승인 + 알림 (1~2주)
- [ ] 프로모션 버튼 (매니페스트 태그 업데이트 → ArgoCD 자동 감지)
- [ ] prd 승인 요청 + Admin 승인 화면
- [ ] 알림 연동
- [ ] 롤백

### Phase 5 — Config / Secret 관리 (1주)
- [ ] 환경별 설정값 UI
- [ ] K8s Secret 주입 + ArgoCD ignoreDifferences
- [ ] AES 암호화 저장

### Phase 6 — APM 대시보드 + K8s 현황 (2~3주) ★ COMPLETED (v2.8)

**Phase 6-1: APM Dashboard MVP (Stub 데이터)** ✅ 완료
- [x] SystemHealthService, APMMetricsService, PlatformHealthService, DoraMetricsService, CICDMonitoringService (Stub) — 대시보드 서비스 5종
- [x] DashboardController 3-Zone 전체뷰 + 2-탭(APM/CI·CD) 개별뷰 + SSE 스트리밍
- [x] DTO 12종 (APMDashboardDto, APMDetailDto, CICDMonitoringDto, DoraMetricsDto 등)
- [x] Thymeleaf 템플릿 (overview.html, detail.html, fragments/dashboard/* 25+ 파일)
- [x] HTMX polling (5s~60s 주기) + Canvas SSE scatter 차트 (1s 실시간)
- [x] DaisyUI v5 반응형 레이아웃 (B-MON 스타일 APM 탭 + CI/CD 탭)

**Phase 6-2: SSE 로그 + 클러스터 헬스** ✅ 완료
- [x] SSE 로그 스트리밍 (K8s Pod Logs)
- [x] 클러스터 헬스체크 (K8s API)

**Phase 6-3: LGTM 연동 (향후, Phase 2)** — 별도 작업
- [ ] Common Framework OpenTelemetry SDK 추가
- [ ] LGTM 스택 설치 (Tempo, Prometheus, Loki, Grafana)
- [ ] SystemHealthServiceDefault, APMMetricsServiceDefault 실제 구현
- [ ] Factory 라우팅 전환 (Stub → Default)

### Phase 7 — install.sh (1~2주)
- [ ] k3s + GitLab rollout 대기 + Jenkins + ArgoCD 설치
- [ ] application.yml 자동 생성

> **총 예상 기간:** MVP 기준 약 **16~20주** (1인 개발)

---

## 15. 리스크 및 대응 방안

| 리스크 | 원인 | 대응 방안 |
|---|---|---|
| SCM 이중 지원 복잡도 | GitLab/GitHub API 차이 | `ScmService` 인터페이스로 추상화, 구현체 분리 |
| 매니페스트 레포 git 충돌 | 동시 프로모션 시 push 충돌 | `ImagePromotionService`에서 git pull --rebase 후 retry (최대 3회, exponential backoff 1s→2s→4s, jitter ±500ms). 3회 실패 시 DeployStatus.FAILED + UI 토스트 + Slack 알림 |
| ArgoCD OutOfSync | DevForge Secret 직접 적용 | ignoreDifferences 자동 설정 |
| GitHub Actions SCM 제약 | GitLab CI를 GitHub 레포에 사용 불가 | 시스템 생성 Wizard SCM 선택 시 CI 자동 필터링 |
| GitLab 설치 지연 | Helm chart 초기화 20분+ | rollout status 대기 루프 |
| SSE 연결 누수 | 완료 후 emitter 미종료 | `@Scheduled` emitter pool 정리 |
| Saga 보상 실패 | 보상 중 네트워크 오류 | 감사 로그 기록 + Admin 수동 정리 가이드 |
| Token 보안 | 설정파일 유출 | Jasypt + 환경변수 마스터키 |

---

## 16. 향후 확장 방향

| 버전 | 기능 |
|---|---|
| v1.5 | 설정 Export/Import (재설치 시 devforge-config.json으로 설정 복원) |
| v1.5 | SonarQube Quality Gate 대시보드 연동 |
| v1.5 | Gitea 지원 |
| v1.5 | 설정 부분 리셋 + 프로젝트 재연결 (불변 항목 개별 변경 without 전체 초기화) |
| v1.5 | Bitbucket SCM 지원 |
| v2.0 | Sealed Secrets / External Secrets Operator |
| v2.0 | Prometheus + Grafana 모니터링 연동 |
| v2.0 | ELK 로깅 연동 |
| v2.0 | 리소스 쿼터 관리 |
| v2.5 | API Gateway 연동 (Kong / Nginx Ingress) |
| v3.0 | 파이프라인 템플릿 커스터마이징 UI |
| v3.0 | 비용 대시보드 |
| v3.0 | 모노레포 지원 |
| v3.0 | Terraform Provider 연동 (인프라 프로비저닝 자동화) |

---

*DevForge Platform PRD v2.10 — 2026-04-18 (갱신: 2026-04-19 — Task 2.5/4a/4b + CI/CD 탭 설계 확정 반영)*
*주요 변경: VM/SSH 배포 지원 제거 (K8s + ArgoCD GitOps 단일 구현으로 확정), 과거 버전 변경 기록은 히스토리 보존*
