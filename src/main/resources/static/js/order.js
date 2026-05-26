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
