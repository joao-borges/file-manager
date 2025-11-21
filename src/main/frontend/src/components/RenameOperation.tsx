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
import type { RenameResponse, ApiError } from '../types';
import { DirectoryPicker } from './DirectoryPicker';

/**
 * RenameOperation Component
 */
const RenameOperation: FC = () => {
  // Form state
  const [sourceDir, setSourceDir] = useState<string>('');
  const [includeSubDirs, setIncludeSubDirs] = useState<boolean>(false);

  // Operation state
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<RenameResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Directory picker state
  const [pickerOpen, setPickerOpen] = useState<boolean>(false);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!sourceDir.trim()) {
      setError('Por favor, informe o diretório de origem');
      return;
    }

    // Reset state
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Execute rename operation
      const data = await renameFiles({
        sourceDirectory: sourceDir,
        includeSubDirectories: includeSubDirs,
      });

      setResult(data);
    } catch (err) {
      // Handle API errors
      const apiError = err as ApiError;
      setError(apiError.message || 'Erro ao renomear arquivos');
    } finally {
      setLoading(false);
    }
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
        Renomear Arquivos
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          fullWidth
          label="Diretório de Origem"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/caminho/para/diretorio"
          disabled={loading}
          required
          helperText="Caminho completo do diretório contendo os arquivos"
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
          label="Incluir subdiretórios"
        />

        <Box sx={{ mt: 2 }}>
          <Button
            variant="contained"
            onClick={handleExecute}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : undefined}
          >
            {loading ? 'Processando...' : 'Executar'}
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
            Resultado
          </Typography>
          <Alert severity="success" sx={{ mb: 2 }}>
            Operação concluída com sucesso! {result.filesRenamed || 0} arquivo(s) renomeado(s).
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
    </Box>
  );
};

export default RenameOperation;
