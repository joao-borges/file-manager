package br.com.joaoborges.filemanager.service;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Progress Service
 *
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
    public void sendProgress(String operationId, int percentage, String message) {
        ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("in_progress")
            .percentage(percentage)
            .message(message)
            .build();

        String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.debug("Progress update sent: {} - {}% - {}", operationId, percentage, message);
    }

    /**
     * Send completion notification to WebSocket clients
     *
     * @param operationId Unique operation identifier
     * @param result Operation result
     */
    public void sendCompletion(String operationId, Object result) {
        ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("completed")
            .percentage(100)
            .message("Operation completed successfully")
            .result(result)
            .build();

        String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.info("Completion notification sent for operation: {}", operationId);
    }

    /**
     * Send error notification to WebSocket clients
     *
     * @param operationId Unique operation identifier
     * @param errorMessage Error message
     */
    public void sendError(String operationId, String errorMessage) {
        ProgressUpdate update = ProgressUpdate.builder()
            .operationId(operationId)
            .status("error")
            .percentage(0)
            .message(errorMessage)
            .build();

        String destination = "/topic/progress/" + operationId;
        messagingTemplate.convertAndSend(destination, update);

        log.error("Error notification sent for operation {}: {}", operationId, errorMessage);
    }

    /**
     * Progress update DTO sent to WebSocket clients
     */
    @lombok.Data
    @lombok.Builder
    public static class ProgressUpdate {
        private String operationId;
        private String status;  // "in_progress", "completed", "error"
        private int percentage;
        private String message;
        private Object result;  // Only populated on completion
    }
}
