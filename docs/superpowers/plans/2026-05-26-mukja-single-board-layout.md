# mukja 단일 주문판 레이아웃 리디자인 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **커밋 정책(사용자 지시):** 자동으로 `git commit`/`git push` 하지 않는다. 각 Task 끝의 "커밋 체크포인트"는 **사용자가 명시적으로 요청할 때만** 실행한다. 기본은 변경을 만들고 멈춰서 보고한다.

**Goal:** 카테고리 선택→팀 선택→주문판의 3페이지 흐름을, 좌측 카테고리 드로어 + 상단 팀 드롭다운/이름 + `주문|집계` 토글을 갖춘 **단일 모바일 주문판** 한 화면으로 통합한다.

**Architecture:** 백엔드는 `OrderController` 라우팅만 변경(루트/카테고리 리다이렉트, 모델 속성 추가, status를 HTMX fragment로도 응답). 화면은 Thymeleaf 템플릿 재구성 + 바닐라 JS. 스타일은 **npm 빌드 없이** — 이미 커밋된 `app.css`(Tailwind+DaisyUI 산출물)를 그대로 쓰고, 미컴파일 클래스(드로어/select 모디파이어)는 손수 쓴 `app-custom.css`로 보완.

**Tech Stack:** Spring Boot 3.5, Thymeleaf, HTMX 2, 바닐라 JS, 손수 쓴 CSS(빌드 없음). Java 21, JUnit5 + MockMvc, Playwright E2E.

**Spec:** `docs/superpowers/specs/2026-05-26-mukja-single-board-layout-redesign.md`

---

## File Structure

| 파일 | 역할 | 작업 |
|---|---|---|
| `src/main/java/dev/sybaek/mukja/order/OrderController.java` | 라우팅 | Modify (리다이렉트, 모델, status fragment, TopCategory) |
| `src/main/resources/templates/order/board.html` | 단일 주문판 | Rewrite |
| `src/main/resources/templates/order/status.html` | 집계 (전체+panel fragment 겸용) | Rewrite |
| `src/main/resources/templates/order/category.html` | (구) 카테고리 선택 | Delete |
| `src/main/resources/templates/order/team.html` | (구) 팀 선택 | Delete |
| `src/main/resources/templates/layout.html` | 공통 레이아웃 | Modify (app-custom.css 링크) |
| `src/main/resources/static/css/app-custom.css` | 미컴파일 클래스 보강(드로어/select) | Create |
| `src/main/resources/static/js/order.js` | 카트/제출/토글/드로어/SSE | Rewrite |
| `src/test/java/dev/sybaek/mukja/order/NavControllerTest.java` | 리다이렉트/보드 진입 | Rewrite |
| `src/test/java/dev/sybaek/mukja/order/StatusControllerTest.java` | fragment 응답 | Modify (+1 test) |
| `e2e/tests/order.spec.ts` | 핵심 흐름 | Rewrite |
| `.gitignore` | 산출물 제외 | Modify (.playwright-mcp 등) |

**확정 사실(검증 완료):** `app.css`에 이미 포함 — `hidden, btn, btn-sm, btn-ghost, btn-primary, btn-warning, badge, badge-xs, badge-success, badge-warning, tab-active, input-bordered, input-sm, card, stat, modal, menu, dropdown`. **미포함** — `drawer*, select-bordered, select-sm, join, tab(단일)`. 따라서 드로어와 select는 `app-custom.css`로, 토글은 `btn-sm`+`btn-primary/btn-ghost`로 구현(추가 클래스 0).

---

## Task 1: 백엔드 라우팅 + status fragment

**Files:**
- Modify: `src/main/java/dev/sybaek/mukja/order/OrderController.java`
- Rewrite: `src/main/resources/templates/order/status.html`
- Delete: `src/main/resources/templates/order/category.html`, `src/main/resources/templates/order/team.html`
- Rewrite: `src/test/java/dev/sybaek/mukja/order/NavControllerTest.java`
- Modify: `src/test/java/dev/sybaek/mukja/order/StatusControllerTest.java`

- [ ] **Step 1: NavControllerTest를 리다이렉트 기대로 교체 (실패하는 테스트)**

`src/test/java/dev/sybaek/mukja/order/NavControllerTest.java` 전체를 교체:

```java
// NavControllerTest.java — 루트/카테고리 리다이렉트 + 보드 진입 검증
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
class NavControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void rootRedirectsToDefaultBoard() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/all"));
    }

    @Test
    void categoryRedirectsToDefaultTeam() throws Exception {
        mvc.perform(get("/coffee")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/all"));
    }

    @Test
    void boardRendersOk() throws Exception {
        mvc.perform(get("/coffee/all")).andExpect(status().isOk());
    }
}
```

> Task 3에서 board.html이 팀 목록·드로어를 렌더하면 이 `boardRendersOk`를 팀/카테고리 내용 단언으로 강화한다.

- [ ] **Step 2: StatusControllerTest에 fragment 테스트 추가 (실패하는 테스트)**

`StatusControllerTest.java`에 import와 테스트 메서드를 추가한다. 클래스 안 `summaryTxtIsPlainText` 뒤에 삽입:

```java
    @Test
    void statusFragmentForHtmxRequest() throws Exception {
        mvc.perform(get("/coffee/stat-team/status").header("HX-Request", "true"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")))
           .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<html"))));
    }
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `./gradlew test --tests NavControllerTest --tests StatusControllerTest`
Expected: FAIL — NavControllerTest는 `/`가 200(현재 category.html)이라 redirect 단언 실패, status fragment는 `<html` 포함이라 실패.

- [ ] **Step 4: status.html을 panel fragment 겸용으로 재작성**

`src/main/resources/templates/order/status.html` 전체 교체:

```html
<!-- status.html — 집계/발주. 전체 페이지 + #agg-view에 swap되는 panel fragment 겸용 -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout :: page('mukja · 집계', ~{:: #content})}">
<body>
  <div id="content" th:fragment="content">
    <div th:replace="~{:: panel}">집계</div>
  </div>

  <!-- panel: 보드의 집계 뷰로 swap되는 조각 -->
  <div th:fragment="panel" class="p-3 space-y-4"
       th:with="s=${agg.stats}">
    <header class="flex items-center justify-between">
      <h1 class="font-bold" th:text="'☕ 커피 · ' + ${teamName}">집계</h1>
      <button class="btn btn-sm btn-primary" onclick="copySummary(event)">주문 요약 복사</button>
    </header>

    <!-- 당번 컨트롤 (주문 화면에서 이동) -->
    <div class="flex gap-1">
      <button class="btn btn-xs flex-1" onclick="setDeadline()">마감 설정</button>
      <button class="btn btn-xs flex-1" onclick="clearDeadline()">마감 해제</button>
      <button class="btn btn-xs btn-warning flex-1" onclick="resetBoard()">초기화</button>
    </div>

    <div class="stats shadow w-full text-center">
      <div class="stat p-2"><div class="stat-title text-xs">인원</div>
        <div class="stat-value text-lg" th:text="${s.people}">8</div></div>
      <div class="stat p-2"><div class="stat-title text-xs">잔수</div>
        <div class="stat-value text-lg" th:text="${s.cups}">12</div></div>
      <div class="stat p-2"><div class="stat-title text-xs">총액</div>
        <div class="stat-value text-lg" th:text="${#numbers.formatInteger(s.totalAmount,0,'COMMA')}">48,500</div></div>
    </div>
    <p class="text-xs opacity-70"
       th:text="'1인당 약 ' + ${#numbers.formatInteger(s.perPersonAmount,0,'COMMA')} + '원'">1인당</p>

    <section>
      <h2 class="font-semibold mb-2">메뉴별</h2>
      <div th:each="m : ${agg.byMenu}" class="card bg-base-200 p-3 mb-2">
        <div class="flex justify-between"><span th:text="${m.name}">아메리카노</span>
          <span class="badge" th:text="${m.totalCount} + '잔'">5잔</span></div>
        <div class="text-xs opacity-70">
          <span th:each="b, i : ${m.optionBreakdown}"
                th:text="${b.key} + ' ' + ${b.value} + (${i.last} ? '' : ', ')">ICE 3</span>
        </div>
      </div>
    </section>

    <section>
      <h2 class="font-semibold mb-2">사람별 (배분용)</h2>
      <div th:each="entry : ${agg.byPerson}" class="border-b py-2">
        <div class="font-medium" th:text="${entry.key}">백상열</div>
        <ul class="text-sm opacity-80">
          <li th:each="line : ${entry.value}"
              th:text="${line.name} + (${line.optionText.isEmpty()} ? '' : (' · ' + ${line.optionText}))">아메리카노 · ICE</li>
        </ul>
      </div>
    </section>

    <div id="sse" hx-ext="sse"
         th:attr="sse-connect=@{/{c}/{t}/status/stream(c=${category},t=${team})}"></div>
  </div>
</body>
</html>
```

(인라인 `copySummary` 스크립트는 제거됨 — Task 3에서 order.js로 이동.)

- [ ] **Step 5: OrderController 리다이렉트 + 모델 + status fragment 분기로 수정**

`OrderController.java`에서 import 추가 — 기존 `import org.springframework.web.bind.annotation.RequestParam;` 아래에 삽입:

```java
import org.springframework.web.bind.annotation.RequestHeader;
```

`category()`(`@GetMapping("/")`)와 `team()`(`@GetMapping("/{category:coffee|food}")`) 두 메서드를 다음으로 **교체**:

```java
    // 루트 → 기본 보드로 리다이렉트
    @GetMapping("/")
    public String root() {
        return "redirect:/coffee/" + props.teams().get(0).id();
    }

    // 카테고리 단독 경로 → 해당 카테고리 기본 팀 보드로 리다이렉트
    @GetMapping("/{category:coffee|food}")
    public String categoryRedirect(@PathVariable String category) {
        return "redirect:/" + category + "/" + props.teams().get(0).id();
    }
```

`board()` 메서드를 다음으로 **교체**(teams·topCategories 추가):

```java
    // 주문판 화면 (단일 레이아웃)
    @GetMapping("/{category:coffee|food}/{team}")
    public String board(@PathVariable String category, @PathVariable String team, Model model) {
        var board = orderRepository.read(category, team);
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("teams", props.teams());
        model.addAttribute("topCategories", topCategories(category));
        model.addAttribute("subCategories", menuService.categoriesIn(category));
        model.addAttribute("closeAt", board.closeAt());
        return "order/board";
    }

    // 상단 드로어용 상위 카테고리 (해당 group에 데이터가 있으면 활성)
    private java.util.List<TopCategory> topCategories(String current) {
        return java.util.List.of(
            new TopCategory("coffee", "☕ 커피", !menuService.categoriesIn("coffee").isEmpty(), current.equals("coffee")),
            new TopCategory("food", "🍱 점심", !menuService.categoriesIn("food").isEmpty(), current.equals("food")));
    }

    // 드로어 항목 (id, 표시명, 활성여부, 현재선택여부)
    public record TopCategory(String id, String name, boolean available, boolean current) {}
```

`status()` 메서드를 다음으로 **교체**(HX-Request면 panel fragment):

```java
    // 집계/발주 화면. HTMX 요청이면 panel fragment, 아니면 전체 페이지
    @GetMapping("/{category:coffee|food}/{team}/status")
    public String status(@PathVariable String category, @PathVariable String team,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                         Model model) {
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("agg", aggregate(category, team));
        return hxRequest != null ? "order/status :: panel" : "order/status";
    }
```

- [ ] **Step 6: 구 선택 화면 템플릿 삭제**

Run: `git rm src/main/resources/templates/order/category.html src/main/resources/templates/order/team.html`
(또는 파일 시스템에서 삭제. 컨트롤러가 더는 참조하지 않음.)

- [ ] **Step 7: 테스트 통과 확인**

Run: `./gradlew test --tests NavControllerTest --tests StatusControllerTest --tests BoardControllerTest`
Expected: 전체 PASS. (`boardRendersOk`는 구 board.html이라도 200이면 통과. 리다이렉트 2건, status fragment, 기존 status/summary, board 헤더 모두 통과.)

- [ ] **Step 8: 전체 컴파일 확인**

Run: `./gradlew compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: 커밋 체크포인트 (사용자 요청 시에만)**

```bash
git add src/main/java/dev/sybaek/mukja/order/OrderController.java \
  src/main/resources/templates/order/status.html \
  src/test/java/dev/sybaek/mukja/order/NavControllerTest.java \
  src/test/java/dev/sybaek/mukja/order/StatusControllerTest.java
git rm src/main/resources/templates/order/category.html src/main/resources/templates/order/team.html
git commit -m "feat: 단일보드용 라우팅(리다이렉트)+status fragment, 선택페이지 제거"
```

---

## Task 2: 손수 쓴 CSS + JS 헬퍼 (드로어/토글/SSE)

**Files:**
- Create: `src/main/resources/static/css/app-custom.css`
- Modify: `src/main/resources/templates/layout.html`
- Rewrite: `src/main/resources/static/js/order.js`

- [ ] **Step 1: app-custom.css 작성 (드로어 + 팀 select 보강)**

`src/main/resources/static/css/app-custom.css` 새로 생성:

```css
/* app-custom.css — npm 빌드 없이 손수 쓴 보강 스타일. app.css 뒤에 로드되어 미컴파일 클래스를 대체한다. */

/* 팀 드롭다운 (DaisyUI select 모디파이어가 트리셰이크되어 미컴파일 → 직접 스타일) */
.board-select {
  height: 2rem; min-width: 5rem; padding: 0 1.75rem 0 .6rem;
  border: 1px solid #d1d5db; border-radius: .5rem; background-color: #fff;
  font-size: .875rem; line-height: 1; color: inherit; appearance: none; cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 20 20' fill='none' stroke='%236b7280' stroke-width='1.6'%3E%3Cpath d='M6 8l4 4 4-4'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right .4rem center;
}

/* 카테고리 슬라이드 드로어 (DaisyUI drawer 미컴파일 → 손수 구현) */
#cat-drawer {
  position: fixed; top: 0; left: 0; bottom: 0; width: 15rem; max-width: 80vw;
  background: #fff; box-shadow: 2px 0 12px rgba(0,0,0,.15); z-index: 50;
  transform: translateX(-100%); transition: transform .25s ease; overflow-y: auto;
}
#cat-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 40;
  opacity: 0; pointer-events: none; transition: opacity .25s ease;
}
body.drawer-open #cat-drawer { transform: translateX(0); }
body.drawer-open #cat-backdrop { opacity: 1; pointer-events: auto; }
```

- [ ] **Step 2: layout.html에 app-custom.css 링크 추가**

`layout.html`의 `<link rel="stylesheet" th:href="@{/css/app.css}">` 줄 **바로 아래**에 추가:

```html
  <link rel="stylesheet" th:href="@{/css/app-custom.css}">
```

- [ ] **Step 3: order.js 전체 재작성 (boardBase, 뷰 토글, 드로어, SSE 재연결, copySummary 이동)**

`src/main/resources/static/js/order.js` 전체 교체:

```js
// order.js — HTMX 보조: 장바구니, 제출, 카운트다운, 옵션 모달, 마감/초기화, 뷰 토글, 드로어, SSE
(function () {
  const cart = []; // {itemId, name, options}
  const PERSON_KEY = 'mukja.person';
  let es = null; // 현재 SSE 연결 (중복 방지)

  // 보드 기본 경로 (.../status 에서 호출돼도 보드 경로로 환원)
  function boardBase() { return location.pathname.replace(/\/status$/, ''); }

  document.addEventListener('DOMContentLoaded', () => {
    const personInput = document.getElementById('person');
    if (personInput) {
      personInput.value = localStorage.getItem(PERSON_KEY) || '';
      personInput.addEventListener('input', () => localStorage.setItem(PERSON_KEY, personInput.value));
    }
    startCountdown();
    connectSse();
  });

  // 집계 패널이 #agg-view로 swap되면 그 안의 SSE를 다시 연결
  document.body.addEventListener('htmx:afterSwap', e => {
    if (e.target && e.target.id === 'agg-view') connectSse();
  });

  // 주문/집계 뷰 토글 (DaisyUI tab 없이 btn 색만 전환)
  window.showView = function (mode) {
    const isOrder = mode === 'order';
    document.getElementById('order-view')?.classList.toggle('hidden', !isOrder);
    document.getElementById('agg-view')?.classList.toggle('hidden', isOrder);
    document.getElementById('cart-bar')?.classList.toggle('hidden', !isOrder);
    const to = document.getElementById('tab-order'), ta = document.getElementById('tab-agg');
    to?.classList.toggle('btn-primary', isOrder); to?.classList.toggle('btn-ghost', !isOrder);
    ta?.classList.toggle('btn-primary', !isOrder); ta?.classList.toggle('btn-ghost', isOrder);
  };

  // 팀 드롭다운 이동 (option value = 대상 URL)
  window.goTeam = function (url) { if (url) location.href = url; };

  // 카테고리 드로어 열고닫기
  window.toggleDrawer = function () { document.body.classList.toggle('drawer-open'); };

  // 옵션 모달의 폼에서 선택값을 읽어 장바구니에 담는다
  window.addToCart = function () {
    const root = document.querySelector('#opt-content [data-item]');
    const form = document.getElementById('opt-form');
    if (!root || !form) return;
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

  // 하단 카트바 갱신
  function renderCart() {
    const btn = document.getElementById('submit-btn');
    if (!btn) return;
    if (cart.length === 0) { btn.disabled = true; btn.textContent = '담은 메뉴 없음'; return; }
    btn.disabled = false;
    const first = cart[0].name;
    btn.textContent = cart.length === 1 ? `${first} 주문하기` : `${first} 외 ${cart.length - 1}건 주문하기`;
  }

  // 주문 제출 (서버에서 가격 계산)
  window.submitOrder = async function () {
    const person = (document.getElementById('person').value || '').trim();
    if (!person) { alert('이름을 입력하세요'); return; }
    if (cart.length === 0) return;
    const res = await fetch(boardBase() + '/orders', {
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

  // 마감 시각 설정
  window.setDeadline = async function () {
    const v = prompt('마감 시각 (HH:MM)', '14:30'); if (!v) return;
    await fetch(boardBase() + '/deadline', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ time: v }) });
    location.reload();
  };
  // 마감 해제
  window.clearDeadline = async function () {
    await fetch(boardBase() + '/deadline', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ time: null }) });
    location.reload();
  };
  // 주문판 초기화
  window.resetBoard = async function () {
    if (!confirm('이 주문판을 초기화할까요? 담긴 주문이 모두 지워집니다.')) return;
    await fetch(boardBase() + '/reset', { method: 'POST' });
    location.reload();
  };

  // 요약 텍스트 클립보드 복사 (status.html 인라인에서 이동)
  window.copySummary = async function (ev) {
    const res = await fetch(boardBase() + '/status/summary.txt');
    await navigator.clipboard.writeText(await res.text());
    const btn = ev.target, old = btn.textContent;
    btn.textContent = '복사됨 ✓'; setTimeout(() => btn.textContent = old, 2000);
  };

  // 집계 패널의 SSE 구독 → 갱신 시 새로고침 (폴백: 5초 폴링). 중복 연결 방지
  function connectSse() {
    const sseEl = document.getElementById('sse');
    if (!sseEl || !sseEl.getAttribute('sse-connect')) return;
    if (es) { es.close(); es = null; }
    try {
      es = new EventSource(sseEl.getAttribute('sse-connect'));
      es.addEventListener('order-update', () => location.reload());
      es.onerror = () => { es.close(); setInterval(() => location.reload(), 5000); };
    } catch (e) { setInterval(() => location.reload(), 5000); }
  }

  // single 옵션 라디오 선택 시 버튼 강조
  document.addEventListener('change', e => {
    if (e.target.type === 'radio') {
      document.querySelectorAll(`[name="${e.target.name}"]`).forEach(r =>
        r.closest('label')?.classList.toggle('btn-primary', r.checked));
    }
  });
})();
```

- [ ] **Step 4: 컴파일/기동 확인 (구 board.html이라도 깨지지 않아야 함)**

Run: `./gradlew test --tests NavControllerTest.rootRedirectsToDefaultBoard --tests StatusControllerTest`
Expected: PASS. (JS/CSS는 단위테스트 없음 — 정적 자원 변경이 기존 테스트를 깨지 않는지 확인.)

- [ ] **Step 5: 커밋 체크포인트 (사용자 요청 시에만)**

```bash
git add src/main/resources/static/css/app-custom.css src/main/resources/templates/layout.html src/main/resources/static/js/order.js
git commit -m "feat: 드로어/select 보강 CSS + 뷰토글·드로어·SSE재연결 JS"
```

---

## Task 3: 단일 주문판 board.html

**Files:**
- Rewrite: `src/main/resources/templates/order/board.html`

- [ ] **Step 1: board.html 전체 재작성**

`src/main/resources/templates/order/board.html` 전체 교체:

```html
<!-- board.html — 단일 주문판 (카테고리 드로어 + 상단바 + 주문/집계 토글) -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout :: page('주문', ~{:: #content})}">
<body>
  <div id="content" th:fragment="content" class="pb-24"
       th:with="base=@{/{c}/{t}(c=${category}, t=${team})}">

    <!-- 카테고리 드로어 -->
    <div id="cat-backdrop" onclick="toggleDrawer()"></div>
    <aside id="cat-drawer">
      <div class="p-3 font-bold text-lg border-b">뭐 먹자? 🍽️</div>
      <ul class="menu w-full p-2">
        <li th:each="top : ${topCategories}">
          <a th:if="${top.available}" th:href="@{/{c}/{t}(c=${top.id}, t=${team})}"
             th:classappend="${top.current} ? 'bg-base-200 font-bold'"
             th:text="${top.name}">☕ 커피</a>
          <span th:unless="${top.available}" class="opacity-40 flex justify-between items-center">
            <span th:text="${top.name}">🍱 점심</span><span class="badge badge-xs">준비중</span>
          </span>
        </li>
      </ul>
    </aside>

    <!-- 상단 바 -->
    <header class="sticky top-0 z-10 bg-base-100 border-b p-3">
      <div class="flex items-center gap-2">
        <button class="btn btn-ghost btn-sm" onclick="toggleDrawer()">☰</button>
        <select class="board-select" onchange="goTeam(this.value)">
          <option th:each="t : ${teams}"
                  th:value="@{/{c}/{t2}(c=${category}, t2=${t.id})}"
                  th:selected="${t.id == team}" th:text="${t.name}">팀</option>
        </select>
        <input id="person" class="input input-bordered input-sm flex-1" placeholder="이름을 입력하세요">
      </div>
      <div class="flex items-center justify-between mt-2">
        <div class="flex gap-1">
          <button id="tab-order" class="btn btn-sm btn-primary" onclick="showView('order')">주문</button>
          <button id="tab-agg" class="btn btn-sm btn-ghost"
                  th:hx-get="${base} + '/status'" hx-target="#agg-view" hx-swap="innerHTML"
                  onclick="showView('agg')">집계</button>
        </div>
        <span th:if="${closeAt}" id="countdown" class="badge badge-success" th:data-close="${closeAt}">마감</span>
      </div>
    </header>

    <!-- 주문 뷰 -->
    <div id="order-view">
      <nav class="flex gap-2 overflow-x-auto p-3">
        <button th:each="sub, i : ${subCategories}" class="btn btn-sm whitespace-nowrap"
                th:classappend="${i.first} ? 'btn-primary'"
                th:hx-get="${base} + '/menu?cat=' + ${sub.id}" hx-target="#menu-grid"
                th:text="${sub.name}">커피</button>
      </nav>
      <div id="menu-grid" th:hx-get="${base} + '/menu?cat=' + ${subCategories[0].id}"
           hx-trigger="load" hx-swap="innerHTML"></div>
    </div>

    <!-- 집계 뷰 (집계 토글 시 panel fragment가 swap됨) -->
    <div id="agg-view" class="hidden"></div>

    <!-- 카트바 (주문 모드에서만) -->
    <div id="cart-bar" class="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] p-3 bg-base-100 border-t">
      <button id="submit-btn" class="btn btn-primary w-full" onclick="submitOrder()" disabled>담은 메뉴 없음</button>
    </div>

    <dialog id="opt-dialog" class="modal modal-bottom"><div id="opt-content" class="modal-box"></div></dialog>
  </div>
</body>
</html>
```

- [ ] **Step 2: NavControllerTest의 boardRendersOk를 내용 단언으로 강화**

`NavControllerTest.java`의 `boardRendersOk` 메서드를 다음으로 교체:

```java
    @Test
    void boardListsTeamsAndCategories() throws Exception {
        mvc.perform(get("/coffee/all")).andExpect(status().isOk())
           .andExpect(content().string(containsString("전체")))
           .andExpect(content().string(containsString("SA팀")))
           .andExpect(content().string(containsString("커피")));
    }
```

- [ ] **Step 3: 전체 테스트 통과 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, 전체 PASS. (드롭다운의 `전체`/`SA팀`, 드로어 `커피`가 렌더되어 통과. `BoardControllerTest.boardPageShowsHeader`는 드롭다운 옵션 `SA팀`으로 통과.)

- [ ] **Step 4: 앱 기동 후 시각 검증 (수동)**

Run: `./gradlew bootRun` (백그라운드) 후
```bash
curl -s -o /dev/null -w "board:%{http_code}\n" localhost:8080/coffee/all
curl -s localhost:8080/ -o /dev/null -w "root:%{http_code}\n" -L
curl -s "localhost:8080/coffee/all/status" -H "HX-Request: true" | head -c 200
```
Expected: board:200, root 리다이렉트 후 200, status fragment는 `<html` 없이 `class="p-3 space-y-4"`로 시작.

브라우저(또는 Playwright)로 `localhost:8080/coffee/all` 확인: ☰ 클릭 시 드로어 슬라이드, 팀 드롭다운 변경 시 URL 이동, `집계` 클릭 시 통계/당번/메뉴별·사람별 표시, `주문` 복귀 시 카트바 재등장. 미컴파일 클래스로 깨진 부분 없는지 육안 확인.

- [ ] **Step 5: 커밋 체크포인트 (사용자 요청 시에만)**

```bash
git add src/main/resources/templates/order/board.html
git commit -m "feat: 단일 주문판 화면(드로어+팀드롭다운+주문/집계 토글)"
```

---

## Task 4: E2E 갱신 + 저장소 정리

**Files:**
- Rewrite: `e2e/tests/order.spec.ts`
- Modify: `.gitignore`

- [ ] **Step 1: .gitignore에 산출물 추가**

`.gitignore`에 다음 줄을 추가(없으면):

```
.playwright-mcp/
e2e/test-results/
```

- [ ] **Step 2: E2E 테스트를 단일 화면 흐름으로 재작성**

`e2e/tests/order.spec.ts` 전체 교체:

```ts
// order.spec.ts — 단일 주문판: 주문 → 집계 토글, 카테고리 드로어
import { test, expect } from '@playwright/test';

test('coffee order flow on single board', async ({ page }) => {
  await page.request.post('/coffee/sa/reset');

  await page.goto('/coffee/sa');
  await page.getByPlaceholder('이름을 입력하세요').fill('테스트');

  // 메뉴 카드 → 옵션 모달 → 담기
  await page.getByText('아메리카노').click();
  const dialog = page.locator('#opt-dialog');
  const addBtn = dialog.getByRole('button', { name: '담기' });
  await expect(addBtn).toBeVisible({ timeout: 10000 });
  await dialog.getByText('HOT', { exact: true }).click();
  await addBtn.click();

  // 제출 (alert 자동 처리)
  await page.getByRole('button', { name: /주문하기/ }).click();

  // 같은 화면에서 '집계' 토글 → #agg-view에 패널 swap
  await page.getByRole('button', { name: '집계' }).click();
  const agg = page.locator('#agg-view');
  await expect(agg.getByText('아메리카노').first()).toBeVisible({ timeout: 10000 });
  await expect(agg.getByText('테스트')).toBeVisible();
});

test('category drawer shows lunch as coming soon', async ({ page }) => {
  await page.goto('/coffee/all');
  await page.getByRole('button', { name: '☰' }).click();
  await expect(page.getByText('준비중')).toBeVisible();
});
```

- [ ] **Step 3: 앱 기동 상태에서 E2E 실행**

Run(앱이 8080에서 실행 중이어야 함): `cd e2e && npx playwright test`
Expected: 2 passed.

- [ ] **Step 4: 전체 회귀 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, 전체 PASS.

- [ ] **Step 5: 커밋 체크포인트 (사용자 요청 시에만)**

```bash
git add e2e/tests/order.spec.ts .gitignore
git commit -m "test: 단일 주문판 E2E 갱신 + 산출물 gitignore"
```

---

## Task 5: 문서 갱신 (CLAUDE.md)

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: 네비/엔드포인트/스타일 설명 갱신**

`CLAUDE.md`에서 다음을 반영:
- "네비" 줄: `GET /`·`GET /{category}`는 이제 기본 보드로 **리다이렉트**, 화면은 단일 보드(드로어+팀드롭다운+주문/집계 토글)임을 명시.
- status 설명: `HX-Request` 헤더면 `order/status :: panel` fragment 응답.
- 템플릿/CSS 노트: **npm 빌드 안 함**. 새 스타일은 손수 쓴 `static/css/app-custom.css`(빌드 없이 직접 편집), `app.css`는 기존 산출물 동결. (기존 "새 클래스 쓰면 npm run build:css" 지침은 "가능하면 app-custom.css에 평문 CSS로 추가, Tailwind 재빌드 지양"으로 수정.)
- "미완료"에서 해당 없음.

- [ ] **Step 2: 커밋 체크포인트 (사용자 요청 시에만)**

```bash
git add CLAUDE.md
git commit -m "docs: 단일 주문판 리디자인 반영 (네비/스타일 빌드 정책)"
```

---

## 최종 검증 체크리스트

- [ ] `./gradlew test` 전체 통과
- [ ] `cd e2e && npx playwright test` 통과 (앱 기동 중)
- [ ] `localhost:8080/` → `/coffee/all` 리다이렉트, 단일 화면에서 드로어·팀드롭다운·주문/집계 토글·당번 컨트롤 동작
- [ ] npm/CDN 미사용 — `app-custom.css`만 손수 편집, `app.css` 미재빌드
- [ ] 기존 주문/마감/초기화/SSE/요약 복사 정상
```
