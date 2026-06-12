package ca.joaoborges.filemanager.operations.extraction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileExistsException;
import org.springframework.stereotype.Service;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.model.ExtensionFilter;
import ca.joaoborges.filemanager.operations.common.OperationConstants;
import ca.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.extern.slf4j.Slf4j;

import static ca.joaoborges.filemanager.operations.common.OperationConstants.EXTRACTION_OPERATION;
import static org.apache.commons.io.FileUtils.moveFile;

@Service(value = OperationConstants.EXTRACTION_OPERATION)
@Slf4j
public class Extractor implements FileOperation<ExtractionResult> {

    @Override
    public ExtractionResult execute(final Map<String, Object> params) throws FileManagerException {
        final Directory dirBase = (Directory) params.get("BASE_DIR");
        final Directory dirDest = (Directory) params.get("DEST_DIR");
        final ExtensionFilter filter = (ExtensionFilter) params.get(ExtensionFilter.class.getName());
        // Keep acceptDirectories=false: the recursive walk descends via its own
        // directory filter, so including dirs in the user filter would make us
        // try to moveFile() a directory, which throws.
        final ExtractionResult result = new ExtractionResult(dirBase, dirDest);

        // recursively scan the base directory looking for files with the given extensions;
        // when found, move them to the same relative path under the destination directory
        dirBase.listContentsRecursively(filter).forEach(file -> {
            String newFileDirPath = file.getPath().replace(dirBase.getPath(), dirDest.getPath());
            newFileDirPath = newFileDirPath.substring(0, newFileDirPath.lastIndexOf(File.separator));
            final File newFileDir = new File(newFileDirPath);
            newFileDir.mkdirs();
            final File newFile = new File(newFileDir.getPath() + File.separator + file.getName());
            try {
                moveFile(file, newFile);
                result.getMovedFiles().put(file.getPath(), newFile.getPath());
            } catch (final FileExistsException logged) {
                log.warn(logged.getMessage());
            } catch (final IOException wrapped) {
                throw new FileManagerException(wrapped.getMessage(), wrapped);
            }
        });
        return result;
    }

    @Override
    public String getOperationName() {
        return "Extract Files of a Specific Type";
    }

    @Override
    public String getOperationID() {
        return EXTRACTION_OPERATION;
    }

}
