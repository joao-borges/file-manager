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
      setError('Por favor, informe o diretório');
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
      setError(apiError.message || 'Erro ao buscar duplicados');
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
        Remover Arquivos Duplicados
      </Typography>

      <Typography variant="body2" color="text.secondary" gutterBottom>
        Encontra e remove arquivos duplicados baseado em hash MD5
      </Typography>

      <Paper sx={{ p: 3, mb: 3, mt: 2 }}>
        <TextField
          fullWidth
          label="Diretório"
          value={directory}
          onChange={(e) => setDirectory(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/caminho/para/diretorio"
          helperText="O arquivo md5sumfiles.txt deve existir no diretório"
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
            {loading ? 'Processando...' : 'Executar'}
          </Button>
        </Box>

        <Alert severity="warning" sx={{ mt: 2 }}>
          <Typography variant="body2">
            <strong>Atenção:</strong> Esta operação irá remover permanentemente arquivos duplicados.
            Certifique-se de ter um backup antes de continuar.
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
            Resultado
          </Typography>
          <Alert severity="success" sx={{ mb: 2 }}>
            Operação concluída com sucesso! {result.duplicatesRemoved || 0} duplicado(s) removido(s).
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

export default DuplicateFinderOperation;
