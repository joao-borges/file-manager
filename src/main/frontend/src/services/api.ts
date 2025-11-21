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
  GetHomeResponse,
  GetRootsResponse,
  ListDirectoryResponse,
  ValidatePathResponse,
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
// File System Browser Services
// ============================================================================

/**
 * Get user's home directory
 *
 * @returns Promise resolving to home directory path
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await getHomeDirectory();
 * console.log(`Home: ${result.path}`);
 * ```
 */
export const getHomeDirectory = async (): Promise<GetHomeResponse> => {
  const response = await api.get<GetHomeResponse>('/filesystem/home');
  return response.data;
};

/**
 * Get system roots (drives on Windows, / on Unix)
 *
 * @returns Promise resolving to list of root directories
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await getRoots();
 * console.log(`Roots: ${result.roots.map(r => r.path).join(', ')}`);
 * ```
 */
export const getRoots = async (): Promise<GetRootsResponse> => {
  const response = await api.get<GetRootsResponse>('/filesystem/roots');
  return response.data;
};

/**
 * List contents of a directory
 *
 * @param path - Directory path to list (optional, defaults to home)
 * @param includeFiles - Whether to include files (default: false, directories only)
 * @returns Promise resolving to directory contents
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await listDirectory('/home/user');
 * console.log(`Current: ${result.currentPath}`);
 * result.entries.forEach(e => console.log(e.name));
 * ```
 */
export const listDirectory = async (
  path?: string,
  includeFiles: boolean = false
): Promise<ListDirectoryResponse> => {
  const response = await api.get<ListDirectoryResponse>('/filesystem/list', {
    params: { path, includeFiles },
  });
  return response.data;
};

/**
 * Validate that a path exists and is accessible
 *
 * @param path - Path to validate
 * @returns Promise resolving to path validation result
 * @throws ApiError if the operation fails
 *
 * @example
 * ```typescript
 * const result = await validatePath('/some/path');
 * if (result.exists && result.isDirectory) {
 *   console.log('Valid directory');
 * }
 * ```
 */
export const validatePath = async (path: string): Promise<ValidatePathResponse> => {
  const response = await api.get<ValidatePathResponse>('/filesystem/validate', {
    params: { path },
  });
  return response.data;
};

// ============================================================================
// Default Export
// ============================================================================

/**
 * Export the configured Axios instance for custom requests
 */
export default api;
