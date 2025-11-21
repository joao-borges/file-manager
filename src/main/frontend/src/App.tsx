/**
 * Main Application Component
 *
 * This is the root component of the File Manager application.
 * It provides a drawer-based navigation system for accessing different
 * file operations.
 *
 * Architecture:
 * - Material-UI components for consistent design
 * - Drawer navigation with operation selection
 * - Dynamic component rendering based on selected operation
 * - Responsive layout with fixed app bar and drawer
 */

import { useState, type FC } from 'react';
import {
  AppBar,
  Box,
  Container,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import {
  DriveFileRenameOutline,
  FolderOpen,
  Unarchive,
  PhotoLibrary,
  FindReplace,
} from '@mui/icons-material';

// Import operation components
import RenameOperation from './components/RenameOperation';
import OrganizeOperation from './components/OrganizeOperation';
import ExtractOperation from './components/ExtractOperation';
import PhotoOrganizationOperation from './components/PhotoOrganizationOperation';
import DuplicateFinderOperation from './components/DuplicateFinderOperation';

// Import types
import type { Operation, OperationId } from './types';

// ============================================================================
// Constants
// ============================================================================

/** Width of the side drawer in pixels */
const DRAWER_WIDTH = 280;

/**
 * Available file operations
 * Each operation has an ID, label, and icon for the navigation menu
 */
const OPERATIONS: readonly Operation[] = [
  {
    id: 'rename',
    label: 'Renomear Arquivos',
    icon: <DriveFileRenameOutline />,
    description: 'Rename files based on patterns',
  },
  {
    id: 'organize',
    label: 'Organizar Arquivos',
    icon: <FolderOpen />,
    description: 'Organize files by extension',
  },
  {
    id: 'extract',
    label: 'Extrair Arquivos',
    icon: <Unarchive />,
    description: 'Extract files from subdirectories',
  },
  {
    id: 'photo',
    label: 'Organizar Imagens por Data',
    icon: <PhotoLibrary />,
    description: 'Organize photos by EXIF date',
  },
  {
    id: 'duplicate',
    label: 'Remover Duplicados',
    icon: <FindReplace />,
    description: 'Find and remove duplicate files',
  },
] as const;

// ============================================================================
// Main Component
// ============================================================================

/**
 * App Component
 * Root component that manages navigation and operation display
 */
const App: FC = () => {
  // State management for selected operation
  const [selectedOperation, setSelectedOperation] = useState<OperationId>('rename');

  /**
   * Render the appropriate operation component based on selection
   */
  const renderOperation = (): React.ReactElement | null => {
    switch (selectedOperation) {
      case 'rename':
        return <RenameOperation />;
      case 'organize':
        return <OrganizeOperation />;
      case 'extract':
        return <ExtractOperation />;
      case 'photo':
        return <PhotoOrganizationOperation />;
      case 'duplicate':
        return <DuplicateFinderOperation />;
      default:
        // TypeScript ensures this case is never reached
        return null;
    }
  };

  return (
    <Box sx={{ display: 'flex' }}>
      {/* Top App Bar */}
      <AppBar
        position="fixed"
        sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
      >
        <Toolbar>
          <Typography variant="h6" noWrap component="div">
            Gerenciador de Arquivos
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Side Navigation Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        {/* Toolbar spacer to push content below app bar */}
        <Toolbar />

        {/* Operation List */}
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {OPERATIONS.map((operation) => (
              <ListItem key={operation.id} disablePadding>
                <ListItemButton
                  selected={selectedOperation === operation.id}
                  onClick={() => setSelectedOperation(operation.id)}
                >
                  <ListItemIcon>{operation.icon}</ListItemIcon>
                  <ListItemText
                    primary={operation.label}
                    secondary={operation.description}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Main Content Area */}
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        {/* Toolbar spacer */}
        <Toolbar />

        {/* Operation Content */}
        <Container maxWidth="xl">
          {renderOperation()}
        </Container>
      </Box>
    </Box>
  );
};

export default App;
