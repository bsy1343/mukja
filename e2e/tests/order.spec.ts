// order.spec.ts — 카테고리→팀→주문→집계 핵심 흐름
import { test, expect } from '@playwright/test';

test('coffee order flow', async ({ page }) => {
  // 깨끗한 보드에서 시작
  await page.request.post('/coffee/sa/reset');

  await page.goto('/');
  await page.getByText('커피', { exact: false }).first().click();
  await page.getByRole('link', { name: 'SA팀' }).click();

  await page.getByPlaceholder('이름을 입력하세요').fill('테스트');

  // 메뉴 카드 → 옵션 모달 → 담기
  // 카드 클릭 시 모달은 즉시 열리고 옵션 fragment는 HTMX로 비동기 로딩되므로,
  // 모달 내용(담기 버튼)이 로드될 때까지 기다린 뒤 옵션을 선택한다.
  await page.getByText('아메리카노').click();
  const dialog = page.locator('#opt-dialog');
  const addBtn = dialog.getByRole('button', { name: '담기' });
  await expect(addBtn).toBeVisible({ timeout: 10000 });
  await dialog.getByText('HOT', { exact: true }).click();
  await addBtn.click();

  // 제출
  await page.getByRole('button', { name: /주문하기/ }).click();

  // 집계 화면에서 확인 (아메리카노는 메뉴별·사람별 2곳에 나타남 → first)
  await page.goto('/coffee/sa/status');
  await expect(page.getByText('아메리카노').first()).toBeVisible();
  await expect(page.getByText('테스트')).toBeVisible();
});
