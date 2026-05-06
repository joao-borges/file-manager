package br.com.joaoborges.filemanager.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Oneshot CLI entry point.
 *
 * When the JVM is started with --oneshot=<json> or --oneshot-file=<path>,
 * parses the payload, dispatches to FileOperationsService, prints a JSON
 * summary to stdout, and terminates the application.
 *
 * Schema is documented in docs/oneshot-cli.md.
 */
@Component
@RequiredArgsConstructor
@Order(0)
@Slf4j
public class OneshotRunner implements ApplicationRunner {

    public static final String ARG_INLINE = "oneshot";
    public static final String ARG_FILE = "oneshot-file";

    /**
     * The original stdout, captured by FileManager.main before logging is
     * redirected to stderr. Defaults to the live System.out so unit tests and
     * non-oneshot callers still work.
     */
    private static volatile PrintStream resultStream = System.out;

    private final FileOperationsService service;
    private final Validator validator;
    private final ApplicationContext context;

    /**
     * Stash a stdout reference for the JSON result. Called from FileManager.main
     * before Spring boots so result output stays on stdout while logs go to stderr.
     */
    public static void captureStdout(PrintStream stream) {
        if (stream != null) {
            resultStream = stream;
        }
    }

    /**
     * Whether the raw program args request oneshot mode.
     * Called from FileManager.main before the Spring context starts so the
     * embedded server can be suppressed.
     */
    public static boolean isOneshot(String[] args) {
        if (args == null) {
            return false;
        }
        for (String a : args) {
            if (a == null) {
                continue;
            }
            if (a.equals("--" + ARG_INLINE) || a.startsWith("--" + ARG_INLINE + "=")
                    || a.equals("--" + ARG_FILE) || a.startsWith("--" + ARG_FILE + "=")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption(ARG_INLINE) && !args.containsOption(ARG_FILE)) {
            return;
        }

        ObjectMapper mapper = buildMapper();
        int exitCode = 0;
        try {
            String json = readPayload(args);
            JsonNode root = mapper.readTree(json);
            JsonNode opNode = root.get("operation");
            if (opNode == null || !opNode.isTextual() || opNode.asText().isBlank()) {
                throw new CliError("Missing required field: operation");
            }
            JsonNode params = root.has("params") ? root.get("params") : mapper.createObjectNode();

            Map<String, Object> output = dispatch(opNode.asText(), params, mapper);
            mapper.writeValue(resultStream, output);
            resultStream.println();
        } catch (CliError e) {
            exitCode = 1;
            writeError(mapper, e.getMessage(), null);
        } catch (Exception e) {
            exitCode = 1;
            log.error("Oneshot execution failed", e);
            writeError(mapper, e.getMessage(), e.getClass().getSimpleName());
        } finally {
            final int finalExitCode = exitCode;
            int code = SpringApplication.exit(context, () -> finalExitCode);
            System.exit(code);
        }
    }

    private String readPayload(ApplicationArguments args) throws IOException {
        boolean hasInline = args.containsOption(ARG_INLINE);
        boolean hasFile = args.containsOption(ARG_FILE);
        if (hasInline && hasFile) {
            throw new CliError("Specify either --oneshot or --oneshot-file, not both");
        }
        if (hasInline) {
            List<String> values = args.getOptionValues(ARG_INLINE);
            if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
                throw new CliError("--oneshot requires a JSON value");
            }
            return values.get(0);
        }
        List<String> files = args.getOptionValues(ARG_FILE);
        if (files == null || files.isEmpty() || files.get(0) == null || files.get(0).isBlank()) {
            throw new CliError("--oneshot-file requires a path");
        }
        Path path = Path.of(files.get(0));
        if (!Files.isReadable(path)) {
            throw new CliError("Cannot read JSON file: " + path);
        }
        return Files.readString(path);
    }

    private Map<String, Object> dispatch(String operation, JsonNode params, ObjectMapper mapper) throws IOException {
        return switch (operation) {
            case "rename" -> doRename(params, mapper);
            case "organize" -> doOrganize(params, mapper);
            case "extract" -> doExtract(params, mapper);
            case "photo-organize" -> doPhotoOrganize(params, mapper);
            case "find-duplicates" -> doFindDuplicates(params, mapper);
            default -> throw new CliError("Unknown operation: " + operation
                    + ". Valid: rename, organize, extract, photo-organize, find-duplicates");
        };
    }

    private Map<String, Object> doRename(JsonNode params, ObjectMapper mapper) throws IOException {
        RenameRequest req = mapper.treeToValue(params, RenameRequest.class);
        validate(req);
        Object result = service.executeRename(req);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", req.getSourceDirectory());
        summary.put("includeSubDirectories", req.isIncludeSubDirectories());
        summary.put("filesRenamed", reflectMapSize(result, "renamedFiles"));
        summary.put("duplicates", reflectMapSize(result, "duplicatedFiles"));
        return wrap("rename", summary, result, mapper);
    }

    private Map<String, Object> doOrganize(JsonNode params, ObjectMapper mapper) throws IOException {
        OrganizeRequest req = mapper.treeToValue(params, OrganizeRequest.class);
        validate(req);
        OrganizationResult result = service.executeOrganize(req);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", req.getSourceDirectory());
        summary.put("destination", req.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("organize", summary, result, mapper);
    }

    private Map<String, Object> doExtract(JsonNode params, ObjectMapper mapper) throws IOException {
        ExtractRequest req = mapper.treeToValue(params, ExtractRequest.class);
        validate(req);
        ExtractionResult result = service.executeExtract(req);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", req.getSourceDirectory());
        summary.put("destination", req.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("extract", summary, result, mapper);
    }

    private Map<String, Object> doPhotoOrganize(JsonNode params, ObjectMapper mapper) throws IOException {
        PhotoOrganizeRequest req = mapper.treeToValue(params, PhotoOrganizeRequest.class);
        validate(req);
        PhotoOrganizatorResult result = service.executePhotoOrganization(req);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", req.getSourceDirectory());
        summary.put("destination", req.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("photo-organize", summary, result, mapper);
    }

    private Map<String, Object> doFindDuplicates(JsonNode params, ObjectMapper mapper) throws IOException {
        DuplicateRequest req = mapper.treeToValue(params, DuplicateRequest.class);
        validate(req);
        DuplicateFinderResult result = service.executeFindDuplicates(req);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("directory", req.getDirectory());
        summary.put("duplicatesRemoved", result == null || result.getFiles() == null ? 0 : result.getFiles().size());
        return wrap("find-duplicates", summary, result, mapper);
    }

    private Map<String, Object> wrap(String operation, Map<String, Object> summary,
                                     Object result, ObjectMapper mapper) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("operation", operation);
        out.put("summary", summary);
        out.put("result", result == null ? null : mapper.convertValue(result, Object.class));
        return out;
    }

    private void writeError(ObjectMapper mapper, String message, String errorType) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("message", message == null ? "Unknown error" : message);
        if (errorType != null) {
            err.put("error", errorType);
        }
        try {
            mapper.writeValue(resultStream, err);
            resultStream.println();
        } catch (IOException ignored) {
            // best effort — error already logged
        }
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(c -> c.getPropertyPath() + ": " + c.getMessage())
                    .collect(Collectors.joining("; "));
            throw new CliError("Validation failed: " + msg);
        }
    }

    /**
     * Read a Map field from a result via reflection. Used for RenamingResult
     * fields whose getters are package-private.
     */
    private static int reflectMapSize(Object target, String fieldName) {
        if (target == null) {
            return 0;
        }
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(target);
            return value instanceof Map<?, ?> m ? m.size() : 0;
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper m = new ObjectMapper();
        // Operation result classes use a mix of public getters and private fields.
        // Field-level visibility makes the JSON output complete without forcing
        // changes to those existing classes.
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        m.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        return m;
    }

    private static class CliError extends RuntimeException {
        CliError(String message) {
            super(message);
        }
    }
}
