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
} from '@mui/material';
import { organizeFiles } from '../services/api';
import type { OrganizeResponse, ApiError } from '../types';

/**
 * OrganizeOperation Component
 */
const OrganizeOperation: FC = () => {
  // Form state
  const [sourceDir, setSourceDir] = useState<string>('');
  const [destDir, setDestDir] = useState<string>('');

  // Operation state
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<OrganizeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  /**
   * Handle form submission
   */
  const handleExecute = async (): Promise<void> => {
    // Validation
    if (!sourceDir.trim() || !destDir.trim()) {
      setError('Por favor, informe os diretórios de origem e destino');
      return;
    }

    // Reset state
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Execute organize operation
      const data = await organizeFiles({
        sourceDirectory: sourceDir,
        destinationDirectory: destDir,
      });

      setResult(data);
    } catch (err) {
      // Handle API errors
      const apiError = err as ApiError;
      setError(apiError.message || 'Erro ao organizar arquivos');
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
        Organizar Arquivos
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          fullWidth
          label="Diretório de Origem"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/caminho/para/origem"
          disabled={loading}
          required
          helperText="Diretório contendo os arquivos para organizar"
        />

        <TextField
          fullWidth
          label="Diretório de Destino"
          value={destDir}
          onChange={(e) => setDestDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/caminho/para/destino"
          disabled={loading}
          required
          helperText="Diretório onde os arquivos serão organizados por categoria"
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
            Operação concluída com sucesso! {result.filesOrganized || 0} arquivo(s) organizado(s).
          </Alert>

          {result.message && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              {result.message}
            </Typography>
          )}
        </Paper>
      )}
    </Box>
  );
};

export default OrganizeOperation;
