package ca.joaoborges.filemanager.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.joaoborges.filemanager.security.PathSecurityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
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
            final String userHome = System.getProperty("user.home");
            return ResponseEntity.ok(Map.of(
                "path", userHome,
                "success", true
            ));
        } catch (final Exception logged) {
            log.error("Error getting home directory", logged);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", logged.getMessage()
            ));
        }
    }

    /**
     * Get system roots (drives on Windows, / on Unix)
     */
    @GetMapping("/roots")
    public ResponseEntity<?> getRoots() {
        try {
            final File[] roots = File.listRoots();
            final List<FileSystemEntry> rootList = Arrays.stream(roots)
                .map((final File root) -> {
                    final FileSystemEntry entry = new FileSystemEntry();
                    entry.setPath(root.getAbsolutePath());
                    entry.setName(root.getAbsolutePath());
                    entry.setDirectory(true);
                    entry.setReadable(root.canRead());
                    entry.setWritable(root.canWrite());
                    return entry;
                })
                .toList();

            return ResponseEntity.ok(Map.of(
                "roots", rootList,
                "success", true
            ));
        } catch (final Exception logged) {
            log.error("Error getting roots", logged);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", logged.getMessage()
            ));
        }
    }

    /**
     * List contents of a directory
     */
    @GetMapping("/list")
    @Cacheable(value = "directoryListings", key = "#path + '-' + #includeFiles", unless = "#result.statusCode.is4xxClientError()")
    public ResponseEntity<?> listDirectory(
            @RequestParam(required = false) final String path,
            @RequestParam(defaultValue = "false") final boolean includeFiles) {
        try {
            // If no path provided, use user home
            final String directoryPath = path != null && !path.isEmpty()
                ? path
                : System.getProperty("user.home");

            // Validate and sanitize path to prevent security vulnerabilities
            final File directory = pathSecurityService.validateAndGetFile(directoryPath);

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

            final List<FileSystemEntry> entries = new ArrayList<>();

            // Add parent directory entry if not at root
            final File parentFile = directory.getParentFile();
            if (parentFile != null) {
                final FileSystemEntry parent = new FileSystemEntry();
                parent.setPath(parentFile.getAbsolutePath());
                parent.setName("..");
                parent.setDirectory(true);
                parent.setParent(true);
                parent.setReadable(true);
                parent.setWritable(parentFile.canWrite());
                entries.add(parent);
            }

            // Add subdirectories and optionally files
            for (final File file : files) {
                if (file.isHidden()) {
                    continue;
                }

                if (!includeFiles && !file.isDirectory()) {
                    continue;
                }

                final FileSystemEntry entry = new FileSystemEntry();
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

            // Sort: parent first, then directories, then by name
            entries.sort((final var left, final var right) -> {
                if (left.isParent()) {
                    return -1;
                }
                if (right.isParent()) {
                    return 1;
                }
                if (left.isDirectory() && !right.isDirectory()) {
                    return -1;
                }
                if (!left.isDirectory() && right.isDirectory()) {
                    return 1;
                }
                return left.getName().compareToIgnoreCase(right.getName());
            });

            return ResponseEntity.ok(Map.of(
                "currentPath", directory.getAbsolutePath(),
                "entries", entries,
                "success", true
            ));

        } catch (final Exception logged) {
            log.error("Error listing directory", logged);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", logged.getMessage()
            ));
        }
    }

    /**
     * Validate that a path exists and is accessible
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validatePath(@RequestParam final String path) {
        try {
            final File file = pathSecurityService.validateAndGetFile(path);

            return ResponseEntity.ok(Map.of(
                "exists", file.exists(),
                "isDirectory", file.isDirectory(),
                "isFile", file.isFile(),
                "canRead", file.canRead(),
                "canWrite", file.canWrite(),
                "absolutePath", file.getAbsolutePath(),
                "success", true
            ));
        } catch (final Exception logged) {
            log.error("Error validating path", logged);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", logged.getMessage()
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
