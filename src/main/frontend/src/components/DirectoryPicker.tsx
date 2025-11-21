/**
 * DirectoryPicker Component
 *
 * A native-like file system browser dialog that allows users to navigate
 * and select directories from the server's file system.
 *
 * Features:
 * - Directory tree navigation
 * - Breadcrumb path display
 * - Quick access to home and system roots
 * - Real-time path validation
 * - Keyboard navigation support
 */

import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  TextField,
  Breadcrumbs,
  Link,
  CircularProgress,
  Alert,
  Box,
  Typography,
  Divider,
} from '@mui/material';
import {
  Folder as FolderIcon,
  FolderOpen as FolderOpenIcon,
  Home as HomeIcon,
  Computer as ComputerIcon,
  ArrowUpward as ArrowUpwardIcon,
} from '@mui/icons-material';
import { getHomeDirectory, getRoots, listDirectory, validatePath } from '../services/api';
import type { FileSystemEntry, ListDirectoryResponse } from '../types';

/**
 * Props for DirectoryPicker component
 */
export interface DirectoryPickerProps {
  /** Whether the dialog is open */
  open: boolean;
  /** Callback when dialog is closed */
  onClose: () => void;
  /** Callback when a directory is selected */
  onSelect: (path: string) => void;
  /** Initial path to display (optional) */
  initialPath?: string;
  /** Dialog title (optional) */
  title?: string;
  /** Whether to allow manual path input (default: true) */
  allowManualInput?: boolean;
}

/**
 * DirectoryPicker Component
 *
 * Provides a dialog for browsing and selecting directories from the server's file system.
 *
 * @example
 * ```tsx
 * const [open, setOpen] = useState(false);
 * const [selectedPath, setSelectedPath] = useState('');
 *
 * <DirectoryPicker
 *   open={open}
 *   onClose={() => setOpen(false)}
 *   onSelect={(path) => {
 *     setSelectedPath(path);
 *     setOpen(false);
 *   }}
 * />
 * ```
 */
export const DirectoryPicker: React.FC<DirectoryPickerProps> = ({
  open,
  onClose,
  onSelect,
  initialPath,
  title = 'Select Directory',
  allowManualInput = true,
}) => {
  const [currentPath, setCurrentPath] = useState<string>('');
  const [entries, setEntries] = useState<FileSystemEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [manualPath, setManualPath] = useState<string>('');
  const [pathValid, setPathValid] = useState<boolean>(true);

  /**
   * Load directory contents
   */
  const loadDirectory = async (path?: string): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      const response: ListDirectoryResponse = await listDirectory(path, false);
      setCurrentPath(response.currentPath);
      setEntries(response.entries);
      setManualPath(response.currentPath);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load directory');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Load home directory
   */
  const loadHome = async (): Promise<void> => {
    try {
      const response = await getHomeDirectory();
      await loadDirectory(response.path);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load home directory');
    }
  };

  /**
   * Load system roots
   */
  const loadRoots = async (): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      const response = await getRoots();
      setCurrentPath('');
      setEntries(response.roots);
      setManualPath('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load system roots');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Validate manual path input
   */
  const validateManualPath = async (path: string): Promise<void> => {
    if (!path) {
      setPathValid(true);
      return;
    }

    try {
      const response = await validatePath(path);
      setPathValid(response.exists && response.isDirectory);
    } catch {
      setPathValid(false);
    }
  };

  /**
   * Handle directory navigation
   */
  const handleNavigate = (path: string): void => {
    loadDirectory(path);
  };

  /**
   * Handle manual path change
   */
  const handleManualPathChange = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const path = event.target.value;
    setManualPath(path);
    validateManualPath(path);
  };

  /**
   * Handle manual path submit
   */
  const handleManualPathSubmit = (): void => {
    if (pathValid && manualPath) {
      loadDirectory(manualPath);
    }
  };

  /**
   * Handle directory selection
   */
  const handleSelect = (): void => {
    onSelect(currentPath);
  };

  /**
   * Build breadcrumb path components
   */
  const buildBreadcrumbs = (): React.ReactElement[] => {
    if (!currentPath) return [];

    const parts = currentPath.split(/[/\\]/).filter(Boolean);
    const breadcrumbs: React.ReactElement[] = [];

    // Add root/home
    breadcrumbs.push(
      <Link
        key="home"
        component="button"
        variant="body2"
        onClick={loadHome}
        underline="hover"
        sx={{ cursor: 'pointer' }}
      >
        <HomeIcon sx={{ fontSize: 16, verticalAlign: 'middle', mr: 0.5 }} />
        Home
      </Link>
    );

    // Add path segments
    let accumulatedPath = '';
    parts.forEach((part, index) => {
      accumulatedPath += (index === 0 && !currentPath.startsWith('/') ? '' : '/') + part;
      const pathToNavigate = accumulatedPath;

      breadcrumbs.push(
        <Link
          key={accumulatedPath}
          component="button"
          variant="body2"
          onClick={() => handleNavigate(pathToNavigate)}
          underline="hover"
          sx={{ cursor: 'pointer' }}
        >
          {part}
        </Link>
      );
    });

    return breadcrumbs;
  };

  /**
   * Initialize directory picker
   */
  useEffect(() => {
    if (open) {
      if (initialPath) {
        loadDirectory(initialPath);
      } else {
        loadHome();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialPath]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {/* Quick Actions */}
        <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
          <Button
            size="small"
            startIcon={<HomeIcon />}
            onClick={loadHome}
            variant="outlined"
          >
            Home
          </Button>
          <Button
            size="small"
            startIcon={<ComputerIcon />}
            onClick={loadRoots}
            variant="outlined"
          >
            Roots
          </Button>
        </Box>

        {/* Breadcrumbs */}
        {currentPath && (
          <Breadcrumbs separator="›" sx={{ mb: 2 }}>
            {buildBreadcrumbs()}
          </Breadcrumbs>
        )}

        <Divider sx={{ mb: 2 }} />

        {/* Current Path Display */}
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          Current Path: <strong>{currentPath || 'System Roots'}</strong>
        </Typography>

        {/* Error Display */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {/* Directory List */}
        <Box
          sx={{
            border: 1,
            borderColor: 'divider',
            borderRadius: 1,
            maxHeight: 400,
            overflow: 'auto',
            mb: 2,
          }}
        >
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List dense>
              {entries.length === 0 ? (
                <ListItem>
                  <ListItemText
                    primary="No directories found"
                    secondary="This directory is empty or contains no accessible subdirectories"
                  />
                </ListItem>
              ) : (
                entries.map((entry) => (
                  <ListItem key={entry.path} disablePadding>
                    <ListItemButton
                      onClick={() => entry.directory && handleNavigate(entry.path)}
                      disabled={!entry.readable}
                    >
                      <ListItemIcon>
                        {entry.parent ? (
                          <ArrowUpwardIcon />
                        ) : entry.directory ? (
                          <FolderIcon />
                        ) : (
                          <FolderOpenIcon />
                        )}
                      </ListItemIcon>
                      <ListItemText
                        primary={entry.name}
                        secondary={
                          !entry.readable
                            ? 'No read permission'
                            : !entry.writable
                            ? 'Read only'
                            : undefined
                        }
                      />
                    </ListItemButton>
                  </ListItem>
                ))
              )}
            </List>
          )}
        </Box>

        {/* Manual Path Input */}
        {allowManualInput && (
          <Box>
            <TextField
              fullWidth
              label="Or enter path manually"
              value={manualPath}
              onChange={handleManualPathChange}
              onKeyPress={(e) => e.key === 'Enter' && handleManualPathSubmit()}
              error={!pathValid}
              helperText={
                !pathValid
                  ? 'Path does not exist or is not a directory'
                  : 'Press Enter to navigate'
              }
              size="small"
            />
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={handleSelect}
          variant="contained"
          disabled={!currentPath}
        >
          Select
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DirectoryPicker;
