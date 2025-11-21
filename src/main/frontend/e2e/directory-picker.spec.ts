/**
 * Directory Picker E2E Tests
 *
 * Tests the directory picker component with real filesystem API.
 */

import { test, expect } from '@playwright/test';

test.describe('Directory Picker', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should open and close directory picker', async ({ page }) => {
    // Find and click a browse button to open the picker
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      // Open the picker
      await browseButton.click();

      // Dialog should be visible
      await expect(page.getByText(/Select.*Directory/i)).toBeVisible({
        timeout: 5000,
      });

      // Close the dialog
      const cancelButton = page.getByRole('button', { name: /Cancel/i });
      await cancelButton.click();

      // Dialog should be closed
      await expect(page.getByText(/Select.*Directory/i)).not.toBeVisible({
        timeout: 3000,
      });
    }
  });

  test('should display quick access buttons', async ({ page }) => {
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      await browseButton.click();

      // Check for Home and Roots buttons
      await expect(page.getByRole('button', { name: /Home/i })).toBeVisible({
        timeout: 5000,
      });
      await expect(page.getByRole('button', { name: /Roots/i })).toBeVisible({
        timeout: 5000,
      });
    }
  });

  test('should list directory entries', async ({ page }) => {
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      await browseButton.click();

      // Wait for the directory list to load
      await page.waitForTimeout(2000);

      // Check if any directory entries are displayed
      const entries = page.locator('[role="button"]').filter({
        has: page.locator('svg'), // folder icons
      });

      // Should have at least one entry (or parent directory)
      await expect(entries.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('should allow manual path input', async ({ page }) => {
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      await browseButton.click();

      // Find the manual path input
      const pathInput = page.getByLabel(/enter path manually/i);

      if (await pathInput.isVisible()) {
        // Enter a path
        await pathInput.fill('/tmp');

        // Press Enter
        await pathInput.press('Enter');

        // Wait for the directory to load
        await page.waitForTimeout(1000);

        // Current path should update
        await expect(page.getByText(/Current Path:.*tmp/i)).toBeVisible({
          timeout: 5000,
        });
      }
    }
  });

  test('should select directory and return path', async ({ page }) => {
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      await browseButton.click();

      // Wait for dialog to open
      await page.waitForTimeout(1000);

      // Click the Select button
      const selectButton = page.getByRole('button', { name: /Select$/i });

      if (await selectButton.isVisible() && !(await selectButton.isDisabled())) {
        await selectButton.click();

        // Dialog should close
        await expect(page.getByText(/Select.*Directory/i)).not.toBeVisible({
          timeout: 3000,
        });

        // The path should be filled in the source directory input
        const sourceDirInput = page.getByLabel(/Diretório/i).first();
        const value = await sourceDirInput.inputValue();
        expect(value.length).toBeGreaterThan(0);
      }
    }
  });
});
