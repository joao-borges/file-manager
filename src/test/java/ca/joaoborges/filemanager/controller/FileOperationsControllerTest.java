package ca.joaoborges.filemanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import ca.joaoborges.filemanager.app.FileManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.joaoborges.filemanager.dto.OrganizeRequest;
import ca.joaoborges.filemanager.dto.RenameRequest;
import ca.joaoborges.filemanager.service.FileOperationsService;

/**
 * Integration tests for FileOperationsController
 *
 * Tests HTTP endpoints, request validation, and response formatting
 */
@WebMvcTest(FileOperationsController.class)
@ContextConfiguration(classes = FileManager.class)
@TestPropertySource(properties = "filemanager.rate-limit.requests-per-second=10000")
class FileOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileOperationsService fileOperationsService;

    @Test
    void testRenameFiles_Success() throws Exception {
        // Given
        final RenameRequest request = RenameRequest.builder()
            .sourceDirectory("/test/source")
            .includeSubDirectories(true)
            .build();

        when(fileOperationsService.executeRename(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/operations/rename")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRenameFiles_ValidationFailure_EmptyDirectory() throws Exception {
        // Given
        final RenameRequest request = RenameRequest.builder()
            .sourceDirectory("")  // Invalid - empty
            .includeSubDirectories(true)
            .build();

        // When & Then
        mockMvc.perform(post("/api/operations/rename")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testOrganizeFiles_Success() throws Exception {
        // Given
        final OrganizeRequest request = OrganizeRequest.builder()
            .sourceDirectory("/test/source")
            .destinationDirectory("/test/dest")
            .build();

        when(fileOperationsService.executeOrganize(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/operations/organize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.filesOrganized").exists());
    }

    @Test
    void testOrganizeFiles_ValidationFailure_InvalidCharacters() throws Exception {
        // Given
        final OrganizeRequest request = OrganizeRequest.builder()
            .sourceDirectory("/test/source<>")  // Invalid characters
            .destinationDirectory("/test/dest")
            .build();

        // When & Then
        mockMvc.perform(post("/api/operations/organize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testRenameFiles_WithNullBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/operations/rename")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

}
