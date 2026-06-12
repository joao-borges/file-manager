package ca.joaoborges.filemanager.operations.extraction;

import java.util.HashMap;
import java.util.Map;

import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.operations.interfaces.OperationResult;

/**
 * Result of the extraction operation.
 */
public class ExtractionResult implements OperationResult {

    private Map<String, String> movedFiles;
    private Directory baseDirectory;
    private Directory destinationDirectory;

    public ExtractionResult(final Directory baseDirectory, final Directory destinationDirectory) {
        this.baseDirectory = baseDirectory;
        this.destinationDirectory = destinationDirectory;
        this.movedFiles = new HashMap<>();
    }

    public Map<String, String> getMovedFiles() {
        return this.movedFiles;
    }

    public void setMovedFiles(final Map<String, String> movedFiles) {
        this.movedFiles = movedFiles;
    }

    public Directory getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(final Directory baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Directory getDestinationDirectory() {
        return this.destinationDirectory;
    }

    public void setDestinationDirectory(final Directory destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

}
