package ca.joaoborges.filemanager.operations.organization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.model.ExtensionFilter;
import ca.joaoborges.filemanager.operations.common.OperationConstants;
import ca.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.extern.slf4j.Slf4j;

@Service(value = OperationConstants.ORGANIZATION_OPERATION)
@Slf4j
public class Organizer implements FileOperation<OrganizationResult> {

    private static final long serialVersionUID = 2135777385329465832L;

    @Override
    public OrganizationResult execute(final Map<String, Object> params) throws FileManagerException {
        final Directory dirBase = (Directory) params.get("BASE_DIR");
        final Directory dirDest = (Directory) params.get("DEST_DIR");
        ExtensionFilter filter = (ExtensionFilter) params.get(ExtensionFilter.class.getName());
        filter = filter != null ? filter : ExtensionFilter.allAcceptedFilter();
        final OrganizationResult result = new OrganizationResult(dirBase, dirDest);

        final Collection<File> contents = dirBase.listContents(filter);

        // map the contents by the first character of the file name
        final Map<String, Collection<File>> mappedFiles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // iterate over the files and group them alphabetically
        for (final File file : contents) {
            final String firstChar = file.getName().substring(0, 1);
            if (!mappedFiles.containsKey(firstChar)) {
                mappedFiles.put(firstChar, new ArrayList<>());
            }
            mappedFiles.get(firstChar).add(file);
        }

        // now that they are grouped, move them to the destination directory,
        // creating the per-letter folders when needed
        final File dirAsFile = dirDest.getDirectory();
        for (final Map.Entry<String, Collection<File>> destFolderContent : mappedFiles.entrySet()) {
            // create the directory if it does not exist
            final File organizedDir = new File(dirAsFile.getAbsolutePath() + File.separator + destFolderContent.getKey());
            if (!organizedDir.exists()) {
                organizedDir.mkdirs();
            }

            // move the files
            for (final File originalFile : destFolderContent.getValue()) {
                final File destFile = new File(organizedDir.getAbsolutePath() + File.separator + originalFile.getName());
                try {
                    FileUtils.moveFile(originalFile, destFile);
                } catch (final FileExistsException logged) {
                    log.warn(logged.getMessage());
                } catch (final IOException wrapped) {
                    throw new FileManagerException(wrapped.getMessage(), wrapped);
                }
                result.getMovedFiles().put(originalFile.getPath(), destFile.getPath());
            }
        }

        return result;
    }

    @Override
    public String getOperationName() {
        return "Organize Files";
    }

    @Override
    public String getOperationID() {
        return OperationConstants.ORGANIZATION_OPERATION;
    }

}
