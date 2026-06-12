# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A full-stack file management application with a Spring Boot REST backend and a React + TypeScript frontend. The backend exposes file operations (rename, organize, extract, photo organization, duplicate detection) over HTTP; the frontend at `src/main/frontend` calls those endpoints and renders results.

The application also supports a **oneshot CLI mode** for scripting: a single invocation accepts a JSON payload, runs one operation, prints a JSON summary, and exits. See `docs/oneshot-cli.md` for the JSON schema.

## Build and Run

**Build the project (compiles Java + builds frontend):**
```bash
./mvnw clean package
```

**Run the application (web mode):**
```bash
./mvnw spring-boot:run
```
Frontend at http://localhost:8080, REST API at http://localhost:8080/api/*.

**Run the application (oneshot CLI mode):**
```bash
java -jar target/file-manager-*.jar --oneshot='{"operation":"organize","params":{"sourceDirectory":"/in","destinationDirectory":"/out"}}'
java -jar target/file-manager-*.jar --oneshot-file=/path/to/payload.json
```
In oneshot mode the embedded web server is suppressed; the JVM exits with code 0 on success, 1 on error.

**Run tests:**
```bash
./mvnw test
```

**Frontend dev server (HMR):**
```bash
cd src/main/frontend && npm run dev
```

## Technology Stack

- Java 21, Spring Boot 3.5.8 (Web, Validation, WebSocket, Cache)
- React 18 + TypeScript + Vite 6 (Material-UI, SockJS/STOMP)
- Maven (with `frontend-maven-plugin` to build the React app)
- Lombok, Jackson, Apache Commons (IO, Text), Guava (rate limiting), Caffeine (cache)
- metadata-extractor / commons-imaging / jaudiotagger for media metadata

## Architecture

### Layered backend

```
Controller (controller/)         — HTTP boundary, @Valid DTOs, thin
       ↓
Service (service/)               — FileOperationsService delegates to operations
       ↓
Operation (operations/<name>/)   — Renamer, Organizer, Extractor,
                                   PhotoOrganizer, DuplicateFinder
```

Each operation implements `FileOperation<R extends OperationResult>` (`operations/interfaces/`) with a single `execute(Map<String, Object> params)` method. The service layer translates DTOs into the parameter `Map` each operation expects.

### Entry points

- `app/FileManager.java` — Spring Boot main class. Detects oneshot args (`--oneshot=` / `--oneshot-file=`) early and switches `WebApplicationType` to `NONE` before the context starts, so no port is bound.
- `cli/OneshotRunner.java` — `ApplicationRunner` that runs only when oneshot args are present. Parses JSON, dispatches to `FileOperationsService`, prints a JSON summary to stdout, and exits via `SpringApplication.exit`.
- `controller/FileOperationsController` — REST endpoints (`POST /api/operations/{rename|organize|extract|photo-organize|find-duplicates}`).
- `controller/FileSystemController` — directory listing endpoints (`GET /api/filesystem/*`).

CLI and REST share the same DTOs (`dto/*Request.java`) and the same `FileOperationsService`, so validation and behavior stay in sync between the two.

### Operation summary

| Operation        | ID constant                      | DTO                    | Result                      |
| ---------------- | -------------------------------- | ---------------------- | --------------------------- |
| Rename           | `RENAME_OPERATION`               | `RenameRequest`        | `Renamer.RenamingResult` |
| Organize         | `ORGANIZATION_OPERATION`         | `OrganizeRequest`      | `OrganizationResult`        |
| Extract          | `EXTRACTION_OPERATION`           | `ExtractRequest`       | `ExtractionResult`          |
| Photo Organize   | `PHOTO_ORGANIZATION_OPERATION`   | `PhotoOrganizeRequest` | `PhotoOrganizerResult`    |
| Find Duplicates  | `DUPLICATE_FINDER_OPERATION`     | `DuplicateRequest`     | `DuplicateFinderResult`     |

Constants live in `operations/common/OperationConstants`. Each operation is a Spring bean named after its ID constant.

### Cross-cutting

- `config/AsyncConfig` — `taskExecutor` thread pool for `@Async` service methods.
- `config/CacheConfig` — Caffeine caches for directory listings and path validations.
- `config/WebMvcConfig` + `config/RateLimitingInterceptor` — Guava-based per-IP rate limiting on `/api/**` (web mode only).
- `config/WebSocketConfig` — STOMP/SockJS at `/ws` for progress events (web mode only; gated by `@ConditionalOnWebApplication`).
- `config/WebSecurityConfig` — CORS (web mode only).
- `security/PathSecurityService` — path traversal guard, allowed-base-path enforcement.
- `exception/GlobalExceptionHandler` — `@RestControllerAdvice` mapping exceptions to JSON responses (HTTP only; the CLI handles errors itself).

### Configuration files

- `src/main/resources/application.yml` — server port, allowed paths, CORS origins, log levels.
- `src/main/resources/exclusions.xml` — exclusion rules for the rename operation.
- `src/main/resources/ca/joaoborges/filemanager/resources/` — `Extensions.properties`, `ExtensionGroups.properties`, `RegexesToFilter.properties`, `StringsToFilter.properties`.

## Common Patterns

**Adding a new operation:**
1. Implement `FileOperation<YourResult>` under `operations/<name>/`, register as a Spring bean with the operation's ID constant.
2. Create a `YourResult implements OperationResult`.
3. Add a `YourRequest` DTO under `dto/` with Jakarta Validation annotations.
4. Add an `executeYourOp(YourRequest)` method to `FileOperationsService` that maps the DTO into the operation's `Map<String, Object>` params.
5. Wire it into `FileOperationsController` (REST endpoint) and into `OneshotRunner.dispatch` (CLI). Document the JSON shape in `docs/oneshot-cli.md`.

**Spring bean naming for operations:**
- Operation beans use their ID constant (e.g. `@Service(value = OperationConstants.RENAME_OPERATION)`).

## Code Style

The entire codebase (Java and the TypeScript in `src/main/frontend`) is English-only — identifiers, comments, log/UI strings, resource bundles. Keep it that way. One deliberate exception: `PhotoOrganizer` writes Portuguese month folder names (`Locale.of("pt", "BR")`, fallback folder `outros`) because users' existing photo trees were organized with those names — do NOT translate them. Formatting and conventions follow the operator's standard Java style:

- **Braces always, multi-line — never a one-liner.** Every `if`/`else`/`for`/`while`/`try`/`catch`/method/lambda block is opening brace, newline, body line(s), newline, closing brace. No braceless control flow and no single-line braced bodies (`if (x) { y; }` is disallowed). The one exception is a genuinely empty body, which stays `{}`. `} else {` / `} catch {` stay cuddled. Applies to the frontend TS/TSX too.
- **Explicit imports only — no star imports.** Import exactly the classes used; prefer import + simple name over inline fully-qualified names (except when two classes share a simple name, e.g. `com.drew.metadata.Directory` vs the model `Directory` in `PhotoOrganizer`).
- **Blank lines for structure.** One blank line between members; one right after a top-level class's opening brace and one right before its closing brace.
- **`final` by default.** Locals and parameters are `final` unless reassigned — including catch parameters and enhanced-for variables, but not classic mutated for-counters. Method declarations are never `final`.
- **Indentation is 4 spaces, never tabs.**
- **Descriptive catch names.** Name the caught exception for what the handler does — `ignored`, `logged`, `wrapped`, `rethrown` — never a bare `e`/`ex`.
- **Prefer `record`** for data-only types where safe (not Jackson-serialized API results or `Serializable` classes that must keep their shape).
- **No dead declarations.** Remove unused imports; don't declare `throws X` a body can't throw.
- **No confirmation comments** — don't add a comment whose only purpose is to note a rule was followed.
- In the frontend: `const` by default (only `let` for genuinely reassigned bindings), function components + arrow callbacks.

## Important Notes

- Operations are synchronous; the async wrappers in `FileOperationsService` exist for the WebSocket progress flow.
- Operation parameters are passed as `Map<String, Object>` between the service and the operation classes — keys are documented as constants on the operation classes.
- In oneshot mode no HTTP/WebSocket beans are loaded; only the operation pipeline runs.
