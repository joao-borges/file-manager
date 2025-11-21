package br.com.joaoborges.filemanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for FileSystemController
 *
 * Tests the filesystem browsing REST API endpoints with actual Spring Boot context.
 */
@SpringBootTest(classes = br.com.joaoborges.filemanager.app.FileManager.class)
@AutoConfigureMockMvc
class FileSystemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetHomeDirectory() throws Exception {
        mockMvc.perform(get("/api/filesystem/home"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.path").isNotEmpty());
    }

    @Test
    void testGetRoots() throws Exception {
        mockMvc.perform(get("/api/filesystem/roots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.roots").isArray())
                .andExpect(jsonPath("$.roots", hasSize(greaterThan(0))));
    }

    @Test
    void testListDirectory_home() throws Exception {
        mockMvc.perform(get("/api/filesystem/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.currentPath").isNotEmpty())
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    void testListDirectory_withPath() throws Exception {
        // Get home directory first
        String homeDir = System.getProperty("user.home");

        mockMvc.perform(get("/api/filesystem/list")
                        .param("path", homeDir))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.currentPath").value(homeDir))
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    void testListDirectory_invalidPath() throws Exception {
        mockMvc.perform(get("/api/filesystem/list")
                        .param("path", "/nonexistent/invalid/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("does not exist")));
    }

    @Test
    void testValidatePath_validDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");

        mockMvc.perform(get("/api/filesystem/validate")
                        .param("path", homeDir))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.isDirectory").value(true))
                .andExpect(jsonPath("$.absolutePath").isNotEmpty());
    }

    @Test
    void testValidatePath_invalidPath() throws Exception {
        mockMvc.perform(get("/api/filesystem/validate")
                        .param("path", "/nonexistent/invalid/path"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.exists").value(false));
    }
}
