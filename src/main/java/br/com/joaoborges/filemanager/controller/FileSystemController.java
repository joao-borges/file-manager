package br.com.joaoborges.filemanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaoborges.filemanager.security.PathSecurityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FileSystemController
 *
 * REST API for browsing the server's file system.
 * Provides endpoints to list directories and navigate the file system.
 */
@RestController
@RequestMapping("/api/filesystem")
@RequiredArgsConstructor
@Slf4j
public class FileSystemController {

    private final PathSecurityService pathSecurityService;

    /**
     * Get user's home directory
     */
    @GetMapping("/home")
    public ResponseEntity<?> getHomeDirectory() {
        try {
            String userHome = System.getProperty("user.home");
            return ResponseEntity.ok(Map.of(
                "path", userHome,
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error getting home directory", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get system roots (drives on Windows, / on Unix)
     */
    @GetMapping("/roots")
    public ResponseEntity<?> getRoots() {
        try {
            File[] roots = File.listRoots();
            List<FileSystemEntry> rootList = Arrays.stream(roots)
                .map(root -> {
                    FileSystemEntry entry = new FileSystemEntry();
                    entry.setPath(root.getAbsolutePath());
                    entry.setName(root.getAbsolutePath());
                    entry.setDirectory(true);
                    entry.setReadable(root.canRead());
                    entry.setWritable(root.canWrite());
                    return entry;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "roots", rootList,
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error getting roots", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * List contents of a directory
     */
    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "false") boolean includeFiles) {
        try {
            // If no path provided, use user home
            String directoryPath = path != null && !path.isEmpty()
                ? path
                : System.getProperty("user.home");

            // Validate and sanitize path to prevent security vulnerabilities
            File directory = pathSecurityService.validateAndGetFile(directoryPath);

            if (!directory.exists()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Directory does not exist: " + directoryPath
                ));
            }

            if (!directory.isDirectory()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Path is not a directory: " + directoryPath
                ));
            }

            if (!directory.canRead()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cannot read directory: " + directoryPath
                ));
            }

            File[] files = directory.listFiles();
            if (files == null) {
                files = new File[0];
            }

            List<FileSystemEntry> entries = new ArrayList<>();

            // Add parent directory entry if not at root
            File parentFile = directory.getParentFile();
            if (parentFile != null) {
                FileSystemEntry parent = new FileSystemEntry();
                parent.setPath(parentFile.getAbsolutePath());
                parent.setName("..");
                parent.setDirectory(true);
                parent.setParent(true);
                parent.setReadable(true);
                parent.setWritable(parentFile.canWrite());
                entries.add(parent);
            }

            // Add subdirectories and optionally files
            for (File file : files) {
                if (file.isHidden()) {
                    continue; // Skip hidden files
                }

                if (!includeFiles && !file.isDirectory()) {
                    continue; // Skip files if only directories requested
                }

                FileSystemEntry entry = new FileSystemEntry();
                entry.setPath(file.getAbsolutePath());
                entry.setName(file.getName());
                entry.setDirectory(file.isDirectory());
                entry.setParent(false);
                entry.setReadable(file.canRead());
                entry.setWritable(file.canWrite());

                if (!file.isDirectory()) {
                    entry.setSize(file.length());
                    entry.setLastModified(file.lastModified());
                }

                entries.add(entry);
            }

            // Sort: directories first, then by name
            entries.sort((a, b) -> {
                if (a.isParent()) return -1;
                if (b.isParent()) return 1;
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            return ResponseEntity.ok(Map.of(
                "currentPath", directory.getAbsolutePath(),
                "entries", entries,
                "success", true
            ));

        } catch (Exception e) {
            log.error("Error listing directory", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Validate that a path exists and is accessible
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validatePath(@RequestParam String path) {
        try {
            // Validate and sanitize path
            File file = pathSecurityService.validateAndGetFile(path);

            return ResponseEntity.ok(Map.of(
                "exists", file.exists(),
                "isDirectory", file.isDirectory(),
                "isFile", file.isFile(),
                "canRead", file.canRead(),
                "canWrite", file.canWrite(),
                "absolutePath", file.getAbsolutePath(),
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error validating path", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * File system entry DTO
     */
    @Data
    public static class FileSystemEntry {
        private String path;
        private String name;
        private boolean directory;
        private boolean parent;
        private boolean readable;
        private boolean writable;
        private Long size;
        private Long lastModified;
    }
}
