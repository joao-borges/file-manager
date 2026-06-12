/**
 * Rename Operation Component
 *
 * This component provides a UI for the file renaming operation.
 * Users can specify a source directory and optionally include subdirectories.
 *
 * Features:
 * - Form validation
 * - Loading states
 * - Error handling
 * - Result display with file details
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
  Checkbox,
  FormControlLabel,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { FolderOpen as FolderOpenIcon } from '@mui/icons-material';
import { renameFiles } from '../services/api';
import type { RenameResponse } from '../types';
import { DirectoryPicker } from './DirectoryPicker';
import { ProgressDialog } from './ProgressDialog';
import { useOperation } from '../hooks/useOperation';

/**
 * RenameOperation Component
 */
const RenameOperation: FC = () => {
  // Form state
  const [sourceDir, setSourceDir] = useState<string>('');
  const [includeSubDirs, setIncludeSubDirs] = useState<boolean>(false);

  // Directory picker state
  const [pickerOpen, setPickerOpen] = useState<boolean>(false);

  // Use the custom operation hook
  const { execute, loading, result, error, clearError } = useOperation<
    { sourceDirectory: string; includeSubDirectories: boolean },
    RenameResponse
  >(renameFiles);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!sourceDir.trim()) {
      return;
    }

    // Execute the operation using the hook
    await execute({
      sourceDirectory: sourceDir,
      includeSubDirectories: includeSubDirs,
    });
  };

  /**
   * Handle Enter key press in form
   */
  const handleKeyPress = (event: React.KeyboardEvent): void => {
    if (event.key === 'Enter' && !loading) {
      handleExecute();
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Rename Files
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          fullWidth
          label="Source Directory"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/path/to/directory"
          disabled={loading}
          required
          helperText="Full path of the directory containing the files"
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  edge="end"
                  onClick={() => setPickerOpen(true)}
                  disabled={loading}
                  title="Browse..."
                >
                  <FolderOpenIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
        />

        <FormControlLabel
          control={
            <Checkbox
              checked={includeSubDirs}
              onChange={(e) => setIncludeSubDirs(e.target.checked)}
              disabled={loading}
            />
          }
          label="Include subdirectories"
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
            Operation completed successfully! {result.filesRenamed || 0} file(s) renamed.
          </Alert>

          {result.message && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              {result.message}
            </Typography>
          )}
        </Paper>
      )}

      <DirectoryPicker
        open={pickerOpen}
        onClose={() => setPickerOpen(false)}
        onSelect={(path) => {
          setSourceDir(path);
          setPickerOpen(false);
        }}
        initialPath={sourceDir}
        title="Select Source Directory"
      />

      <ProgressDialog
        open={loading}
        title="Renaming Files"
        message="Please wait while we rename your files..."
        detail={includeSubDirs ? 'Including subdirectories' : 'Current directory only'}
      />
    </Box>
  );
};

export default RenameOperation;
