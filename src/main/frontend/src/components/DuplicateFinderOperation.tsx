/**
 * Duplicate Finder Operation Component
 *
 * This component provides a UI for finding and removing duplicate files
 * based on MD5 hash comparison. It reads from an md5sumfiles.txt file
 * in the target directory.
 *
 * Features:
 * - MD5 hash-based duplicate detection
 * - Automatic duplicate file removal
 * - Form validation
 * - Loading states and error handling
 * - Result display
 */

import { useState, type FC } from 'react';
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Alert,
  CircularProgress,
} from '@mui/material';
import { findDuplicates } from '../services/api';
import type { DuplicateResponse, ApiError } from '../types';
import { ProgressDialog } from './ProgressDialog';

/**
 * DuplicateFinderOperation Component
 */
const DuplicateFinderOperation: FC = () => {
  // Form state
  const [directory, setDirectory] = useState<string>('');

  // Operation state
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<DuplicateResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!directory.trim()) {
      setError('Please provide the directory');
      return;
    }

    // Reset state
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Execute duplicate finder operation
      const data = await findDuplicates({
        directory,
      });

      setResult(data);
    } catch (err) {
      // Handle API errors
      const apiError = err as ApiError;
      setError(apiError.message || 'Error finding duplicates');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle Enter key press
   */
  const handleKeyPress = (event: React.KeyboardEvent): void => {
    if (event.key === 'Enter' && !loading) {
      handleExecute();
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Remove Duplicate Files
      </Typography>

      <Typography variant="body2" color="text.secondary" gutterBottom>
        Finds and removes duplicate files based on MD5 hash
      </Typography>

      <Paper sx={{ p: 3, mb: 3, mt: 2 }}>
        <TextField
          fullWidth
          label="Directory"
          value={directory}
          onChange={(e) => setDirectory(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/path/to/directory"
          helperText="The md5sumfiles.txt file must exist in the directory"
          disabled={loading}
          required
        />

        <Box sx={{ mt: 2 }}>
          <Button
            variant="contained"
            onClick={handleExecute}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : undefined}
            color="warning"
          >
            {loading ? 'Processing...' : 'Execute'}
          </Button>
        </Box>

        <Alert severity="warning" sx={{ mt: 2 }}>
          <Typography variant="body2">
            <strong>Warning:</strong> This operation will permanently remove duplicate files.
            Make sure you have a backup before continuing.
          </Typography>
        </Alert>
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {result && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Result
          </Typography>
          <Alert severity="success" sx={{ mb: 2 }}>
            Operation completed successfully! {result.duplicatesRemoved || 0} duplicate(s) removed.
          </Alert>

          {result.message && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              {result.message}
            </Typography>
          )}
        </Paper>
      )}

      <ProgressDialog
        open={loading}
        title="Finding Duplicates"
        message="Please wait while we scan for duplicate files using MD5 hashes..."
      />
    </Box>
  );
};

export default DuplicateFinderOperation;
