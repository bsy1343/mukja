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
