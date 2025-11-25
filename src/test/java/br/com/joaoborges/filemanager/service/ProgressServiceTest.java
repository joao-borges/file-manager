package br.com.joaoborges.filemanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import br.com.joaoborges.filemanager.service.ProgressService.ProgressUpdate;

/**
 * Unit tests for ProgressService
 *
 * Tests WebSocket progress notification functionality
 */
@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProgressService progressService;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<ProgressUpdate> updateCaptor;

    private String operationId;

    @BeforeEach
    void setUp() {
        operationId = progressService.generateOperationId();
    }

    @Test
    void testGenerateOperationId() {
        // When
        String id1 = progressService.generateOperationId();
        String id2 = progressService.generateOperationId();

        // Then
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2); // Should generate unique IDs
        assertTrue(id1.length() > 0);
    }

    @Test
    void testSendProgress() {
        // Given
        int percentage = 50;
        String message = "Processing files...";

        // When
        progressService.sendProgress(operationId, percentage, message);

        // Then
        verify(messagingTemplate, times(1))
            .convertAndSend(destinationCaptor.capture(), updateCaptor.capture());

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/progress/" + operationId, destination);

        ProgressUpdate update = updateCaptor.getValue();
        assertEquals(operationId, update.getOperationId());
        assertEquals("in_progress", update.getStatus());
        assertEquals(percentage, update.getPercentage());
        assertEquals(message, update.getMessage());
        assertNull(update.getResult());
    }

    @Test
    void testSendCompletion() {
        // Given
        Object result = new Object();

        // When
        progressService.sendCompletion(operationId, result);

        // Then
        verify(messagingTemplate, times(1))
            .convertAndSend(destinationCaptor.capture(), updateCaptor.capture());

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/progress/" + operationId, destination);

        ProgressUpdate update = updateCaptor.getValue();
        assertEquals(operationId, update.getOperationId());
        assertEquals("completed", update.getStatus());
        assertEquals(100, update.getPercentage());
        assertEquals("Operation completed successfully", update.getMessage());
        assertEquals(result, update.getResult());
    }

    @Test
    void testSendError() {
        // Given
        String errorMessage = "File not found";

        // When
        progressService.sendError(operationId, errorMessage);

        // Then
        verify(messagingTemplate, times(1))
            .convertAndSend(destinationCaptor.capture(), updateCaptor.capture());

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/progress/" + operationId, destination);

        ProgressUpdate update = updateCaptor.getValue();
        assertEquals(operationId, update.getOperationId());
        assertEquals("error", update.getStatus());
        assertEquals(0, update.getPercentage());
        assertEquals(errorMessage, update.getMessage());
        assertNull(update.getResult());
    }

    @Test
    void testMultipleProgressUpdates() {
        // When
        progressService.sendProgress(operationId, 25, "Step 1");
        progressService.sendProgress(operationId, 50, "Step 2");
        progressService.sendProgress(operationId, 75, "Step 3");
        progressService.sendCompletion(operationId, "Done");

        // Then
        verify(messagingTemplate, times(4)).convertAndSend(anyString(), any(ProgressUpdate.class));
    }
}
