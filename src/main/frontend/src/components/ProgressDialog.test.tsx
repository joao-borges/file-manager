/**
 * ProgressDialog Component Unit Tests
 *
 * Tests the ProgressDialog component rendering and behavior.
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProgressDialog } from './ProgressDialog';

describe('ProgressDialog Component', () => {
  it('should render when open', () => {
    render(
      <ProgressDialog
        open={true}
        title="Test Operation"
        message="Processing..."
      />
    );

    expect(screen.getByText('Test Operation')).toBeInTheDocument();
    expect(screen.getByText('Processing...')).toBeInTheDocument();
  });

  it('should not render when closed', () => {
    render(
      <ProgressDialog
        open={false}
        title="Test Operation"
        message="Processing..."
      />
    );

    expect(screen.queryByText('Test Operation')).not.toBeInTheDocument();
  });

  it('should display progress percentage when provided', () => {
    render(
      <ProgressDialog
        open={true}
        title="Test Operation"
        message="Processing..."
        progress={45}
      />
    );

    expect(screen.getByText('45% complete')).toBeInTheDocument();
  });

  it('should display detail message when provided', () => {
    render(
      <ProgressDialog
        open={true}
        title="Test Operation"
        message="Processing..."
        detail="Processing file 5 of 10"
      />
    );

    expect(screen.getByText('Processing file 5 of 10')).toBeInTheDocument();
  });

  it('should show cancel button when cancellable', () => {
    const onCancel = vi.fn();

    render(
      <ProgressDialog
        open={true}
        title="Test Operation"
        message="Processing..."
        cancellable={true}
        onCancel={onCancel}
      />
    );

    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  it('should not show cancel button when not cancellable', () => {
    render(
      <ProgressDialog
        open={true}
        title="Test Operation"
        message="Processing..."
        cancellable={false}
      />
    );

    expect(screen.queryByText('Cancel')).not.toBeInTheDocument();
  });
});
