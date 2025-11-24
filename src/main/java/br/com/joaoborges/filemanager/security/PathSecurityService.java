package br.com.joaoborges.filemanager.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Path Security Service
 *
 * Validates and sanitizes file system paths to prevent security vulnerabilities
 * including path traversal attacks, invalid characters, and unauthorized access.
 *
 * Security Features:
 * - Path traversal prevention (blocks "..")
 * - Invalid character detection
 * - Path normalization and canonicalization
 * - Allowed base path validation
 * - Absolute path resolution
 */
@Service
@Slf4j
public class PathSecurityService {

    /**
     * Pattern for detecting unsafe characters in paths
     * Blocks: < > : " | ? * and other control characters
     */
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[<>:\"|?*\\x00-\\x1F]");

    /**
     * Maximum path length to prevent buffer overflow attacks
     */
    private static final int MAX_PATH_LENGTH = 4096;

    /**
     * Allowed base paths for file operations
     * Can be configured via application.properties
     */
    @Value("${filemanager.allowed-paths:/tmp,${user.home}}")
    private String allowedPathsConfig;

    private List<String> allowedBasePaths;

    /**
     * Initialize allowed base paths from configuration
     */
    private void initializeAllowedPaths() {
        if (allowedBasePaths == null) {
            String userHome = System.getProperty("user.home");
            String tmpDir = System.getProperty("java.io.tmpdir");

            // Parse configured paths and add defaults
            allowedBasePaths = Arrays.stream(allowedPathsConfig.split(","))
                .map(String::trim)
                .map(p -> p.replace("${user.home}", userHome))
                .map(p -> p.replace("${tmp}", tmpDir))
                .toList();

            log.info("Initialized allowed base paths: {}", allowedBasePaths);
        }
    }

    /**
     * Validate and normalize a user-supplied path
     *
     * @param userPath The path provided by the user
     * @return Normalized and validated absolute path
     * @throws SecurityException if path is not allowed
     * @throws InvalidPathException if path format is invalid
     * @throws IllegalArgumentException if path is null or empty
     */
    public Path validateAndNormalizePath(String userPath) {
        initializeAllowedPaths();

        // Check for null/empty
        if (userPath == null || userPath.isBlank()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        // Check length
        if (userPath.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Path exceeds maximum length of %d characters", MAX_PATH_LENGTH)
            );
        }

        // Check for path traversal attempts
        if (userPath.contains("..")) {
            log.warn("Path traversal attempt detected: {}", userPath);
            throw new SecurityException("Path traversal not allowed: path contains '..'");
        }

        // Check for unsafe characters
        if (UNSAFE_CHARS.matcher(userPath).find()) {
            log.warn("Invalid characters detected in path: {}", userPath);
            throw new IllegalArgumentException(
                "Path contains invalid characters. Allowed: letters, numbers, -, _, /, \\"
            );
        }

        // Normalize and resolve to absolute path
        Path normalizedPath;
        try {
            normalizedPath = Paths.get(userPath).normalize().toAbsolutePath();
        } catch (InvalidPathException e) {
            log.warn("Invalid path format: {}", userPath, e);
            throw new IllegalArgumentException("Invalid path format: " + e.getMessage());
        }

        // Validate against allowed base paths
        boolean isAllowed = allowedBasePaths.stream()
            .anyMatch(basePath -> {
                try {
                    Path base = Paths.get(basePath).normalize().toAbsolutePath();
                    return normalizedPath.startsWith(base);
                } catch (InvalidPathException e) {
                    log.warn("Invalid base path in configuration: {}", basePath, e);
                    return false;
                }
            });

        if (!isAllowed) {
            log.warn("Access denied to path outside allowed directories: {}", normalizedPath);
            throw new SecurityException(
                "Access to path not allowed. Path must be under configured allowed directories."
            );
        }

        log.debug("Path validated successfully: {} -> {}", userPath, normalizedPath);
        return normalizedPath;
    }

    /**
     * Validate path and return as File object
     *
     * @param userPath The path provided by the user
     * @return Validated File object
     */
    public File validateAndGetFile(String userPath) {
        Path validatedPath = validateAndNormalizePath(userPath);
        return validatedPath.toFile();
    }

    /**
     * Check if a path is valid without throwing exceptions
     *
     * @param userPath The path to check
     * @return true if path is valid and allowed, false otherwise
     */
    public boolean isPathValid(String userPath) {
        try {
            validateAndNormalizePath(userPath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the canonical path string (for display purposes)
     *
     * @param userPath The user-supplied path
     * @return Canonical path string
     */
    public String getCanonicalPathString(String userPath) {
        try {
            Path validated = validateAndNormalizePath(userPath);
            return validated.toFile().getCanonicalPath();
        } catch (Exception e) {
            log.warn("Failed to get canonical path for: {}", userPath, e);
            return userPath;
        }
    }
}
