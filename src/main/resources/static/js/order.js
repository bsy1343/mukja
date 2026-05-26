// order.js — HTMX 보조: 장바구니, 제출, 카운트다운, 옵션 모달, 마감/초기화, 뷰 토글, 드로어, SSE
(function () {
  const cart = []; // {itemId, name, options, lineTotal}
  const PERSON_KEY = 'mukja.person';
  const MEMBER_KEY = 'mukja.member';
  let es = null; // 현재 SSE 연결 (중복 방지)

  const won = n => (n || 0).toLocaleString('ko-KR');

  // 보드 기본 경로 (.../status 에서 호출돼도 보드 경로로 환원)
  function boardBase() { return location.pathname.replace(/\/status$/, ''); }

  document.addEventListener('DOMContentLoaded', () => {
    const personInput = document.getElementById('person');
    if (personInput) {
      personInput.value = localStorage.getItem(PERSON_KEY) || '';
      personInput.addEventListener('input', () => {
        localStorage.setItem(PERSON_KEY, personInput.value);
        refreshMyOrderStatus();
      });
    }
    // 팀원 콤보 복원 (현재 팀에 있는 이름일 때만)
    const memberSel = document.getElementById('member');
    if (memberSel) {
      const saved = localStorage.getItem(MEMBER_KEY);
      if (saved && [...memberSel.options].some(o => o.value === saved)) memberSel.value = saved;
      onMemberChange();
    }
    refreshMyOrderStatus();
    startCountdown();
    connectSse();
    // 옵션 모달: 바깥(백드롭) 탭하면 닫기 — 안 닫히면 모달이 탭/토글 클릭을 가로챔
    const dlg = document.getElementById('opt-dialog');
    if (dlg) dlg.addEventListener('click', e => { if (e.target === dlg) dlg.close(); });
  });

  // HTMX swap 후 처리: 집계 SSE 재연결 / 메뉴 그리드 뱃지 갱신 / 옵션 모달 초기화
  document.body.addEventListener('htmx:afterSwap', e => {
    if (!e.target) return;
    if (e.target.id === 'agg-view') connectSse();
    if (e.target.id === 'menu-grid') refreshCardBadges();
    if (e.target.id === 'opt-content') updateModalPrice();
  });

  // 주문/집계 뷰 토글 (DaisyUI tab 없이 btn 색만 전환)
  window.showView = function (mode) {
    const isOrder = mode === 'order';
    document.getElementById('order-view')?.classList.toggle('hidden', !isOrder);
    document.getElementById('agg-view')?.classList.toggle('hidden', isOrder);
    document.getElementById('cart-bar')?.classList.toggle('hidden', !isOrder);
    document.getElementById('tab-order')?.classList.toggle('seg-on', isOrder);
    document.getElementById('tab-agg')?.classList.toggle('seg-on', !isOrder);
  };

  // 팀 드롭다운 이동 (option value = 대상 URL)
  window.goTeam = function (url) { if (url) location.href = url; };

  // 카테고리 드로어 열고닫기
  window.toggleDrawer = function () { document.body.classList.toggle('drawer-open'); };

  // 이미 주문한 사람 목록 (페이지 로드 시 서버가 주입)
  function ordered() { return window.mukjaOrdered || []; }

  // 현재 주문자 이름: 팀원 콤보 값(기타가 아니면), 기타면 직접입력란 값
  function personName() {
    const m = document.getElementById('member');
    if (m && m.value && m.value !== '__etc__') return m.value;
    const i = document.getElementById('person');
    return (i && i.value || '').trim();
  }

  // 팀원 콤보 변경: '기타' 선택 시 같은 줄에 직접입력란 표시 (콤보는 줄어듦)
  window.onMemberChange = function () {
    const m = document.getElementById('member');
    const person = document.getElementById('person');
    const isEtc = m && m.value === '__etc__';
    if (person) person.style.display = isEtc ? '' : 'none';
    m?.classList.toggle('flex-1', !isEtc);
    if (m) localStorage.setItem(MEMBER_KEY, m.value);
    if (isEtc) person?.focus();
    refreshMyOrderStatus();
  };

  // 이름이 이미 주문한 사람이면 '이미 주문함 + 취소' 표시
  function refreshMyOrderStatus() {
    const el = document.getElementById('my-order-status');
    if (!el) return;
    const name = personName();
    el.style.display = (name && ordered().includes(name)) ? 'flex' : 'none';
  }

  // 내 주문 취소 (이름 기준 삭제)
  window.cancelOrder = async function () {
    const name = personName();
    if (!name || !ordered().includes(name)) return;
    if (!confirm(name + '님 주문을 취소할까요?')) return;
    await fetch(boardBase() + '/orders/delete', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ person: name }) });
    location.reload();
  };

  // 모달의 현재 선택으로 가격 계산 (데이터 주도: data-extra. 서버가 제출 시 권위 있게 재계산)
  function computePrice() {
    const root = document.querySelector('#opt-content [data-item]');
    const form = document.getElementById('opt-form');
    if (!root) return 0;
    let total = parseInt(root.dataset.price, 10) || 0;
    if (!form) return total;
    form.querySelectorAll('input[type=radio]:checked').forEach(r =>
      total += parseInt(r.closest('.opt-pill')?.dataset.extra || '0', 10));
    form.querySelectorAll('input[type=checkbox]:checked').forEach(c =>
      total += parseInt(c.dataset.extra || '0', 10));
    form.querySelectorAll('input[type=number]').forEach(n =>
      total += (parseInt(n.value, 10) || 0) * (parseInt(n.dataset.extra || '0', 10)));
    return total;
  }

  // 필수 옵션이 모두 선택됐는지
  function requiredOk() {
    let ok = true;
    document.querySelectorAll('#opt-form .opt-group[data-required="true"]').forEach(g => {
      if (!g.querySelector('input[type=radio]:checked')) ok = false;
    });
    return ok;
  }

  // 모달 담기 버튼: 실시간 가격 + 필수 미선택 시 비활성
  function updateModalPrice() {
    const addBtn = document.getElementById('modal-add');
    if (!addBtn) return;
    if (!requiredOk()) {
      addBtn.disabled = true;
      addBtn.textContent = '옵션을 선택하세요';
    } else {
      addBtn.disabled = false;
      addBtn.textContent = '담기 · ' + won(computePrice()) + '원';
    }
  }

  // 샷 스테퍼 (+/−)
  window.stepCounter = function (btn, delta) {
    const input = btn.parentElement.querySelector('input[type=number]');
    if (!input) return;
    const max = parseInt(input.max, 10) || 99;
    input.value = Math.max(0, Math.min(max, (parseInt(input.value, 10) || 0) + delta));
    updateModalPrice();
  };

  // 옵션 모달의 폼에서 선택값을 읽어 장바구니에 담는다
  window.addToCart = function () {
    const root = document.querySelector('#opt-content [data-item]');
    const form = document.getElementById('opt-form');
    if (!root || !form || !requiredOk()) return;
    const data = new FormData(form);
    const options = {};
    for (const [k, v] of data.entries()) {
      if (v === 'on') options[k] = true;
      else if (/^\d+$/.test(v)) options[k] = parseInt(v, 10);
      else options[k] = v;
    }
    cart.push({ itemId: parseInt(root.dataset.item, 10), name: root.dataset.name, options, lineTotal: computePrice() });
    document.getElementById('opt-dialog').close();
    renderCart();
    refreshCardBadges();
  };

  // 하단 카트바 갱신 (총액 표시)
  function renderCart() {
    const btn = document.getElementById('submit-btn');
    if (!btn) return;
    if (cart.length === 0) { btn.disabled = true; btn.textContent = '담은 메뉴 없음'; return; }
    btn.disabled = false;
    const total = cart.reduce((s, c) => s + (c.lineTotal || 0), 0);
    const first = cart[0].name;
    const label = cart.length === 1 ? first : `${first} 외 ${cart.length - 1}건`;
    btn.textContent = `${label} · ${won(total)}원 주문하기`;
  }

  // 메뉴 카드에 장바구니 수량 뱃지 + 담김 강조 반영
  function refreshCardBadges() {
    document.querySelectorAll('.menu-card[data-item-id]').forEach(card => {
      const id = parseInt(card.dataset.itemId, 10);
      const qty = cart.filter(c => c.itemId === id).length;
      const badge = card.querySelector('.card-qty');
      card.classList.toggle('in-cart', qty > 0);
      if (badge) { badge.textContent = qty; badge.style.display = qty > 0 ? '' : 'none'; }
    });
  }

  // 주문 제출 (서버에서 가격 계산)
  window.submitOrder = async function () {
    const person = personName();
    if (!person) { alert('이름을 선택하거나 입력하세요'); return; }
    if (cart.length === 0) return;
    if (ordered().includes(person) && !confirm('이미 주문하셨어요. 새로 담은 내용으로 변경할까요?')) return;
    const res = await fetch(boardBase() + '/orders', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ person, lines: cart.map(c => ({ itemId: c.itemId, options: c.options })) })
    });
    if (res.status === 409) { alert('마감된 주문판입니다'); return; }
    if (res.ok) {
      alert('주문 완료!');
      if (!ordered().includes(person)) ordered().push(person);
      cart.length = 0; renderCart(); refreshCardBadges(); refreshMyOrderStatus();
    }
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

  // single 옵션 라디오 선택 시 알약 강조 + 모달 가격/필수 갱신
  document.addEventListener('change', e => {
    if (e.target.type === 'radio') {
      document.querySelectorAll(`[name="${e.target.name}"]`).forEach(r =>
        r.closest('label')?.classList.toggle('btn-primary', r.checked));
    }
    if (e.target.closest && e.target.closest('#opt-form')) updateModalPrice();
  });

  // 집계 사람별 목록의 삭제 버튼 (당번이 각자 주문 삭제)
  document.addEventListener('click', async e => {
    const btn = e.target.closest && e.target.closest('.order-del');
    if (!btn) return;
    const person = btn.dataset.person;
    if (!person || !confirm(person + '님 주문을 삭제할까요?')) return;
    await fetch(boardBase() + '/orders/delete', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ person }) });
    location.reload();
  });
})();
