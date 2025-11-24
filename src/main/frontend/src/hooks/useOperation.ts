/**
 * useOperation Hook
 *
 * A custom React hook that encapsulates the common pattern for executing
 * file operations with loading states, error handling, and result management.
 *
 * This hook reduces boilerplate code across all operation components by
 * providing a unified interface for:
 * - Loading state management
 * - Error handling and display
 * - Result state management
 * - API call execution
 *
 * @example
 * ```tsx
 * const { execute, loading, result, error, reset } = useOperation(renameFiles);
 *
 * const handleRename = async () => {
 *   await execute({
 *     sourceDirectory: '/path/to/source',
 *     includeSubDirectories: true
 *   });
 * };
 * ```
 */

import { useState, useCallback } from 'react';
import type { ApiError } from '../types';

/**
 * Generic operation result type
 */
export interface OperationResult {
  success: boolean;
  message: string;
}

/**
 * Hook return type
 */
export interface UseOperationResult<TParams, TResult extends OperationResult> {
  /**
   * Execute the operation with the given parameters
   */
  execute: (params: TParams) => Promise<TResult | null>;

  /**
   * Whether the operation is currently executing
   */
  loading: boolean;

  /**
   * The result of the last successful operation
   */
  result: TResult | null;

  /**
   * The error message from the last failed operation
   */
  error: string | null;

  /**
   * Reset all state (loading, result, error) to initial values
   */
  reset: () => void;

  /**
   * Clear the current error
   */
  clearError: () => void;
}

/**
 * Operation function type - async function that takes params and returns a result
 */
export type OperationFunction<TParams, TResult extends OperationResult> = (
  params: TParams
) => Promise<TResult>;

/**
 * Custom hook for managing file operation state and execution
 *
 * @param operationFn - The API function to execute (e.g., renameFiles, organizeFiles)
 * @param onSuccess - Optional callback to run after successful operation
 * @param onError - Optional callback to run after failed operation
 * @returns Object containing execute function, loading state, result, error, and reset functions
 */
export function useOperation<TParams = unknown, TResult extends OperationResult = OperationResult>(
  operationFn: OperationFunction<TParams, TResult>,
  onSuccess?: (result: TResult) => void,
  onError?: (error: string) => void
): UseOperationResult<TParams, TResult> {
  // Operation state
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<TResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  /**
   * Execute the operation with error handling and state management
   */
  const execute = useCallback(
    async (params: TParams): Promise<TResult | null> => {
      // Reset state before execution
      setLoading(true);
      setError(null);
      setResult(null);

      try {
        // Execute the operation
        const data = await operationFn(params);

        // Update result state
        setResult(data);

        // Call success callback if provided
        if (onSuccess) {
          onSuccess(data);
        }

        return data;
      } catch (err) {
        // Handle API errors
        const apiError = err as ApiError;
        const errorMessage = apiError.message || 'An unexpected error occurred';

        setError(errorMessage);

        // Call error callback if provided
        if (onError) {
          onError(errorMessage);
        }

        return null;
      } finally {
        setLoading(false);
      }
    },
    [operationFn, onSuccess, onError]
  );

  /**
   * Reset all state to initial values
   */
  const reset = useCallback(() => {
    setLoading(false);
    setResult(null);
    setError(null);
  }, []);

  /**
   * Clear the current error
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    execute,
    loading,
    result,
    error,
    reset,
    clearError,
  };
}
