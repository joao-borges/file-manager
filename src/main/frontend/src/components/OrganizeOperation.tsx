/**
 * Organize Operation Component
 *
 * This component provides a UI for organizing files by extension into
 * categorized folders. Files are moved from the source directory to
 * the destination directory, grouped by their file type.
 *
 * Features:
 * - Dual directory input (source and destination)
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
  InputAdornment,
  IconButton,
} from '@mui/material';
import { FolderOpen as FolderOpenIcon } from '@mui/icons-material';
import { organizeFiles } from '../services/api';
import type { OrganizeResponse } from '../types';
import { DirectoryPicker } from './DirectoryPicker';
import { ProgressDialog } from './ProgressDialog';
import { useOperation } from '../hooks/useOperation';

/**
 * OrganizeOperation Component
 */
const OrganizeOperation: FC = () => {
  // Form state
  const [sourceDir, setSourceDir] = useState<string>('');
  const [destDir, setDestDir] = useState<string>('');

  // Directory picker state
  const [sourcePickerOpen, setSourcePickerOpen] = useState<boolean>(false);
  const [destPickerOpen, setDestPickerOpen] = useState<boolean>(false);

  // Use the custom operation hook
  const { execute, loading, result, error, clearError } = useOperation<
    { sourceDirectory: string; destinationDirectory: string },
    OrganizeResponse
  >(organizeFiles);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!sourceDir.trim() || !destDir.trim()) {
      return;
    }

    // Execute the operation using the hook
    await execute({
      sourceDirectory: sourceDir,
      destinationDirectory: destDir,
    });
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
        Organize Files
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          fullWidth
          label="Source Directory"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/path/to/source"
          disabled={loading}
          required
          helperText="Directory containing the files to organize"
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  edge="end"
                  onClick={() => setSourcePickerOpen(true)}
                  disabled={loading}
                  title="Browse..."
                >
                  <FolderOpenIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
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
          helperText="Directory where files will be organized by category"
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  edge="end"
                  onClick={() => setDestPickerOpen(true)}
                  disabled={loading}
                  title="Browse..."
                >
                  <FolderOpenIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
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
        <Alert severity="error" sx={{ mb: 2 }} onClose={clearError}>
          {error}
        </Alert>
      )}

      {result && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Result
          </Typography>
          <Alert severity="success" sx={{ mb: 2 }}>
            Operation completed successfully! {result.filesOrganized || 0} file(s) organized.
          </Alert>

          {result.message && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              {result.message}
            </Typography>
          )}
        </Paper>
      )}

      <DirectoryPicker
        open={sourcePickerOpen}
        onClose={() => setSourcePickerOpen(false)}
        onSelect={(path) => {
          setSourceDir(path);
          setSourcePickerOpen(false);
        }}
        initialPath={sourceDir}
        title="Select Source Directory"
      />

      <DirectoryPicker
        open={destPickerOpen}
        onClose={() => setDestPickerOpen(false)}
        onSelect={(path) => {
          setDestDir(path);
          setDestPickerOpen(false);
        }}
        initialPath={destDir}
        title="Select Destination Directory"
      />

      <ProgressDialog
        open={loading}
        title="Organizing Files"
        message="Please wait while we organize your files by extension..."
        detail={`From: ${sourceDir || '(not selected)'} → To: ${destDir || '(not selected)'}`}
      />
    </Box>
  );
};

export default OrganizeOperation;
