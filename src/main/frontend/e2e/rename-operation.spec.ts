/**
 * Rename Operation E2E Tests
 *
 * Tests the file renaming operation end-to-end with the backend.
 */

import { test, expect } from '@playwright/test';

test.describe('Rename Operation', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Navigate to rename operation if needed
  });

  test('should display rename operation form', async ({ page }) => {
    // Look for the form elements
    const sourceDirInput = page.getByLabel(/Source Directory/i);
    const includeSubdirsCheckbox = page.getByLabel(/Include subdirectories/i);
    const executeButton = page.getByRole('button', { name: /Execute/i });

    await expect(sourceDirInput).toBeVisible();
    await expect(includeSubdirsCheckbox).toBeVisible();
    await expect(executeButton).toBeVisible();
  });

  test('should open directory picker when browse button is clicked', async ({ page }) => {
    // Click the folder icon to open directory picker
    const browseButton = page.locator('button[title="Browse..."]').first();

    if (await browseButton.isVisible()) {
      await browseButton.click();

      // Check if directory picker dialog opened
      await expect(page.getByText(/Select Source Directory/i)).toBeVisible({
        timeout: 5000,
      });
    }
  });

  test('should validate required fields', async ({ page }) => {
    // Try to execute without filling in the source directory
    const executeButton = page.getByRole('button', { name: /Execute/i });

    if (await executeButton.isVisible()) {
      await executeButton.click();

      // Should show an error message
      await expect(page.getByText(/cannot be empty/i)).toBeVisible({
        timeout: 3000,
      });
    }
  });

  test('should show progress dialog during operation', async ({ page }) => {
    // Fill in a valid directory path
    const sourceDirInput = page.getByLabel(/Source Directory/i);

    if (await sourceDirInput.isVisible()) {
      await sourceDirInput.fill('/tmp/test');

      // Execute the operation
      const executeButton = page.getByRole('button', { name: /Execute/i });
      await executeButton.click();

      // Progress dialog should appear
      await expect(page.getByText(/Renaming Files/i)).toBeVisible({
        timeout: 5000,
      });
    }
  });
});
