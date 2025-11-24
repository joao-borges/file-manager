package br.com.joaoborges.filemanager.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.joaoborges.filemanager.dto.DuplicateRequest;
import br.com.joaoborges.filemanager.dto.ExtractRequest;
import br.com.joaoborges.filemanager.dto.OrganizeRequest;
import br.com.joaoborges.filemanager.dto.PhotoOrganizeRequest;
import br.com.joaoborges.filemanager.dto.RenameRequest;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinder;
import br.com.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinderResult;
import br.com.joaoborges.filemanager.operations.extraction.ExtractionResult;
import br.com.joaoborges.filemanager.operations.extraction.Extrator;
import br.com.joaoborges.filemanager.operations.organization.OrganizationResult;
import br.com.joaoborges.filemanager.operations.organization.Organizador;
import br.com.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizator;
import br.com.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizatorResult;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.renaming.Renomeador.INCLUDE_SUB_DIRECTORIES;

/**
 * Service layer for file operations
 *
 * This service encapsulates the business logic for all file operations,
 * separating it from the controller layer. It handles:
 * - Parameter mapping from DTOs to operation-specific formats
 * - Operation execution
 * - Result transformation
 *
 * Benefits of the service layer:
 * - Single Responsibility: Controllers handle HTTP, services handle business logic
 * - Reusability: Service methods can be called from multiple controllers or scheduled tasks
 * - Testability: Business logic can be tested independently of HTTP layer
 * - Transaction Management: @Transactional can be applied at service level
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileOperationsService {

    private final Renomeador renomeador;
    private final Organizador organizador;
    private final Extrator extrator;
    private final PhotoOrganizator photoOrganizator;
    private final DuplicateFinder duplicateFinder;

    /**
     * Execute file renaming operation
     *
     * @param request Rename request parameters
     * @return Renaming operation result
     */
    public Object executeRename(RenameRequest request) {
        log.info("Executing rename operation for directory: {}", request.getSourceDirectory());

        Map<String, Object> params = new HashMap<>();
        params.put("DIRETORIO", new Diretorio(request.getSourceDirectory()));
        params.put(INCLUDE_SUB_DIRECTORIES, request.isIncludeSubDirectories());

        return renomeador.execute(params);
    }

    /**
     * Execute file organization operation
     *
     * Organizes files by extension into categorized folders.
     *
     * @param request Organization request parameters
     * @return Organization operation result
     */
    public OrganizationResult executeOrganize(OrganizeRequest request) {
        log.info("Executing organize operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Diretorio(request.getSourceDirectory()));
        params.put("DEST_DIR", new Diretorio(request.getDestinationDirectory()));
        params.put(FiltroExtensoes.class.getName(), FiltroExtensoes.allAcceptedFilter());

        return organizador.execute(params);
    }

    /**
     * Execute file extraction operation
     *
     * Recursively extracts files from nested subdirectories.
     *
     * @param request Extraction request parameters
     * @return Extraction operation result
     */
    public ExtractionResult executeExtract(ExtractRequest request) {
        log.info("Executing extract operation: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Diretorio(request.getSourceDirectory()));
        params.put("DEST_DIR", new Diretorio(request.getDestinationDirectory()));
        params.put(FiltroExtensoes.class.getName(), FiltroExtensoes.allAcceptedFilter());

        return extrator.execute(params);
    }

    /**
     * Execute photo organization operation
     *
     * Organizes photos and videos by date extracted from EXIF metadata.
     *
     * @param request Photo organization request parameters
     * @return Photo organization operation result
     */
    public PhotoOrganizatorResult executePhotoOrganization(PhotoOrganizeRequest request) {
        log.info("Executing photo organization: {} -> {}",
            request.getSourceDirectory(), request.getDestinationDirectory());

        // Create filter for photos and videos (IMAGE and VIDEO types)
        // Extensions: jpg, jpeg, png, bmp, mov, mp4, avi, wmv, mpeg, mpg
        List<String> photoAndVideoExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "bmp",  // Images
            "mov", "mp4", "avi", "wmv", "mpeg", "mpg"  // Videos
        );
        FiltroExtensoes photoFilter = new FiltroExtensoes(photoAndVideoExtensions);

        Map<String, Object> params = new HashMap<>();
        params.put("BASE_DIR", new Diretorio(request.getSourceDirectory()));
        params.put("DEST_DIR", new Diretorio(request.getDestinationDirectory()));
        params.put(FiltroExtensoes.class.getName(), photoFilter);

        return photoOrganizator.execute(params);
    }

    /**
     * Execute duplicate file finder operation
     *
     * Finds and removes duplicate files based on MD5 hash comparison.
     *
     * @param request Duplicate finder request parameters
     * @return Duplicate finder operation result
     */
    public DuplicateFinderResult executeFindDuplicates(DuplicateRequest request) {
        log.info("Executing duplicate finder for directory: {}", request.getDirectory());

        Map<String, Object> params = new HashMap<>();
        params.put(Diretorio.class.getName(), new Diretorio(request.getDirectory()));

        return duplicateFinder.execute(params);
    }
}
