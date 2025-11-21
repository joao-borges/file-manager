package br.com.joaoborges.filemanager.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaoborges.filemanager.exception.FileManagerException;
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

@RestController
@RequestMapping("/api/operations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FileOperationsController {

    private final Renomeador renomeador;
    private final Organizador organizador;
    private final Extrator extrator;
    private final PhotoOrganizator photoOrganizator;
    private final DuplicateFinder duplicateFinder;

    @PostMapping("/rename")
    public ResponseEntity<?> renameFiles(@RequestBody RenameRequest request) {
        try {
            log.info("Rename operation requested for directory: {}", request.getSourceDirectory());

            Map<String, Object> params = new HashMap<>();
            params.put("DIRETORIO", new Diretorio(request.getSourceDirectory()));
            params.put(INCLUDE_SUB_DIRECTORIES, request.isIncludeSubDirectories());

            var result = renomeador.execute(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Arquivos renomeados com sucesso",
                "filesRenamed", result != null ? 1 : 0,
                "result", result != null ? result : Map.of()
            ));
        } catch (FileManagerException e) {
            log.error("Error renaming files", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/organize")
    public ResponseEntity<?> organizeFiles(@RequestBody OrganizeRequest request) {
        try {
            log.info("Organize operation requested: {} -> {}",
                request.getSourceDirectory(), request.getDestinationDirectory());

            Map<String, Object> params = new HashMap<>();
            params.put("BASE_DIR", new Diretorio(request.getSourceDirectory()));
            params.put("DEST_DIR", new Diretorio(request.getDestinationDirectory()));
            params.put(FiltroExtensoes.class.getName(), FiltroExtensoes.allAcceptedFilter());

            OrganizationResult result = organizador.execute(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Arquivos organizados com sucesso",
                "filesOrganized", result != null ? 1 : 0,
                "result", result != null ? result : Map.of()
            ));
        } catch (FileManagerException e) {
            log.error("Error organizing files", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractFiles(@RequestBody ExtractRequest request) {
        try {
            log.info("Extract operation requested: {} -> {}",
                request.getSourceDirectory(), request.getDestinationDirectory());

            Map<String, Object> params = new HashMap<>();
            params.put("BASE_DIR", new Diretorio(request.getSourceDirectory()));
            params.put("DEST_DIR", new Diretorio(request.getDestinationDirectory()));
            params.put(FiltroExtensoes.class.getName(), FiltroExtensoes.allAcceptedFilter());

            ExtractionResult result = extrator.execute(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Arquivos extraídos com sucesso",
                "filesExtracted", result != null ? 1 : 0,
                "result", result != null ? result : Map.of()
            ));
        } catch (FileManagerException e) {
            log.error("Error extracting files", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/photo-organize")
    public ResponseEntity<?> organizePhotos(@RequestBody PhotoOrganizeRequest request) {
        try {
            log.info("Photo organization requested: {} -> {}",
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

            PhotoOrganizatorResult result = photoOrganizator.execute(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fotos organizadas com sucesso",
                "photosOrganized", result != null ? 1 : 0,
                "result", result != null ? result : Map.of()
            ));
        } catch (FileManagerException e) {
            log.error("Error organizing photos", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/find-duplicates")
    public ResponseEntity<?> findDuplicates(@RequestBody DuplicateRequest request) {
        try {
            log.info("Duplicate finder requested for directory: {}", request.getDirectory());

            Map<String, Object> params = new HashMap<>();
            params.put(Diretorio.class.getName(), new Diretorio(request.getDirectory()));

            DuplicateFinderResult result = duplicateFinder.execute(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Busca por duplicados concluída",
                "duplicatesRemoved", result != null ? result.getFiles().size() : 0,
                "result", result != null ? result : Map.of()
            ));
        } catch (FileManagerException e) {
            log.error("Error finding duplicates", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // Request DTOs
    public static class RenameRequest {
        private String sourceDirectory;
        private boolean includeSubDirectories;

        public String getSourceDirectory() {
            return sourceDirectory;
        }

        public void setSourceDirectory(String sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
        }

        public boolean isIncludeSubDirectories() {
            return includeSubDirectories;
        }

        public void setIncludeSubDirectories(boolean includeSubDirectories) {
            this.includeSubDirectories = includeSubDirectories;
        }
    }

    public static class OrganizeRequest {
        private String sourceDirectory;
        private String destinationDirectory;

        public String getSourceDirectory() {
            return sourceDirectory;
        }

        public void setSourceDirectory(String sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
        }

        public String getDestinationDirectory() {
            return destinationDirectory;
        }

        public void setDestinationDirectory(String destinationDirectory) {
            this.destinationDirectory = destinationDirectory;
        }
    }

    public static class ExtractRequest {
        private String sourceDirectory;
        private String destinationDirectory;

        public String getSourceDirectory() {
            return sourceDirectory;
        }

        public void setSourceDirectory(String sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
        }

        public String getDestinationDirectory() {
            return destinationDirectory;
        }

        public void setDestinationDirectory(String destinationDirectory) {
            this.destinationDirectory = destinationDirectory;
        }
    }

    public static class PhotoOrganizeRequest {
        private String sourceDirectory;
        private String destinationDirectory;

        public String getSourceDirectory() {
            return sourceDirectory;
        }

        public void setSourceDirectory(String sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
        }

        public String getDestinationDirectory() {
            return destinationDirectory;
        }

        public void setDestinationDirectory(String destinationDirectory) {
            this.destinationDirectory = destinationDirectory;
        }
    }

    public static class DuplicateRequest {
        private String directory;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }
}
