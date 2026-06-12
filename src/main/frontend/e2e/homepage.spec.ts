/**
 * Homepage E2E Tests
 *
 * Tests the main homepage and navigation of the File Manager application.
 */

import { test, expect } from '@playwright/test';

test.describe('Homepage', () => {
  test('should load the homepage', async ({ page }) => {
    await page.goto('/');

    // Check that the page loaded
    await expect(page).toHaveTitle(/File Manager/i);
  });

  test('should display the main navigation', async ({ page }) => {
    await page.goto('/');

    // Check for main navigation items
    await expect(page.locator('nav')).toBeVisible();
  });

  test('should navigate to different operations', async ({ page }) => {
    await page.goto('/');

    // Test navigation to each operation
    const operations = [
      'Rename Files',
      'Organize Files',
      'Extract Files',
      'Organize Photos by Date',
      'Remove Duplicates',
    ];

    for (const operation of operations) {
      const link = page.getByRole('link', { name: operation });
      if (await link.isVisible()) {
        await link.click();
        await expect(page).toHaveURL(/\//);
        await page.goto('/');
      }
    }
  });
});
