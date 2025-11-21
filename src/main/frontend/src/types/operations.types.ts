/**
 * Operation Type Definitions
 *
 * This file contains type definitions specific to the file operations
 * and UI components that manage them.
 */

import type { ReactNode } from 'react';

// ============================================================================
// Operation Identifiers
// ============================================================================

/**
 * All available file operations in the application
 */
export type OperationId = 'rename' | 'organize' | 'extract' | 'photo' | 'duplicate';

/**
 * Operation definition for UI navigation
 */
export interface Operation {
  /** Unique identifier for the operation */
  id: OperationId;
  /** Display label for the operation */
  label: string;
  /** Icon component for the operation */
  icon: ReactNode;
  /** Optional description */
  description?: string;
}

// ============================================================================
// Form Field Types
// ============================================================================

/**
 * Common form field props
 */
export interface FormFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  helperText?: string;
}

/**
 * Directory picker props
 */
export interface DirectoryPickerProps {
  label: string;
  value: string;
  onChange: (path: string) => void;
  error?: string;
  required?: boolean;
}

// ============================================================================
// Operation Component Props
// ============================================================================

/**
 * Props for operation components
 * All operation components should accept these props
 */
export interface OperationComponentProps {
  /** Optional callback when operation completes */
  onComplete?: () => void;
  /** Optional callback when operation fails */
  onError?: (error: Error) => void;
}

// ============================================================================
// Validation Types
// ============================================================================

/**
 * Form validation errors
 */
export interface ValidationErrors {
  [fieldName: string]: string | undefined;
}

/**
 * Form validation result
 */
export interface ValidationResult {
  isValid: boolean;
  errors: ValidationErrors;
}

// ============================================================================
// Progress Tracking
// ============================================================================

/**
 * Operation progress information
 */
export interface OperationProgress {
  /** Current progress percentage (0-100) */
  percentage: number;
  /** Current step description */
  currentStep: string;
  /** Total number of files to process */
  totalFiles: number;
  /** Number of files processed so far */
  processedFiles: number;
  /** Estimated time remaining in seconds */
  estimatedTimeRemaining?: number;
}
