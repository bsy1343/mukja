# PRD: mukja — 사내 주문 취합 웹앱

**Version**: 3.0 (vendor 레이어 · 음식점 확장 · 영구 운영 기능)
**Project**: mukja
**Tech Stack**: JDK 21 · Spring Boot 3.5.3 · Thymeleaf SSR · HTMX 2 · 손수 쓴 CSS(+ 동결 Tailwind/DaisyUI 산출물) · SSE · JSON 파일 저장(DB 없음)

> 이 PRD는 구현된 시스템의 단일 기준이다. 설계 결정의 배경은
> `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md`(보드 모델),
> `docs/superpowers/specs/2026-05-26-mukja-single-board-layout-redesign.md`(단일 보드 UI),
> 구현 가이드는 루트 `CLAUDE.md`를 참고한다.
> **v1(단일 OPEN 세션)·v2(카테고리×팀 평면)는 폐기**하고 **(가게 × 팀)** 보드 모델로 진화했다.

---

## 1. 프로젝트 개요

### 1.1 목적
사내에서 커피·점심을 함께 주문할 때, 당번 한 명이 취합하는 과정을 돕는다. 각자 자기 메뉴를 담고, 당번이 집계 화면에서 요약을 복사해 발주(또는 식당에 직접 전화)하며, 음식이 나오면 사람별 리스트로 나눠준다.

### 1.2 배경
- 기존: 메신저로 "뭐 마실래?"를 일일이 받아 손으로 취합 — 누락·오타·재확인이 잦다.
- 해결: 링크 하나로 각자 셀프 주문 → 자동 집계·금액 계산 → 복사용 발주 요약 + 식당 직통 전화 버튼.

### 1.3 범위
- **포함**: 가게(커피 1, 식당 8) × 팀(4) 보드, 데이터 주도 옵션·가격, 팀 명단·미주문자 감지, 마감 카운트다운(임박 주황·종료 빨강), 매일 자정 자동 초기화, 메뉴별/사람별 집계, 요약 복사·식당 전화하기, 보드별 SSE 실시간, 관리자 메뉴 조회(PIN), 링크 미리보기(OG)·파비콘·CDN 캐시 자동 무효화.
- **제외**: 주문 이력/통계 보관, 마감·초기화 권한 제어, 관리자 인라인 메뉴 편집, Jasypt PIN 암호화, Cloudflare Access.

### 1.4 대상 사용자
- **주문자**: 팀 명단에서 자기 이름 선택(없으면 "기타" 직접 입력) → 메뉴 담아 제출. 1인 1메뉴(재담기는 교체).
- **당번**: 마감 시각 설정/해제·수동 초기화·집계 발주·전화. PIN 없음(팀 내부 신뢰 전제).
- **관리자**: `/admin` PIN으로 메뉴 조회.

### 1.5 운영 환경
- 사내 내부망, Self-hosted 러너 + GitHub Actions 배포, 서브도메인 `mukja.sybaek.dev`(Cloudflare + Nginx Proxy Manager).
- 모바일 우선(360~480px). **PC도 폰처럼** — 480px 가운데 컬럼에 콘텐츠·하단시트·드로어·CTA 모두 정렬.

---

## 2. 핵심 개념 — 주문판(Board)

**주문판 = (가게 vendor × 팀 team)** 조합. 보드마다 독립 저장·독립 락. 상시 존재.
- **상위 그룹**: `coffee` / `food` (URL의 `{category}` 세그먼트, 정규식 `{coffee|food}`).
- **가게**: `coffee` 그룹 = KT그룹희망나눔재단 / `food` 그룹 = 고향집삼계탕·두향·란반·밥상머리·예돈·라이라이·푸른바다볼테기·등촌샤브샤브.
- **팀**: ICE · KOS · ICIS · 비발디 — `application.yml`에서 관리. 팀마다 명단(`teams.json`)을 따로 둔다.
- **흐름**: 가게 드로어(☰) → 팀 알약 → 주문자 알약(가나다순) → 메뉴 카드 → 옵션 모달 → 담기 → 주문하기 → 집계.
- 세션/날짜별 보관 없음. **매일 00:00 KST 자동 초기화**(주문·마감 모두 비움) + 수동 초기화 병행.

---

## 3. 데이터 모델 (JSON, `JsonStore` 경유)

### 3.1 menus.json (시드 `menus.seed.json` 최초 복사)
- `optionDefs` (temp/ice/light/shot/meal): `single`(required면 미선택 시 담기 불가) · `toggle` · `counter`. `fixedTemp` 메뉴는 temp UI 숨김·가산 무시.
- `vendors[]`: `{id, name, group, floor, phone?, categories[]}`. **`phone`은 식당만** — 집계 화면 "📞 전화하기" 버튼(`tel:`) 노출.
- `categories[].items[].options[]`로 어느 옵션 묶음을 쓸지 가게/메뉴별 데이터 주도 결정.
- `PriceCalculator`/`OptionTextBuilder`가 optionDefs로 가격·옵션텍스트 계산. 라인 옵션이 비면 집계 분해에서 제외(괄호 자체 생략).

### 3.2 teams.json (시드 `teams.seed.json`)
```json
{ "members": { "ice": ["김자영", "남효우", ...], "kos": [...], "icis": [...], "vivaldi": [...] } }
```
- 명단은 렌더 시 **가나다 오름차순** 정렬. 미주문자(`agg.missing`)도 동일 정렬.

### 3.3 보드 파일 `data/orders/{vendor}-{team}.json`
```json
{ "closeAt": "2026-05-28T14:30:00+09:00",
  "orders": [ { "person": "백상열", "submittedAt": "...",
    "lines": [ { "itemId":102, "name":"아메리카노", "unitPrice":1600,
      "options":{"temp":"ice","shot":1}, "optionText":"ICE·샷+1", "lineTotal":2600 } ] } ] }
```
- `closeAt` nullable. "주문 받는 중" = `closeAt == null || now < closeAt`. 경과 시 제출 409.
- 동일 `person` 재제출 시 덮어쓰기(1인 1메뉴). `reset` → `BoardData.empty()` → `closeAt=null, orders=[]`.

### 3.4 팀 설정 (application.yml)
```yaml
mukja:
  data-dir: ${MUKJA_DATA_DIR:./data}
  admin-pin: ${MUKJA_ADMIN_PIN:1234}
  teams:
    - { id: ice,     name: ICE }
    - { id: kos,     name: KOS }
    - { id: icis,    name: ICIS }
    - { id: vivaldi, name: 비발디 }
```

---

## 4. API / 라우트

| Method | Path | 설명 |
|---|---|---|
| GET | `/` | 기본 보드(`/coffee/kt/ice`)로 리다이렉트 |
| GET | `/{category}` | 그룹의 첫 가게/기본 팀으로 리다이렉트 |
| GET | `/{category}/{vendor}/{team}` | 주문판 화면 |
| GET | `/{category}/{vendor}/{team}/menu?cat={sub}` | 메뉴 그리드 fragment |
| GET | `/{category}/{vendor}/{team}/menu/{itemId}/options` | 옵션 모달 fragment |
| POST | `/{category}/{vendor}/{team}/orders` | 주문 제출/수정 (마감 409, 빈 이름·복수 라인 400) |
| POST | `/{category}/{vendor}/{team}/orders/delete` | 특정 person 주문 취소 |
| GET | `/{category}/{vendor}/{team}/status` | 집계 (HTMX면 panel fragment) |
| GET | `/{category}/{vendor}/{team}/status/summary.txt` | 복사용 요약 텍스트 |
| GET | `/{category}/{vendor}/{team}/status/stream` | SSE 구독 |
| POST | `/{category}/{vendor}/{team}/deadline` · `/reset` | 마감 설정·해제 / 초기화 (PIN 없음) |
| GET | `/admin/login` · `POST /admin/auth` · `GET /admin` | 관리자(PIN 보호, 메뉴 조회) |

- `{category}`는 `coffee|food` 정규식으로 제약 — `/css`·`/js`·`/webjars`·`/img` 정적 경로 보호.
- 정적 자산은 **콘텐츠 해시 버저닝**(`spring.web.resources.chain.strategy.content`) — `/css/app-custom-<hash>.css` 형태로 서빙되어 CDN(Cloudflare) 캐시가 자동 무효화된다.

---

## 5. 화면 (모바일 우선 · PC는 480 컬럼)

### 5.1 주문판 `/{category}/{vendor}/{team}`
- **헤더**: ☰(가게 드로어) · 가게명 + ☕/🍱 · 우측 마감 배지(임박 주황·종료 **빨강**). 부제 라인 우측에 **● 팀명 세션** 배지.
- **팀 알약** 행 + **주문자 알약** 가로 슬라이드(가나다순 + 마우스 드래그 + 끝까지 시 페이드 사라짐 + "기타" → 직접 입력란).
- **주문/집계 세그먼트 토글**. **메뉴 종류 탭**(밑줄형) · **2열 메뉴 카드 그리드**(HOT/ICE/HOT·ICE 태그) · 하단 고정 **CTA**("메뉴를 선택하세요" / "메뉴명 · 가격 주문하기" · "주문 취소").
- **옵션 모달**: 하단시트, single 알약·toggle 스위치·counter 스테퍼. 비필수 single은 기본값 선택. 옵션이 없으면 모달이 가격(`~` 없이)과 "담기"만 노출.
- **가게 드로어**: 커피/점심 그룹 아래 가게 목록 + 맨 아래 **📖 사용 가이드** 버튼 → 7단계 모달.
- PC에선 콘텐츠·드로어·하단시트·CTA 모두 480 컬럼 폭으로 가운데 정렬.

### 5.2 집계 `/status`
- 상단: 가게·팀명, **주문 요약 복사**, (점심) **📞 〇〇 전화하기 031-xxx-xxxx** 풀폭 버튼(`tel:`).
- 당번 컨트롤: 마감 설정(디폴트 현재+30분)·마감 해제·초기화.
- 통계: 인원/잔수(또는 개수)/총액·1인당. 단위는 `coffee`=잔, `food`=개.
- **미주문자**: "🔔 미주문 N명" 한 줄, **다음 줄에 가나다순 이름 나열**(또는 ✅ 전원 주문 완료).
- **메뉴별**: 옵션 분해(`HOT 2, ICE·얼음 많이 1`). 옵션 없는 메뉴는 괄호 없이 "삼계탕 3개"만.
- **사람별(배분용)**: 사람마다 메뉴+옵션 + **삭제** 버튼.
- SSE 실시간 갱신 + 폴링 폴백.

### 5.3 관리자 `/admin`
- PIN 게이트 → 메뉴 조회. (편집은 후속.)

---

## 6. 비기능 요구사항

- **동시성**: 보드별 `JsonStore.mutate`(write-lock read-modify-write, tmp→`ATOMIC_MOVE`)로 동시 제출 무손실.
- **자정 초기화**: `BoardResetScheduler @Scheduled(cron="0 0 0 * * *", zone="Asia/Seoul")` 가 모든 (가게×팀) 보드에 `reset()` → SSE 브로드캐스트. 단, 서버가 자정에 떠 있어야 발화(놓친 분량 소급 X).
- **데이터 보존**: 컨테이너 재시작에도 `data/` 유지. 단, 운영 서버 볼륨이 비어 있으면 시드가 새로 복사됨(seed-on-missing).
- **CDN 캐시 안전**: CSS/JS 콘텐츠 해시 버저닝으로 Cloudflare 캐시 자동 무효화 — 매 배포마다 신경 쓸 필요 없음.
- **링크 미리보기**: Open Graph + Twitter Card 메타 + `og.png` 카드 이미지(1200×630) + 브랜드 favicon. 메신저는 미리보기를 캐시하므로 옛 링크는 새로 공유해 갱신.
- **프라이버시**: 계좌/금융정보 저장 금지, 외부 전송 없음. 시각 전부 KST 직렬화.
- **코딩 컨벤션**: 파일 최상단 역할 주석·함수 위 기능 주석(한국어), record·생성자 주입, Lombok 미사용. Node/npm 미사용.

---

## 7. 빌드/배포

- 로컬: `./gradlew bootRun`(기본 8080), `./gradlew test`.
- 컨테이너: `docker build -t mukja .` — multi-stage(Gradle bootJar → JRE).
- 배포: GitHub Actions(self-hosted Mac mini 러너) → `docker run` 으로 기동, Cloudflare Tunnel 뒤 NPM 프록시.
- CSS 빌드 없음(동결 `app.css` + 손수 쓴 `app-custom.css`). E2E 테스트(Playwright) 별도 디렉터리 없음.

---

## 8. 완료/후속

- **완료**: 보드 모델 v3(가게×팀), 식당 8곳 + 전화하기, 팀 명단 4팀 + 가나다 정렬, 마감 빨강·세션 배지, 자정 자동 초기화, PC 폰 레이아웃, OG·favicon, CDN 해시 버저닝, 사용 가이드 인앱 모달.
- **후속 후보**: 관리자 인라인 메뉴 편집, 주문 이력/통계, Jasypt PIN 암호화, 자정 다운타임 보정(기동 시 미초기화 보드 감지), Cloudflare Access.
