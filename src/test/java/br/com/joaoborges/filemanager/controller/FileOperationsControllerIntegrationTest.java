package br.com.joaoborges.filemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for FileOperationsController
 *
 * Tests all file operation REST API endpoints with actual Spring Boot context
 * and temporary test directories.
 */
@SpringBootTest(classes = br.com.joaoborges.filemanager.app.FileManager.class)
@AutoConfigureMockMvc
class FileOperationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    private Path testSourceDir;
    private Path testDestDir;

    @BeforeEach
    void setUp() throws IOException {
        testSourceDir = tempDir.resolve("source");
        testDestDir = tempDir.resolve("dest");
        Files.createDirectories(testSourceDir);
        Files.createDirectories(testDestDir);
    }

    @Test
    void testRenameFiles_success() throws Exception {
        // Create test files
        createTestFile(testSourceDir, "test file 1.txt");
        createTestFile(testSourceDir, "test file 2.txt");

        Map<String, Object> request = new HashMap<>();
        request.put("sourceDirectory", testSourceDir.toString());
        request.put("includeSubDirectories", false);

        mockMvc.perform(post("/api/operations/rename")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filesRenamed").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void testRenameFiles_invalidDirectory() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceDirectory", "/nonexistent/invalid/path");
        request.put("includeSubDirectories", false);

        mockMvc.perform(post("/api/operations/rename")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testOrganizeFiles_success() throws Exception {
        // Create test files with different extensions
        createTestFile(testSourceDir, "document.txt");
        createTestFile(testSourceDir, "image.jpg");
        createTestFile(testSourceDir, "video.mp4");

        Map<String, Object> request = new HashMap<>();
        request.put("sourceDirectory", testSourceDir.toString());
        request.put("destinationDirectory", testDestDir.toString());

        mockMvc.perform(post("/api/operations/organize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filesOrganized").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void testExtractFiles_success() throws Exception {
        // Create nested directory structure
        Path subDir = testSourceDir.resolve("subdir");
        Files.createDirectories(subDir);
        createTestFile(subDir, "nested.txt");

        Map<String, Object> request = new HashMap<>();
        request.put("sourceDirectory", testSourceDir.toString());
        request.put("destinationDirectory", testDestDir.toString());

        mockMvc.perform(post("/api/operations/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filesExtracted").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void testPhotoOrganize_success() throws Exception {
        // Create test photo files (won't have EXIF but should not fail)
        createTestFile(testSourceDir, "photo1.jpg");
        createTestFile(testSourceDir, "photo2.jpg");

        Map<String, Object> request = new HashMap<>();
        request.put("sourceDirectory", testSourceDir.toString());
        request.put("destinationDirectory", testDestDir.toString());

        mockMvc.perform(post("/api/operations/photo-organize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.photosOrganized").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void testFindDuplicates_noMd5File() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("directory", testSourceDir.toString());

        // Should handle missing md5sumfiles.txt gracefully
        mockMvc.perform(post("/api/operations/find-duplicates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * Helper method to create a test file with some content
     */
    private void createTestFile(Path dir, String filename) throws IOException {
        Path file = dir.resolve(filename);
        Files.write(file, "Test content".getBytes());
    }
}
