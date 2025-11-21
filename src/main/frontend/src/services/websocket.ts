/**
 * WebSocket Service for Real-Time Progress Updates
 *
 * This service provides WebSocket connectivity for receiving real-time progress
 * updates from the backend during long-running file operations.
 *
 * Architecture:
 * - Uses STOMP protocol over WebSocket
 * - Connects to Spring Boot WebSocket endpoint
 * - Subscribes to operation-specific progress topics
 * - Provides TypeScript-safe progress update handling
 *
 * Dependencies:
 * - @stomp/stompjs - STOMP client library
 * - sockjs-client - SockJS fallback for browsers without WebSocket
 */

import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Progress update from backend
 */
export interface ProgressUpdate {
  message?: string;
  percentage?: number;
  current?: number;
  total?: number;
  detail?: string;
  status?: 'running' | 'completed' | 'error';
  error?: string;
  metadata?: Record<string, unknown>;
}

/**
 * WebSocket client for progress updates
 */
export class ProgressWebSocketClient {
  private client: Client | null = null;
  private connected: boolean = false;
  private subscriptions: Map<string, () => void> = new Map();

  /**
   * Connect to WebSocket server
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected) {
        resolve();
        return;
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws') as WebSocket,
        debug: (str) => {
          console.log('[WebSocket]', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = () => {
        console.log('[WebSocket] Connected');
        this.connected = true;
        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error('[WebSocket] STOMP error:', frame);
        reject(new Error(`STOMP error: ${frame.headers['message']}`));
      };

      this.client.onWebSocketError = (event) => {
        console.error('[WebSocket] WebSocket error:', event);
        reject(new Error('WebSocket connection error'));
      };

      this.client.activate();
    });
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.client) {
      this.subscriptions.clear();
      this.client.deactivate();
      this.connected = false;
      console.log('[WebSocket] Disconnected');
    }
  }

  /**
   * Subscribe to progress updates for an operation
   *
   * @param operationId Unique operation identifier
   * @param callback Callback function for progress updates
   * @returns Unsubscribe function
   */
  subscribeToProgress(
    operationId: string,
    callback: (progress: ProgressUpdate) => void
  ): () => void {
    if (!this.client || !this.connected) {
      console.warn('[WebSocket] Cannot subscribe: not connected');
      return () => {};
    }

    const destination = `/topic/progress/${operationId}`;

    const subscription = this.client.subscribe(destination, (message: IMessage) => {
      try {
        const progress: ProgressUpdate = JSON.parse(message.body);
        callback(progress);
      } catch (error) {
        console.error('[WebSocket] Error parsing progress update:', error);
      }
    });

    // Create unsubscribe function
    const unsubscribe = () => {
      subscription.unsubscribe();
      this.subscriptions.delete(operationId);
      console.log(`[WebSocket] Unsubscribed from ${destination}`);
    };

    this.subscriptions.set(operationId, unsubscribe);
    console.log(`[WebSocket] Subscribed to ${destination}`);

    return unsubscribe;
  }

  /**
   * Check if client is connected
   */
  isConnected(): boolean {
    return this.connected;
  }
}

/**
 * Singleton WebSocket client instance
 */
let globalClient: ProgressWebSocketClient | null = null;

/**
 * Get or create global WebSocket client
 */
export function getWebSocketClient(): ProgressWebSocketClient {
  if (!globalClient) {
    globalClient = new ProgressWebSocketClient();
  }
  return globalClient;
}

/**
 * Clean up global WebSocket client
 */
export function cleanupWebSocketClient(): void {
  if (globalClient) {
    globalClient.disconnect();
    globalClient = null;
  }
}
