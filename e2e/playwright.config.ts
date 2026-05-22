// playwright.config.ts — 로컬 8080 대상 E2E 설정 (모바일 뷰포트)
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  use: { baseURL: 'http://localhost:8080', viewport: { width: 390, height: 844 } },
});
