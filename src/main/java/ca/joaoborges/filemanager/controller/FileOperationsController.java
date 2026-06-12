package ca.joaoborges.filemanager.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.joaoborges.filemanager.dto.DuplicateRequest;
import ca.joaoborges.filemanager.dto.ExtractRequest;
import ca.joaoborges.filemanager.dto.OrganizeRequest;
import ca.joaoborges.filemanager.dto.PhotoOrganizeRequest;
import ca.joaoborges.filemanager.dto.RenameRequest;
import ca.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinderResult;
import ca.joaoborges.filemanager.operations.extraction.ExtractionResult;
import ca.joaoborges.filemanager.operations.organization.OrganizationResult;
import ca.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizerResult;
import ca.joaoborges.filemanager.service.FileOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for file operations.
 *
 * Provides HTTP endpoints for the file management operations and delegates all
 * business logic to FileOperationsService, keeping this layer thin and focused
 * on HTTP concerns (request/response handling).
 *
 * All endpoints:
 * - Accept validated DTOs via @Valid annotation
 * - Return standardized response format
 * - Delegate to service layer for execution
 * - Exception handling done by GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
@Slf4j
public class FileOperationsController {

    private final FileOperationsService fileOperationsService;

    @PostMapping("/rename")
    public ResponseEntity<?> renameFiles(@Valid @RequestBody final RenameRequest request) {
        log.info("Rename operation requested for directory: {}", request.getSourceDirectory());

        final var result = fileOperationsService.executeRename(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Files renamed successfully",
            "filesRenamed", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/organize")
    public ResponseEntity<?> organizeFiles(@Valid @RequestBody final OrganizeRequest request) {
        log.info("Organize operation requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        final OrganizationResult result = fileOperationsService.executeOrganize(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Files organized successfully",
            "filesOrganized", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractFiles(@Valid @RequestBody final ExtractRequest request) {
        log.info("Extract operation requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        final ExtractionResult result = fileOperationsService.executeExtract(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Files extracted successfully",
            "filesExtracted", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/photo-organize")
    public ResponseEntity<?> organizePhotos(@Valid @RequestBody final PhotoOrganizeRequest request) {
        log.info("Photo organization requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        final PhotoOrganizerResult result = fileOperationsService.executePhotoOrganization(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Photos organized successfully",
            "photosOrganized", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/find-duplicates")
    public ResponseEntity<?> findDuplicates(@Valid @RequestBody final DuplicateRequest request) {
        log.info("Duplicate finder requested for directory: {}", request.getDirectory());

        final DuplicateFinderResult result = fileOperationsService.executeFindDuplicates(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Duplicate search completed",
            "duplicatesRemoved", result != null ? result.getFiles().size() : 0,
            "result", result != null ? result : Map.of()
        ));
    }

}
