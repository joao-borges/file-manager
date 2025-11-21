package br.com.joaoborges.filemanager.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Progress WebSocket Service
 *
 * Sends real-time progress updates to WebSocket clients during file operations.
 * Uses Spring's SimpMessagingTemplate to broadcast messages to subscribed clients.
 *
 * Usage:
 * - Inject this service into operation classes
 * - Call sendProgress() during operation execution
 * - Clients receive updates on /topic/progress/{operationId}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send progress update to WebSocket clients
     *
     * @param operationId Unique identifier for the operation
     * @param progress Progress data to send
     */
    public void sendProgress(String operationId, ProgressUpdate progress) {
        try {
            String destination = "/topic/progress/" + operationId;
            messagingTemplate.convertAndSend(destination, progress);
            log.debug("Sent progress update to {}: {}", destination, progress);
        } catch (Exception e) {
            log.error("Error sending progress update for operation {}", operationId, e);
        }
    }

    /**
     * Send simple progress update with message only
     */
    public void sendProgress(String operationId, String message) {
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .build());
    }

    /**
     * Send progress update with percentage
     */
    public void sendProgress(String operationId, String message, int percentage) {
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .percentage(percentage)
                .build());
    }

    /**
     * Send progress update with detailed information
     */
    public void sendProgress(String operationId, String message, int current, int total) {
        int percentage = total > 0 ? (int) ((current * 100.0) / total) : 0;
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .percentage(percentage)
                .current(current)
                .total(total)
                .build());
    }

    /**
     * Progress Update DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ProgressUpdate {
        /** Progress message */
        private String message;

        /** Progress percentage (0-100) */
        private Integer percentage;

        /** Current item being processed */
        private Integer current;

        /** Total items to process */
        private Integer total;

        /** Additional details */
        private String detail;

        /** Operation status (running, completed, error) */
        private String status;

        /** Error message if status is error */
        private String error;

        /** Additional metadata */
        private Map<String, Object> metadata;
    }
}
