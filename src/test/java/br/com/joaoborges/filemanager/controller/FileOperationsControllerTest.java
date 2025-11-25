package br.com.joaoborges.filemanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.joaoborges.filemanager.dto.OrganizeRequest;
import br.com.joaoborges.filemanager.dto.RenameRequest;
import br.com.joaoborges.filemanager.service.FileOperationsService;

/**
 * Integration tests for FileOperationsController
 *
 * Tests HTTP endpoints, request validation, and response formatting
 */
@WebMvcTest(FileOperationsController.class)
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
        RenameRequest request = RenameRequest.builder()
            .sourceDirectory("/test/source")
            .includeSubDirectories(true)
            .build();

        when(fileOperationsService.executeRename(any())).thenReturn(new Object());

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
        RenameRequest request = RenameRequest.builder()
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
        OrganizeRequest request = OrganizeRequest.builder()
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
        OrganizeRequest request = OrganizeRequest.builder()
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
