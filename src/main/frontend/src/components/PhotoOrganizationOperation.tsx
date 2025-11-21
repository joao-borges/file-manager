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
      setError('Por favor, informe os diretórios de origem e destino');
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
      setError(apiError.message || 'Erro ao organizar fotos');
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
        Organizar Imagens por Data
      </Typography>

      <Typography variant="body2" color="text.secondary" gutterBottom>
        Organiza fotos e vídeos em pastas baseadas na data EXIF
      </Typography>

      <Paper sx={{ p: 3, mb: 3, mt: 2 }}>
        <TextField
          fullWidth
          label="Diretório de Origem"
          value={sourceDir}
          onChange={(e) => setSourceDir(e.target.value)}
          onKeyPress={handleKeyPress}
          margin="normal"
          placeholder="/caminho/para/fotos"
          disabled={loading}
          required
          helperText="Diretório contendo fotos e vídeos com metadados EXIF"
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
          helperText="Diretório onde as fotos serão organizadas por data"
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
            Operação concluída com sucesso! {result.photosOrganized || 0} foto(s) organizada(s).
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

export default PhotoOrganizationOperation;
