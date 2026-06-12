package ca.joaoborges.filemanager.service;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles real-time progress notifications for file operations via WebSocket.
 * Allows operations to send progress updates that are broadcast to subscribed clients.
 *
 * Usage:
 * 1. Generate operation ID: String id = progressService.generateOperationId()
 * 2. Send progress: progressService.sendProgress(id, 50, "Processing files...")
 * 3. Send completion: progressService.sendCompletion(id, result)
 * 4. Send error: progressService.sendError(id, errorMessage)
 *
 * Clients subscribe to: /topic/progress/{operationId}
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnWebApplication
public class ProgressService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Generate a unique operation ID for tracking progress
     *
     * @return Unique operation ID
     */
    public String generateOperationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Send progress update to WebSocket clients
     *
     * @param operationId Unique operation identifier
     * @param percentage Progress percentage (0-100)
     * @param message Progress message
     */
    public void sendProgress(final String operationId, final int percentage, final String message) {
        final ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("in_progress")
            .percentage(percentage)
            .message(message)
            .build();

        final String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.debug("Progress update sent: {} - {}% - {}", operationId, percentage, message);
    }

    /**
     * Send completion notification to WebSocket clients
     *
     * @param operationId Unique operation identifier
     * @param result Operation result
     */
    public void sendCompletion(final String operationId, final Object result) {
        final ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("completed")
            .percentage(100)
            .message("Operation completed successfully")
            .result(result)
            .build();

        final String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.info("Completion notification sent for operation: {}", operationId);
    }

    /**
     * Send error notification to WebSocket clients
     *
     * @param operationId Unique operation identifier
     * @param errorMessage Error message
     */
    public void sendError(final String operationId, final String errorMessage) {
        final ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("error")
            .percentage(0)
            .message(errorMessage)
            .build();

        final String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.error("Error notification sent for operation {}: {}", operationId, errorMessage);
    }

    /**
     * Progress update DTO sent to WebSocket clients
     */
    @Data
    @Builder
    public static class ProgressUpdate {

        private String operationId;

        /** One of "in_progress", "completed", "error" */
        private String status;

        private int percentage;

        private String message;

        /** Only populated on completion */
        private Object result;
    }

}
