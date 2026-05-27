# mukja 먹자

사내 커피·점심 주문을 **카테고리 × 팀 단위 상시 주문판**으로 취합하는 모바일 우선 웹앱.
세션 개념 없이 주문판은 상시 존재하며, 당번이 마감 시각을 설정하고 매일 자정(KST) 자동 초기화로 재사용한다.

## 주요 기능

- **주문판 = (가게 × 팀)** — 좌측 가게 드로어, 상단 팀 전환, 주문/집계 토글의 단일 화면
- **데이터 주도 메뉴** — 가격·옵션·옵션 텍스트를 `menus.json`만으로 제어 (커피 HOT/ICE·얼음·연하게·샷, 식당 식사 선택 등)
- **주문자 선택** — 팀 명단 알약(가로 슬라이드·드래그·가나다순), "기타" 직접 입력, 1인 1메뉴
- **집계/발주** — 메뉴별·사람별 집계, 미주문자 목록(가나다순), 요약 텍스트 복사, 식당 **전화하기** 버튼
- **마감/초기화** — 마감 시각 설정 시 주문 차단(409)·카운트다운(임박 주황·마감 빨강), 수동 초기화 및 **매일 00:00 KST 자동 초기화**
- **실시간 갱신** — 보드별 SSE + 폴링 폴백
- **링크 미리보기/파비콘** — Open Graph 카드 + 브랜드 favicon

## 기술 스택

- **Spring Boot 3.5.3 / Java 21 / Gradle (Kotlin DSL)** — 패키지 `com.mukja`
- **Thymeleaf SSR + HTMX**(webjar) — 부분 갱신
- **CSS는 npm/빌드 없이** — 동결된 `static/css/app.css`(Tailwind/DaisyUI 산출물) + 손수 쓴 `app-custom.css`. 정적 자산은 콘텐츠 해시 버저닝으로 CDN 캐시 자동 무효화
- **저장소: 보드별 JSON 파일** — `JsonStore<T>`(RWLock·atomic move), `data/orders/{vendor}-{team}.json`. 별도 DB 없음

> Node/npm 미사용 프로젝트. htmx는 Gradle 의존성(`org.webjars.npm:htmx.org`)으로 제공.

## 빠른 시작

```bash
./gradlew bootRun     # 실행 (기본 http://localhost:8080)
./gradlew test        # 테스트
docker build -t mukja .   # 컨테이너 이미지 빌드
```

> Gradle 래퍼는 8.14, 컴파일 toolchain은 JDK 21. 시스템에 JDK 21이 없으면
> `~/.gradle/gradle.properties`의 `org.gradle.java.installations.paths`로 경로를 지정한다.

## 설정 (환경변수)

| 키 | 기본값 | 설명 |
|---|---|---|
| `MUKJA_DATA_DIR` | `./data` | 보드·메뉴·명단 JSON 저장 위치 |
| `MUKJA_ADMIN_PIN` | `1234` | `/admin/**` 보호 PIN |
| `SERVER_PORT` | `8080` | 서비스 포트 |

- 시각은 전부 KST(`Asia/Seoul`) 직렬화.
- 팀 목록은 `application.yml`의 `mukja.teams`에서 관리: **ICE · KOS · ICIS · 비발디**.

## 데이터

`data/`는 gitignore. 최초 기동 시 클래스패스 seed가 **없을 때만** 복사된다.

- `menus.seed.json` → `data/menus.json` : 가게·카테고리·메뉴·옵션 정의
  - `group: "coffee" | "food"` 로 상위 구분. 식당은 `phone`으로 전화하기 버튼 노출
  - 현재 가게: 커피 **KT그룹희망나눔재단** / 점심 **고향집삼계탕·두향·란반·밥상머리·예돈·라이라이·푸른바다볼테기**
- `teams.seed.json` → `data/teams.json` : 팀별 주문자 명단

> 메뉴·전화번호·명단을 바꾸려면 seed 수정 후, 운영 서버의 `data/*.json`을 갱신(삭제 후 재기동 시 재복사)한다.

## 주요 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/` → `/{coffee}/{가게}/{팀}` | 기본 보드로 리다이렉트 |
| GET | `/{category}/{vendor}/{team}` | 주문판 화면 |
| POST | `/{category}/{vendor}/{team}/orders` | 주문 제출 (마감 시 409) |
| GET | `/{category}/{vendor}/{team}/status` | 집계(HTMX면 panel fragment) |
| GET | `/{category}/{vendor}/{team}/status/summary.txt` | 복사용 요약 |
| GET | `/{category}/{vendor}/{team}/status/stream` | SSE 구독 |
| POST | `/{category}/{vendor}/{team}/deadline` · `/reset` | 마감 설정·해제 / 초기화 |
| GET | `/admin/login`, `/admin` | 관리(PIN 보호, 메뉴 조회) |

- `category`는 `{coffee|food}` 정규식으로 제약 — static 경로(`/css`,`/js`,`/webjars`,`/img`) 보호.

## 아키텍처 메모

- 패키지: `common/store`(JsonStore) · `config` · `menu` · `order`(+`order.sse`) · `admin`
- JSON 접근은 항상 `JsonStore<T>` 경유(`read`/`write`/`mutate`). 쓰기는 tmp → `ATOMIC_MOVE`
- 초기화(`reset`)는 주문·마감을 함께 비운다(`BoardData.empty()`)
- 계좌/금융정보 미저장 — 이름·주문만 보관. 마감·초기화는 권한 없음(팀 내부 신뢰 전제)

설계 결정의 배경은 `docs/superpowers/specs/2026-05-22-mukja-team-order-boards-design.md` 참고.
