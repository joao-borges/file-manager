package ca.joaoborges.filemanager.cli;

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
    public static void captureStdout(final PrintStream stream) {
        if (stream != null) {
            resultStream = stream;
        }
    }

    /**
     * Whether the raw program args request oneshot mode.
     * Called from FileManager.main before the Spring context starts so the
     * embedded server can be suppressed.
     */
    public static boolean isOneshot(final String[] args) {
        if (args == null) {
            return false;
        }
        for (final String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.equals("--" + ARG_INLINE) || arg.startsWith("--" + ARG_INLINE + "=")
                    || arg.equals("--" + ARG_FILE) || arg.startsWith("--" + ARG_FILE + "=")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(final ApplicationArguments args) {
        if (!args.containsOption(ARG_INLINE) && !args.containsOption(ARG_FILE)) {
            return;
        }

        final ObjectMapper mapper = buildMapper();
        int exitCode = 0;
        try {
            final String json = readPayload(args);
            final JsonNode root = mapper.readTree(json);
            final JsonNode opNode = root.get("operation");
            if (opNode == null || !opNode.isTextual() || opNode.asText().isBlank()) {
                throw new CliError("Missing required field: operation");
            }
            final JsonNode params = root.has("params") ? root.get("params") : mapper.createObjectNode();

            final Map<String, Object> output = dispatch(opNode.asText(), params, mapper);
            mapper.writeValue(resultStream, output);
            resultStream.println();
        } catch (final CliError reported) {
            exitCode = 1;
            writeError(mapper, reported.getMessage(), null);
        } catch (final Exception logged) {
            exitCode = 1;
            log.error("Oneshot execution failed", logged);
            writeError(mapper, logged.getMessage(), logged.getClass().getSimpleName());
        } finally {
            final int finalExitCode = exitCode;
            final int code = SpringApplication.exit(context, () -> {
                return finalExitCode;
            });
            System.exit(code);
        }
    }

    private String readPayload(final ApplicationArguments args) throws IOException {
        final boolean hasInline = args.containsOption(ARG_INLINE);
        final boolean hasFile = args.containsOption(ARG_FILE);
        if (hasInline && hasFile) {
            throw new CliError("Specify either --oneshot or --oneshot-file, not both");
        }
        if (hasInline) {
            final List<String> values = args.getOptionValues(ARG_INLINE);
            if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
                throw new CliError("--oneshot requires a JSON value");
            }
            return values.get(0);
        }
        final List<String> files = args.getOptionValues(ARG_FILE);
        if (files == null || files.isEmpty() || files.get(0) == null || files.get(0).isBlank()) {
            throw new CliError("--oneshot-file requires a path");
        }
        final Path path = Path.of(files.get(0));
        if (!Files.isReadable(path)) {
            throw new CliError("Cannot read JSON file: " + path);
        }
        return Files.readString(path);
    }

    private Map<String, Object> dispatch(final String operation, final JsonNode params,
                                         final ObjectMapper mapper) throws IOException {
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

    private Map<String, Object> doRename(final JsonNode params, final ObjectMapper mapper) throws IOException {
        final RenameRequest request = mapper.treeToValue(params, RenameRequest.class);
        validate(request);
        final Object result = service.executeRename(request);
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", request.getSourceDirectory());
        summary.put("includeSubDirectories", request.isIncludeSubDirectories());
        summary.put("filesRenamed", reflectMapSize(result, "renamedFiles"));
        summary.put("duplicates", reflectMapSize(result, "duplicatedFiles"));
        return wrap("rename", summary, result, mapper);
    }

    private Map<String, Object> doOrganize(final JsonNode params, final ObjectMapper mapper) throws IOException {
        final OrganizeRequest request = mapper.treeToValue(params, OrganizeRequest.class);
        validate(request);
        final OrganizationResult result = service.executeOrganize(request);
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", request.getSourceDirectory());
        summary.put("destination", request.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("organize", summary, result, mapper);
    }

    private Map<String, Object> doExtract(final JsonNode params, final ObjectMapper mapper) throws IOException {
        final ExtractRequest request = mapper.treeToValue(params, ExtractRequest.class);
        validate(request);
        final ExtractionResult result = service.executeExtract(request);
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", request.getSourceDirectory());
        summary.put("destination", request.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("extract", summary, result, mapper);
    }

    private Map<String, Object> doPhotoOrganize(final JsonNode params, final ObjectMapper mapper) throws IOException {
        final PhotoOrganizeRequest request = mapper.treeToValue(params, PhotoOrganizeRequest.class);
        validate(request);
        final PhotoOrganizerResult result = service.executePhotoOrganization(request);
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source", request.getSourceDirectory());
        summary.put("destination", request.getDestinationDirectory());
        summary.put("filesMoved", result == null || result.getMovedFiles() == null ? 0 : result.getMovedFiles().size());
        return wrap("photo-organize", summary, result, mapper);
    }

    private Map<String, Object> doFindDuplicates(final JsonNode params, final ObjectMapper mapper) throws IOException {
        final DuplicateRequest request = mapper.treeToValue(params, DuplicateRequest.class);
        validate(request);
        final DuplicateFinderResult result = service.executeFindDuplicates(request);
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("directory", request.getDirectory());
        summary.put("duplicatesRemoved", result == null || result.getFiles() == null ? 0 : result.getFiles().size());
        return wrap("find-duplicates", summary, result, mapper);
    }

    private Map<String, Object> wrap(final String operation, final Map<String, Object> summary,
                                     final Object result, final ObjectMapper mapper) {
        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("operation", operation);
        out.put("summary", summary);
        out.put("result", result == null ? null : mapper.convertValue(result, Object.class));
        return out;
    }

    private void writeError(final ObjectMapper mapper, final String message, final String errorType) {
        final Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message == null ? "Unknown error" : message);
        if (errorType != null) {
            error.put("error", errorType);
        }
        try {
            mapper.writeValue(resultStream, error);
            resultStream.println();
        } catch (final IOException ignored) {
            // best effort — error already logged
        }
    }

    private <T> void validate(final T request) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            final String message = violations.stream()
                    .map((final var violation) -> {
                        return violation.getPropertyPath() + ": " + violation.getMessage();
                    })
                    .collect(Collectors.joining("; "));
            throw new CliError("Validation failed: " + message);
        }
    }

    /**
     * Read a Map field from a result via reflection. Used for RenamingResult
     * fields whose getters are package-private.
     */
    private static int reflectMapSize(final Object target, final String fieldName) {
        if (target == null) {
            return 0;
        }
        try {
            final var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            final Object value = field.get(target);
            return value instanceof final Map<?, ?> mapValue ? mapValue.size() : 0;
        } catch (final ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static ObjectMapper buildMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        // Operation result classes use a mix of public getters and private fields.
        // Field-level visibility makes the JSON output complete without forcing
        // changes to those existing classes.
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    private static class CliError extends RuntimeException {

        CliError(final String message) {
            super(message);
        }
    }

}
