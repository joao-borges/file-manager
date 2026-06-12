package ca.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;
import java.util.Collection;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.FileDTO;
import ca.joaoborges.filemanager.operations.renaming.Renamer.RenamingResult;

/**
 * Marks a post processor that can be selected for a given file type.
 */
public interface PostProcessor extends Serializable {

    String BEAN_NAME_FORMAT = "PostProcessor#";

    /**
     * Applies post-processing to the file name.
     *
     * @return the post-processed name
     */
    String processFileName(String fileName);

    /**
     * Processes the file itself, applying the desired modifications.
     */
    void processFile(FileDTO file, RenamingResult result, Collection<String> originalFileList)
            throws FileManagerException;

}
