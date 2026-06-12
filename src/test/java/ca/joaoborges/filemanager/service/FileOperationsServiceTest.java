package ca.joaoborges.filemanager.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.joaoborges.filemanager.dto.OrganizeRequest;
import ca.joaoborges.filemanager.dto.RenameRequest;
import ca.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinder;
import ca.joaoborges.filemanager.operations.extraction.Extractor;
import ca.joaoborges.filemanager.operations.organization.Organizer;
import ca.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizer;
import ca.joaoborges.filemanager.operations.renaming.Renamer;

/**
 * Unit tests for FileOperationsService
 *
 * Tests both synchronous and asynchronous operation methods
 */
@ExtendWith(MockitoExtension.class)
class FileOperationsServiceTest {

    @Mock
    private Renamer renamer;

    @Mock
    private Organizer organizer;

    @Mock
    private Extractor extractor;

    @Mock
    private PhotoOrganizer photoOrganizer;

    @Mock
    private DuplicateFinder duplicateFinder;

    @InjectMocks
    private FileOperationsService service;

    @TempDir
    Path tempDir;

    private RenameRequest renameRequest;
    private OrganizeRequest organizeRequest;

    @BeforeEach
    void setUp() throws IOException {
        // The service constructs Directory(path) before delegating to the mocked
        // operation, and Directory rejects non-existent paths — so the inputs
        // must point at real directories.
        final Path source = Files.createDirectories(tempDir.resolve("source"));
        final Path dest = Files.createDirectories(tempDir.resolve("dest"));

        renameRequest = RenameRequest.builder()
            .sourceDirectory(source.toString())
            .includeSubDirectories(true)
            .build();

        organizeRequest = OrganizeRequest.builder()
            .sourceDirectory(source.toString())
            .destinationDirectory(dest.toString())
            .build();
    }

    @Test
    void testExecuteRename_CallsRenamer() {
        // Given
        when(renamer.execute(any())).thenReturn(null);

        // When
        service.executeRename(renameRequest);

        // Then
        verify(renamer, times(1)).execute(any());
    }

    @Test
    void testExecuteOrganize_CallsOrganizer() {
        // Given
        when(organizer.execute(any())).thenReturn(null);

        // When
        service.executeOrganize(organizeRequest);

        // Then
        verify(organizer, times(1)).execute(any());
    }

}
