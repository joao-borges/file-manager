package br.com.joaoborges.filemanager.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaoborges.filemanager.dto.DuplicateRequest;
import br.com.joaoborges.filemanager.dto.ExtractRequest;
import br.com.joaoborges.filemanager.dto.OrganizeRequest;
import br.com.joaoborges.filemanager.dto.PhotoOrganizeRequest;
import br.com.joaoborges.filemanager.dto.RenameRequest;
import br.com.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinderResult;
import br.com.joaoborges.filemanager.operations.extraction.ExtractionResult;
import br.com.joaoborges.filemanager.operations.organization.OrganizationResult;
import br.com.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizatorResult;
import br.com.joaoborges.filemanager.service.FileOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for file operations
 *
 * This controller provides HTTP endpoints for various file management operations.
 * It delegates all business logic to FileOperationsService, keeping the controller
 * layer thin and focused on HTTP concerns (request/response handling).
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
    public ResponseEntity<?> renameFiles(@Valid @RequestBody RenameRequest request) {
        log.info("Rename operation requested for directory: {}", request.getSourceDirectory());

        var result = fileOperationsService.executeRename(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Arquivos renomeados com sucesso",
            "filesRenamed", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/organize")
    public ResponseEntity<?> organizeFiles(@Valid @RequestBody OrganizeRequest request) {
        log.info("Organize operation requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        OrganizationResult result = fileOperationsService.executeOrganize(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Arquivos organizados com sucesso",
            "filesOrganized", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractFiles(@Valid @RequestBody ExtractRequest request) {
        log.info("Extract operation requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        ExtractionResult result = fileOperationsService.executeExtract(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Arquivos extraídos com sucesso",
            "filesExtracted", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/photo-organize")
    public ResponseEntity<?> organizePhotos(@Valid @RequestBody PhotoOrganizeRequest request) {
        log.info("Photo organization requested: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        PhotoOrganizatorResult result = fileOperationsService.executePhotoOrganization(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Fotos organizadas com sucesso",
            "photosOrganized", result != null ? 1 : 0,
            "result", result != null ? result : Map.of()
        ));
    }

    @PostMapping("/find-duplicates")
    public ResponseEntity<?> findDuplicates(@Valid @RequestBody DuplicateRequest request) {
        log.info("Duplicate finder requested for directory: {}", request.getDirectory());

        DuplicateFinderResult result = fileOperationsService.executeFindDuplicates(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Busca por duplicados concluída",
            "duplicatesRemoved", result != null ? result.getFiles().size() : 0,
            "result", result != null ? result : Map.of()
        ));
    }
}
