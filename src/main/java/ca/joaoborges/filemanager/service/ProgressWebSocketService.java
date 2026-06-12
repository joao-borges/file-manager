package ca.joaoborges.filemanager.service;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
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
@ConditionalOnWebApplication
public class ProgressWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send progress update to WebSocket clients
     *
     * @param operationId Unique identifier for the operation
     * @param progress Progress data to send
     */
    public void sendProgress(final String operationId, final ProgressUpdate progress) {
        try {
            final String destination = "/topic/progress/" + operationId;
            messagingTemplate.convertAndSend(destination, progress);
            log.debug("Sent progress update to {}: {}", destination, progress);
        } catch (final Exception logged) {
            log.error("Error sending progress update for operation {}", operationId, logged);
        }
    }

    /**
     * Send simple progress update with message only
     */
    public void sendProgress(final String operationId, final String message) {
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .build());
    }

    /**
     * Send progress update with percentage
     */
    public void sendProgress(final String operationId, final String message, final int percentage) {
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .percentage(percentage)
                .build());
    }

    /**
     * Send progress update with detailed information
     */
    public void sendProgress(final String operationId, final String message, final int current, final int total) {
        final int percentage = total > 0 ? (int) ((current * 100.0) / total) : 0;
        sendProgress(operationId, ProgressUpdate.builder()
                .message(message)
                .percentage(percentage)
                .current(current)
                .total(total)
                .build());
    }

    /**
     * Progress update DTO
     */
    @Data
    @Builder
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
