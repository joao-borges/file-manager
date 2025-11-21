/**
 * ProgressDialog Component
 *
 * A reusable dialog that displays operation progress with visual feedback.
 * Provides a consistent UX for long-running file operations.
 *
 * Features:
 * - Linear progress bar with indeterminate mode
 * - Operation status message
 * - Optional detailed progress (e.g., "Processing file 5 of 100")
 * - Cancel button support
 * - Non-dismissible during operation
 */

import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  LinearProgress,
  Typography,
  Box,
} from '@mui/material';

/**
 * Props for ProgressDialog component
 */
export interface ProgressDialogProps {
  /** Whether the dialog is open */
  open: boolean;
  /** Operation title (e.g., "Renaming Files") */
  title: string;
  /** Current operation status message */
  message?: string;
  /** Current progress (0-100) for determinate progress, undefined for indeterminate */
  progress?: number;
  /** Detailed progress info (e.g., "Processing file 5 of 100") */
  detail?: string;
  /** Whether the operation can be cancelled */
  cancellable?: boolean;
  /** Callback when cancel is clicked */
  onCancel?: () => void;
}

/**
 * ProgressDialog Component
 *
 * Displays a modal dialog with progress indicator for long-running operations.
 *
 * @example
 * ```tsx
 * <ProgressDialog
 *   open={loading}
 *   title="Renaming Files"
 *   message="Please wait while we process your files..."
 *   progress={45}
 *   detail="Processing file 45 of 100"
 *   cancellable={true}
 *   onCancel={() => setLoading(false)}
 * />
 * ```
 */
export const ProgressDialog: React.FC<ProgressDialogProps> = ({
  open,
  title,
  message = 'Processing...',
  progress,
  detail,
  cancellable = false,
  onCancel,
}) => {
  /**
   * Handle cancel button click
   */
  const handleCancel = (): void => {
    if (cancellable && onCancel) {
      onCancel();
    }
  };

  return (
    <Dialog
      open={open}
      disableEscapeKeyDown
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Box sx={{ width: '100%', mb: 2 }}>
          {progress !== undefined ? (
            <LinearProgress variant="determinate" value={progress} />
          ) : (
            <LinearProgress />
          )}
        </Box>

        <Typography variant="body1" gutterBottom>
          {message}
        </Typography>

        {detail && (
          <Typography variant="body2" color="text.secondary">
            {detail}
          </Typography>
        )}

        {progress !== undefined && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {Math.round(progress)}% complete
          </Typography>
        )}
      </DialogContent>

      {cancellable && (
        <DialogActions>
          <Button onClick={handleCancel} color="error">
            Cancel
          </Button>
        </DialogActions>
      )}
    </Dialog>
  );
};

export default ProgressDialog;
