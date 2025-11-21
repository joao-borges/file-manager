/**
 * useProgress Hook
 *
 * React hook for managing WebSocket-based progress updates during file operations.
 *
 * Features:
 * - Automatic WebSocket connection management
 * - Type-safe progress updates
 * - Cleanup on unmount
 * - Easy integration with operation components
 *
 * Usage:
 * ```tsx
 * const [progress, subscribeToProgress] = useProgress();
 *
 * const handleExecute = async () => {
 *   const operationId = generateOperationId();
 *   subscribeToProgress(operationId);
 *   await executeOperation(operationId);
 * };
 *
 * // progress.percentage, progress.message, etc. are automatically updated
 * ```
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { getWebSocketClient, ProgressUpdate } from '../services/websocket';

/**
 * Progress state returned by the hook
 */
export interface ProgressState {
  message?: string;
  percentage?: number;
  current?: number;
  total?: number;
  detail?: string;
  status?: 'running' | 'completed' | 'error';
  error?: string;
  connected: boolean;
}

/**
 * useProgress Hook
 *
 * @returns [progress state, subscribe function, cleanup function]
 */
export function useProgress(): [
  ProgressState,
  (operationId: string) => void,
  () => void
] {
  const [progress, setProgress] = useState<ProgressState>({
    connected: false,
  });

  const wsClient = useRef(getWebSocketClient());
  const unsubscribeRef = useRef<(() => void) | null>(null);

  /**
   * Initialize WebSocket connection on mount
   */
  useEffect(() => {
    const client = wsClient.current;

    const connect = async () => {
      try {
        if (!client.isConnected()) {
          await client.connect();
          setProgress((prev) => ({ ...prev, connected: true }));
        }
      } catch (error) {
        console.error('[useProgress] Connection error:', error);
        setProgress((prev) => ({
          ...prev,
          connected: false,
          error: 'Failed to connect to WebSocket server',
        }));
      }
    };

    connect();

    // Cleanup on unmount
    return () => {
      if (unsubscribeRef.current) {
        unsubscribeRef.current();
      }
    };
  }, []);

  /**
   * Subscribe to progress updates for an operation
   */
  const subscribeToProgress = useCallback((operationId: string) => {
    // Unsubscribe from previous operation if any
    if (unsubscribeRef.current) {
      unsubscribeRef.current();
    }

    // Reset progress state
    setProgress({
      connected: true,
      status: 'running',
    });

    // Subscribe to new operation
    const unsubscribe = wsClient.current.subscribeToProgress(
      operationId,
      (update: ProgressUpdate) => {
        setProgress((prev) => ({
          ...prev,
          message: update.message,
          percentage: update.percentage,
          current: update.current,
          total: update.total,
          detail: update.detail,
          status: update.status || 'running',
          error: update.error,
        }));
      }
    );

    unsubscribeRef.current = unsubscribe;
  }, []);

  /**
   * Manual cleanup function
   */
  const cleanup = useCallback(() => {
    if (unsubscribeRef.current) {
      unsubscribeRef.current();
      unsubscribeRef.current = null;
    }

    setProgress({
      connected: wsClient.current.isConnected(),
    });
  }, []);

  return [progress, subscribeToProgress, cleanup];
}

/**
 * Generate a unique operation ID
 */
export function generateOperationId(): string {
  return `op-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}
