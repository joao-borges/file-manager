/**
 * API Type Definitions for File Manager Application
 *
 * This file contains all TypeScript type definitions for API requests and responses.
 * These types ensure type safety across the frontend application when communicating
 * with the Spring Boot backend.
 */

// ============================================================================
// Base Types
// ============================================================================

/**
 * Standard API response wrapper
 * All API endpoints return this structure
 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  result?: T;
}

/**
 * Error response from API
 */
export interface ApiError {
  success: false;
  message: string;
  error?: string;
}

// ============================================================================
// Operation Request Types
// ============================================================================

/**
 * Request payload for file renaming operation
 */
export interface RenameRequest {
  /** Source directory path to rename files from */
  sourceDirectory: string;
  /** Whether to include files in subdirectories */
  includeSubDirectories: boolean;
}

/**
 * Request payload for file organization operation
 */
export interface OrganizeRequest {
  /** Source directory containing files to organize */
  sourceDirectory: string;
  /** Destination directory where organized files will be placed */
  destinationDirectory: string;
}

/**
 * Request payload for file extraction operation
 */
export interface ExtractRequest {
  /** Source directory to extract files from */
  sourceDirectory: string;
  /** Destination directory for extracted files */
  destinationDirectory: string;
}

/**
 * Request payload for photo organization operation
 */
export interface PhotoOrganizeRequest {
  /** Source directory containing photos/videos */
  sourceDirectory: string;
  /** Destination directory for organized photos */
  destinationDirectory: string;
}

/**
 * Request payload for duplicate finder operation
 */
export interface DuplicateRequest {
  /** Directory to search for duplicate files */
  directory: string;
}

// ============================================================================
// Operation Response Types
// ============================================================================

/**
 * Response from rename operation
 */
export interface RenameResponse extends ApiResponse {
  filesRenamed: number;
}

/**
 * Response from organize operation
 */
export interface OrganizeResponse extends ApiResponse {
  filesOrganized: number;
}

/**
 * Response from extract operation
 */
export interface ExtractResponse extends ApiResponse {
  filesExtracted: number;
}

/**
 * Response from photo organize operation
 */
export interface PhotoOrganizeResponse extends ApiResponse {
  photosOrganized: number;
}

/**
 * Response from duplicate finder operation
 */
export interface DuplicateResponse extends ApiResponse {
  duplicatesRemoved: number;
}

// ============================================================================
// Operation Result Types (detailed results from backend)
// ============================================================================

/**
 * Detailed result information from operations
 * This matches the Java backend Result classes
 */
export interface OperationResult {
  /** Timestamp when operation completed */
  timestamp?: string;
  /** Total files processed */
  totalFiles?: number;
  /** Files successfully processed */
  successfulFiles?: number;
  /** Files that failed processing */
  failedFiles?: number;
  /** Any error messages */
  errors?: string[];
}

// ============================================================================
// UI State Types
// ============================================================================

/**
 * Operation status for UI state management
 */
export type OperationStatus = 'idle' | 'loading' | 'success' | 'error';

/**
 * Generic operation state for UI components
 */
export interface OperationState<T = unknown> {
  status: OperationStatus;
  data?: T;
  error?: string;
}

// ============================================================================
// File System Types
// ============================================================================

/**
 * File information
 */
export interface FileInfo {
  name: string;
  path: string;
  size: number;
  extension: string;
  lastModified: Date;
}

/**
 * Directory information
 */
export interface DirectoryInfo {
  path: string;
  name: string;
  fileCount: number;
  totalSize: number;
}
