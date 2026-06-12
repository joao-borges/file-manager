package ca.joaoborges.filemanager.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.joaoborges.filemanager.dto.DuplicateRequest;
import ca.joaoborges.filemanager.dto.ExtractRequest;
import ca.joaoborges.filemanager.dto.OrganizeRequest;
import ca.joaoborges.filemanager.dto.PhotoOrganizeRequest;
import ca.joaoborges.filemanager.dto.RenameRequest;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.model.ExtensionFilter;
import ca.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinder;
import ca.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinderResult;
import ca.joaoborges.filemanager.operations.extraction.ExtractionResult;
import ca.joaoborges.filemanager.operations.extraction.Extractor;
import ca.joaoborges.filemanager.operations.organization.OrganizationResult;
import ca.joaoborges.filemanager.operations.organization.Organizer;
import ca.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizer;
import ca.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizerResult;
import ca.joaoborges.filemanager.operations.renaming.Renamer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ca.joaoborges.filemanager.operations.renaming.Renamer.INCLUDE_SUB_DIRECTORIES;

/**
 * Service layer for file operations.
 *
 * Encapsulates the business logic for all file operations, separating it from
 * the controller layer. It handles:
 * - Parameter mapping from DTOs to operation-specific formats
 * - Operation execution
 * - Result transformation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileOperationsService {

    private final Renamer renamer;

    private final Organizer organizer;

    private final Extractor extractor;

    private final PhotoOrganizer photoOrganizer;

    private final DuplicateFinder duplicateFinder;

    /**
     * Execute file renaming operation
     *
     * @param request Rename request parameters
     * @return Renaming operation result
     */
    public Object executeRename(final RenameRequest request) {
        log.info("Executing rename operation for directory: {}", request.getSourceDirectory());

        final Map<String, Object> params = new HashMap<>();
        params.put(Directory.class.getName(), new Directory(request.getSourceDirectory()));
        params.put(INCLUDE_SUB_DIRECTORIES, request.isIncludeSubDirectories());

        return renamer.execute(params);
    }

    /**
     * Execute file organization operation
     *
     * Organizes files by extension into categorized folders.
     *
     * @param request Organization request parameters
     * @return Organization operation result
     */
    public OrganizationResult executeOrganize(final OrganizeRequest request) {
        log.info("Executing organize operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        final Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Directory(request.getSourceDirectory()));
        params.put("DEST_DIR", new Directory(request.getDestinationDirectory()));
        params.put(ExtensionFilter.class.getName(), ExtensionFilter.allAcceptedFilter());

        return organizer.execute(params);
    }

    /**
     * Execute file extraction operation
     *
     * Recursively extracts files from nested subdirectories.
     *
     * @param request Extraction request parameters
     * @return Extraction operation result
     */
    public ExtractionResult executeExtract(final ExtractRequest request) {
        log.info("Executing extract operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        final Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Directory(request.getSourceDirectory()));
        params.put("DEST_DIR", new Directory(request.getDestinationDirectory()));
        params.put(ExtensionFilter.class.getName(), ExtensionFilter.allAcceptedFilter());

        return extractor.execute(params);
    }

    /**
     * Execute photo organization operation
     *
     * Organizes photos and videos by date extracted from EXIF metadata.
     *
     * @param request Photo organization request parameters
     * @return Photo organization operation result
     */
    public PhotoOrganizerResult executePhotoOrganization(final PhotoOrganizeRequest request) {
        log.info("Executing photo organization: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        // Filter for photos and videos (IMAGE and VIDEO types)
        final List<String> photoAndVideoExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "bmp",  // Images
            "mov", "mp4", "avi", "wmv", "mpeg", "mpg"  // Videos
        );
        final ExtensionFilter photoFilter = new ExtensionFilter(photoAndVideoExtensions);

        final Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Directory(request.getSourceDirectory()));
        params.put("DEST_DIR", new Directory(request.getDestinationDirectory()));
        params.put(ExtensionFilter.class.getName(), photoFilter);

        return photoOrganizer.execute(params);
    }

    /**
     * Execute duplicate file finder operation
     *
     * Finds and removes duplicate files based on MD5 hash comparison.
     *
     * @param request Duplicate finder request parameters
     * @return Duplicate finder operation result
     */
    public DuplicateFinderResult executeFindDuplicates(final DuplicateRequest request) {
        log.info("Executing duplicate finder for directory: {}", request.getDirectory());

        final Map<String, Object> params = new HashMap<>();
        params.put(Directory.class.getName(), new Directory(request.getDirectory()));

        return duplicateFinder.execute(params);
    }

    // ============================================================================
    // Async Methods
    // ============================================================================

    /**
     * Execute file renaming operation asynchronously
     *
     * @param request Rename request parameters
     * @return CompletableFuture with operation result
     */
    @Async("taskExecutor")
    public CompletableFuture<Object> executeRenameAsync(final RenameRequest request) {
        log.info("Executing async rename operation for directory: {}", request.getSourceDirectory());
        final Object result = executeRename(request);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Execute file organization operation asynchronously
     *
     * @param request Organization request parameters
     * @return CompletableFuture with operation result
     */
    @Async("taskExecutor")
    public CompletableFuture<OrganizationResult> executeOrganizeAsync(final OrganizeRequest request) {
        log.info("Executing async organize operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());
        final OrganizationResult result = executeOrganize(request);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Execute file extraction operation asynchronously
     *
     * @param request Extraction request parameters
     * @return CompletableFuture with operation result
     */
    @Async("taskExecutor")
    public CompletableFuture<ExtractionResult> executeExtractAsync(final ExtractRequest request) {
        log.info("Executing async extract operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());
        final ExtractionResult result = executeExtract(request);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Execute photo organization operation asynchronously
     *
     * @param request Photo organization request parameters
     * @return CompletableFuture with operation result
     */
    @Async("taskExecutor")
    public CompletableFuture<PhotoOrganizerResult> executePhotoOrganizationAsync(
            final PhotoOrganizeRequest request) {
        log.info("Executing async photo organization: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());
        final PhotoOrganizerResult result = executePhotoOrganization(request);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Execute duplicate file finder operation asynchronously
     *
     * @param request Duplicate finder request parameters
     * @return CompletableFuture with operation result
     */
    @Async("taskExecutor")
    public CompletableFuture<DuplicateFinderResult> executeFindDuplicatesAsync(
            final DuplicateRequest request) {
        log.info("Executing async duplicate finder for directory: {}", request.getDirectory());
        final DuplicateFinderResult result = executeFindDuplicates(request);
        return CompletableFuture.completedFuture(result);
    }

}
