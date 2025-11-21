/**
 * API Service - File Manager Backend Communication
 *
 * This service provides a fully typed interface for communicating with the
 * Spring Boot backend API. All methods include proper error handling,
 * request/response typing, and JSDoc documentation.
 *
 * Architecture:
 * - Uses Axios for HTTP communication
 * - Implements interceptors for error handling
 * - Provides type-safe method signatures
 * - Centralizes all API endpoint communication
 */

import axios, { type AxiosInstance, type AxiosError, type AxiosResponse } from 'axios';
import type {
  RenameRequest,
  RenameResponse,
  OrganizeRequest,
  OrganizeResponse,
  ExtractRequest,
  ExtractResponse,
  PhotoOrganizeRequest,
  PhotoOrganizeResponse,
  DuplicateRequest,
  DuplicateResponse,
  ApiError,
} from '../types';

// ============================================================================
// API Configuration
// ============================================================================

/**
 * Base Axios instance configured for the File Manager API
 */
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000, // 60 second timeout for long operations
});

// ============================================================================
// Interceptors
// ============================================================================

/**
 * Response interceptor for error handling
 * Transforms backend errors into a consistent format
 */
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError<ApiError>) => {
    // Extract error message from response or use default
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';

    // Create standardized error object
    const apiError: ApiError = {
      success: false,
      message,
      error: error.response?.data?.error,
    };

    return Promise.reject(apiError);
  }
);

// ============================================================================
// File Operation Services
// ============================================================================

/**
 * Rename files in the specified directory
 *
 * @param params - Rename operation parameters
 * @returns Promise resolving to rename operation result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await renameFiles({
 *   sourceDirectory: '/path/to/files',
 *   includeSubDirectories: true
 * });
 * console.log(`Renamed ${result.filesRenamed} files`);
 * ```
 */
export const renameFiles = async (params: RenameRequest): Promise<RenameResponse> => {
  const response = await api.post<RenameResponse>('/operations/rename', params);
  return response.data;
};

/**
 * Organize files by extension into categorized folders
 *
 * @param params - Organization parameters
 * @returns Promise resolving to organization result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await organizeFiles({
 *   sourceDirectory: '/path/to/files',
 *   destinationDirectory: '/path/to/organized'
 * });
 * console.log(`Organized ${result.filesOrganized} files`);
 * ```
 */
export const organizeFiles = async (params: OrganizeRequest): Promise<OrganizeResponse> => {
  const response = await api.post<OrganizeResponse>('/operations/organize', params);
  return response.data;
};

/**
 * Extract files from nested subdirectories
 *
 * @param params - Extraction parameters
 * @returns Promise resolving to extraction result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await extractFiles({
 *   sourceDirectory: '/path/to/nested',
 *   destinationDirectory: '/path/to/flat'
 * });
 * console.log(`Extracted ${result.filesExtracted} files`);
 * ```
 */
export const extractFiles = async (params: ExtractRequest): Promise<ExtractResponse> => {
  const response = await api.post<ExtractResponse>('/operations/extract', params);
  return response.data;
};

/**
 * Organize photos and videos by EXIF date metadata
 *
 * @param params - Photo organization parameters
 * @returns Promise resolving to photo organization result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await organizePhotos({
 *   sourceDirectory: '/path/to/photos',
 *   destinationDirectory: '/path/to/organized'
 * });
 * console.log(`Organized ${result.photosOrganized} photos`);
 * ```
 */
export const organizePhotos = async (params: PhotoOrganizeRequest): Promise<PhotoOrganizeResponse> => {
  const response = await api.post<PhotoOrganizeResponse>('/operations/photo-organize', params);
  return response.data;
};

/**
 * Find and remove duplicate files based on MD5 hash
 *
 * @param params - Duplicate finder parameters
 * @returns Promise resolving to duplicate removal result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await findDuplicates({
 *   directory: '/path/to/files'
 * });
 * console.log(`Removed ${result.duplicatesRemoved} duplicates`);
 * ```
 */
export const findDuplicates = async (params: DuplicateRequest): Promise<DuplicateResponse> => {
  const response = await api.post<DuplicateResponse>('/operations/find-duplicates', params);
  return response.data;
};

// ============================================================================
// File System Services (Future Enhancement)
// ============================================================================

/**
 * List files in a directory
 * Note: This endpoint is not yet implemented in the backend
 *
 * @param path - Directory path to list
 * @returns Promise resolving to file list
 */
export const listDirectory = async (path: string): Promise<unknown> => {
  const response = await api.get('/files/list', { params: { path } });
  return response.data;
};

/**
 * Get supported file extensions
 * Note: This endpoint is not yet implemented in the backend
 *
 * @returns Promise resolving to extension list
 */
export const getExtensions = async (): Promise<unknown> => {
  const response = await api.get('/files/extensions');
  return response.data;
};

// ============================================================================
// Default Export
// ============================================================================

/**
 * Export the configured Axios instance for custom requests
 */
export default api;
