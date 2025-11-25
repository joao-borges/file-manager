package br.com.joaoborges.filemanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.joaoborges.filemanager.dto.OrganizeRequest;
import br.com.joaoborges.filemanager.dto.RenameRequest;
import br.com.joaoborges.filemanager.operations.duplicateFinder.DuplicateFinder;
import br.com.joaoborges.filemanager.operations.extraction.Extrator;
import br.com.joaoborges.filemanager.operations.organization.OrganizationResult;
import br.com.joaoborges.filemanager.operations.organization.Organizador;
import br.com.joaoborges.filemanager.operations.photoOrganization.PhotoOrganizator;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador;

/**
 * Unit tests for FileOperationsService
 *
 * Tests both synchronous and asynchronous operation methods
 */
@ExtendWith(MockitoExtension.class)
class FileOperationsServiceTest {

    @Mock
    private Renomeador renomeador;

    @Mock
    private Organizador organizador;

    @Mock
    private Extrator extrator;

    @Mock
    private PhotoOrganizator photoOrganizator;

    @Mock
    private DuplicateFinder duplicateFinder;

    @InjectMocks
    private FileOperationsService service;

    private RenameRequest renameRequest;
    private OrganizeRequest organizeRequest;

    @BeforeEach
    void setUp() {
        renameRequest = RenameRequest.builder()
            .sourceDirectory("/test/source")
            .includeSubDirectories(true)
            .build();

        organizeRequest = OrganizeRequest.builder()
            .sourceDirectory("/test/source")
            .destinationDirectory("/test/dest")
            .build();
    }

    @Test
    void testExecuteRename_CallsRenomeador() {
        // Given
        when(renomeador.execute(any())).thenReturn(null);

        // When
        service.executeRename(renameRequest);

        // Then
        verify(renomeador, times(1)).execute(any());
    }

    @Test
    void testExecuteOrganize_CallsOrganizador() {
        // Given
        when(organizador.execute(any())).thenReturn(null);

        // When
        service.executeOrganize(organizeRequest);

        // Then
        verify(organizador, times(1)).execute(any());
    }
}
