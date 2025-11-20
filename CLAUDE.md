# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot-based Java Swing desktop application for file management operations. The application provides a GUI for performing various file operations like renaming, organizing, extracting, photo organization by date, and duplicate file detection.

## Build and Run

**Build the project:**
```bash
./mvnw clean package
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Run tests:**
```bash
./mvnw test
```

## Technology Stack

- Java 21
- Spring Boot 3.2.3
- Java Swing (UI framework)
- Maven (build tool)
- Lombok (code generation)
- Apache Commons (IO, Text)
- Metadata extractors for images/videos (metadata-extractor, jaudiotagger)

## Architecture

### Core Operation Pattern

The application follows a consistent operation pattern across all file operations:

1. **FileOperation Interface**: All operations implement `FileOperation<R>` where R extends `OperationResult`
   - Located in `operations/interfaces/`
   - Each operation must implement `execute(Map<String, Object> params)`

2. **Operation Execution Flow**:
   - User selects operation from `TelaPrincipal` (main UI)
   - `OperationExecuteListener` intercepts the action
   - `OperationRunner` orchestrates the execution:
     - Retrieves the appropriate `OperationParamsBuilder` bean by operation ID
     - Builds parameters via UI dialogs
     - Executes the operation
     - Returns the result

3. **Operation Components** (per operation):
   - Operation class (e.g., `Renomeador`, `Organizador`) - implements `FileOperation<R>`
   - ParamsBuilder (e.g., `RenamingParamsBuilder`) - builds operation parameters via UI
   - Result class (e.g., `RenamingResult`) - encapsulates operation results
   - ResultProcessor (e.g., `RenamingResultProcessor`) - processes and displays results

### Main Operations

**Renaming (`operations/renaming/`)**
- Operation ID: `RENAME_OPERATION`
- Renames files based on patterns and exclusion rules
- Uses `ExclusionManagerService` to load exclusion patterns from `exclusions.xml`
- Supports post-processors (e.g., `AudioPostProcessor` for audio files)

**Organization (`operations/organization/`)**
- Operation ID: `ORGANIZATION_OPERATION`
- Organizes files by extension into category folders
- Uses extension filters from properties files

**Extraction (`operations/extraction/`)**
- Operation ID: `EXTRACTION_OPERATION`
- Recursively extracts files matching extension filters from nested directories

**Photo Organization (`operations/photoOrganization/`)**
- Operation ID: `PHOTO_ORGANIZATION_OPERATION`
- Organizes photos/videos by date extracted from EXIF metadata
- Uses `metadata-extractor` library to read EXIF data from images and videos

**Duplicate Finder (`operations/duplicateFinder/`)**
- Operation ID: `DUPLICATE_FINDER_OPERATION`
- Finds and removes duplicate files based on MD5 hashes
- Reads from `md5sumfiles.txt` in the target directory

### Spring Configuration

- Main class: `FileManager.java`
- Component scan: `br.com.joaoborges.filemanager`
- Headless mode: **disabled** (GUI application)
- `SpringUtils` provides static access to ApplicationContext for bean lookup

### UI Structure

- `TelaPrincipal`: Main window with menu and file table
- `ui/listener/`: Action listeners for menu operations
- `ui/operations/`: UI-specific operation handling
- `ui/utils/`: UI utilities (waiting screen, file info display)

### Model and Utilities

- `model/Diretorio`: Represents a directory with file listing capabilities
- `model/FiltroExtensoes`: File extension filter system
- `model/util/FileUtils`: File operation utilities
- `type/`: Enums for file types, extensions, and constants

### Configuration Files

- `src/main/resources/br/com/joaoborges/filemanager/resources/`:
  - `Extensoes.properties`: File extension definitions
  - `GruposExtensoes.properties`: Extension groups (audio, video, images, etc.)
  - `RegexesToFilter.properties`: Regex patterns for filtering
  - `StringsToFilter.properties`: String patterns for filtering
- `src/main/resources/exclusions.xml`: Exclusion rules for renaming operations

## Common Patterns

**Adding a New Operation:**
1. Create operation package under `operations/`
2. Implement `FileOperation<YourResult>`
3. Create `YourResult` extending `OperationResult`
4. Create `YourParamsBuilder` implementing `OperationParamsBuilder`
5. Create `YourResultProcessor` for result handling
6. Add operation constant to `OperationConstants`
7. Register in `TelaPrincipal` menu

**Spring Bean Naming:**
- Operations are registered as Spring beans with their operation ID as the bean name
- ParamsBuilders use format: `OperationParamsBuilder.BEAN_NAME_FORMAT + operationID`

## Important Notes

- The application uses Portuguese for UI labels and some variable names
- Operations are executed synchronously with a waiting screen displayed
- File operations typically show preview in the main table before execution
- The application stores operation state in `Map<String, Object>` passed between components
