/**
 * Photo Organization Operation Component
 *
 * This component provides a UI for organizing photos and videos by their
 * EXIF date metadata. Files are sorted into date-based folders automatically.
 *
 * Features:
 * - EXIF metadata extraction
 * - Date-based folder organization
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
import { organizePhotos } from '../services/api';
import type { PhotoOrganizeResponse, ApiError } from '../types';
import { ProgressDialog } from './ProgressDialog';

/**
 * PhotoOrganizationOperation Component
 */
const PhotoOrganizationOperation: FC = () => {
  // Form state
  const [sourceDir, setSourceDir] = useState<string>('');
  const [destDir, setDestDir] = useState<string>('');

  // Operation state
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<PhotoOrganizeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!sourceDir.trim() || !destDir.trim()) {
      setError('Please provide the source and destination directories');
      return;
    }

    // Reset state
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Execute photo organization operation
      const data = await organizePhotos({
        sourceDirectory: sourceDir,
        destinationDirectory: destDir,
      });

      setResult(data);
    } catch (err) {
      // Handle API errors
      const apiError = err as ApiError;
      setError(apiError.message || 'Error organizing photos');
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
        Organize Photos by Date
      </Typography>

      <Typography variant="body2" color="text.secondary" gutterBottom>
        Organizes photos and videos into folders based on EXIF date
      </Typography>

      <Paper sx={{ p: 3, mb: 3, mt: 2 }}>
        <TextField
          fullWidth
          label="Source Directory"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/path/to/photos"
          disabled={loading}
          required
          helperText="Directory containing photos and videos with EXIF metadata"
        />

        <TextField
          fullWidth
          label="Destination Directory"
          value={destDir}
          onChange={(e) => setDestDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/path/to/destination"
          disabled={loading}
          required
          helperText="Directory where photos will be organized by date"
        />

        <Box sx={{ mt: 2 }}>
          <Button
            variant="contained"
            onClick={handleExecute}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : undefined}
          >
            {loading ? 'Processing...' : 'Execute'}
          </Button>
        </Box>
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
            Operation completed successfully! {result.photosOrganized || 0} photo(s) organized.
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
        title="Organizing Photos"
        message="Please wait while we organize photos by EXIF date metadata..."
      />
    </Box>
  );
};

export default PhotoOrganizationOperation;
