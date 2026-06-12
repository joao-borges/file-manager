package ca.joaoborges.filemanager.operations.renaming;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.model.ExtensionFilter;
import ca.joaoborges.filemanager.model.FileDTO;
import ca.joaoborges.filemanager.model.util.Message;
import ca.joaoborges.filemanager.operations.NameUtils;
import ca.joaoborges.filemanager.operations.common.OperationConstants;
import ca.joaoborges.filemanager.operations.common.SpringUtils;
import ca.joaoborges.filemanager.operations.interfaces.FileOperation;
import ca.joaoborges.filemanager.operations.interfaces.OperationResult;
import ca.joaoborges.filemanager.operations.renaming.Renamer.RenamingResult;
import ca.joaoborges.filemanager.type.Extension;
import ca.joaoborges.filemanager.type.ReplacingConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for applying the renaming rules and producing the new file names.
 */
@Service(value = OperationConstants.RENAME_OPERATION)
@Slf4j
public class Renamer implements FileOperation<RenamingResult> {

    public static final String INCLUDE_SUB_DIRECTORIES = "INCLUDE_SUB_DIRECTORIES";

    private static final long serialVersionUID = -8287266807900801749L;

    @Autowired
    private ExclusionManagerService exclusions;

    /**
     * Renames the files contained in the directory.
     *
     * @throws FileManagerException on errors.
     */
    public RenamingResult execute(final Map<String, Object> params) throws FileManagerException {
        final Directory contentDirectory = (Directory) params.get(Directory.class.getName());
        ExtensionFilter requestedFilter = (ExtensionFilter) params.get(ExtensionFilter.class.getName());
        Boolean includeSubDirectories = (Boolean) params.get(INCLUDE_SUB_DIRECTORIES);
        final RenamingResult result = new RenamingResult(contentDirectory);

        // sub-directories are excluded unless explicitly requested
        if (includeSubDirectories == null) {
            includeSubDirectories = false;
        }
        if (requestedFilter == null) {
            requestedFilter = ExtensionFilter.allAcceptedFilter();
            requestedFilter.setAcceptDirectories(includeSubDirectories);
        }

        this.renameDirectoryContents(contentDirectory, requestedFilter, result);

        return result;
    }

    private void renameDirectoryContents(final Directory contentDirectory, final ExtensionFilter requestedFilter,
            final RenamingResult result) {
        final Collection<String> originalFileList = contentDirectory.listFileNames(requestedFilter);
        final Collection<File> contents = contentDirectory.listContents(requestedFilter);
        log.debug("Total " + contents.size());
        for (final File fileToRename : contents) {
            if (fileToRename.isDirectory()) {
                final Directory subDirectory = new Directory(fileToRename.getPath());
                this.renameDirectoryContents(subDirectory, requestedFilter, result);
            } else if (!fileToRename.isHidden() && fileToRename.canRead() && fileToRename.canWrite()) {
                try {
                    final String originalName = fileToRename.getName();
                    log.debug("Renaming: " + originalName);

                    String newName = originalName;
                    newName = newName.trim().toLowerCase();
                    final String[] nameSeparated = NameUtils.splitExtension(newName);
                    final String extension = nameSeparated[1];
                    final Extension ext = Extension.valueOf(extension.toUpperCase());
                    // try to find a post processor matching the file type
                    final PostProcessor postProcessor = SpringUtils.getContext()
                            .getBean(PostProcessor.BEAN_NAME_FORMAT + ext.getType(), PostProcessor.class);

                    newName = nameSeparated[0];
                    newName = newName.trim();
                    newName = replaces(newName);
                    newName = newName.trim();
                    newName = processNumbers(newName);
                    newName = newName.trim();
                    newName = postProcessor.processFileName(newName);
                    newName = newName.trim();
                    // assemble the final name
                    newName += ReplacingConstants.POINT + extension;

                    // remove escaped characters
                    newName = StringEscapeUtils.unescapeXml(newName);

                    // title case for a nicer-looking name
                    newName = WordUtils.capitalizeFully(newName);

                    File renamedFile = new File(contentDirectory.getPath() + File.separator + newName);
                    if (!originalName.equals(newName)) {
                        if (originalFileList.contains(newName)) {
                            newName = "(" + System.currentTimeMillis() + ") " + newName;
                            renamedFile = new File(contentDirectory.getPath() + File.separator + newName);
                        }
                        fileToRename.renameTo(renamedFile);

                        result.renamedFiles.put(renamedFile.getPath(), fileToRename.getPath());
                        log.debug("Final name: " + newName);
                        // run additional file-level operations, if any
                        postProcessor.processFile(new FileDTO(renamedFile, ext, contentDirectory), result,
                                originalFileList);
                    }

                } catch (final Exception logged) {
                    log.error("There is a problem with file " + fileToRename.getName(), logged);
                }
            }
        }
    }

    /**
     * Groups the name correction operations.
     */
    static String replaces(final String newName) {
        return simpleReplacements(newName);
    }

    /**
     * Replaces unwanted strings with spaces.
     */
    private static String simpleReplacements(String newName) {
        newName = doReplacement(newName, ReplacingConstants.PLUS, ReplacingConstants.SPACE);

        final Enumeration<String> filterStrings = Message.getLocalizer(Message.STRINGS_TO_FILTER, null).getKeys();

        while (filterStrings.hasMoreElements()) {
            final String key = filterStrings.nextElement();
            final String filter = Message.getMessage(Message.STRINGS_TO_FILTER, key);

            if (newName.lastIndexOf(filter) != -1) {
                newName = newName.replaceAll(filter, ReplacingConstants.SPACE);
            }
            newName = newName.trim();
        }
        return newName;
    }

    private static String doReplacement(String stringToReplace, final String charToBeReplaced,
            final String substituteChar) {
        for (int i = 0; i < stringToReplace.length(); i++) {
            final char c = stringToReplace.charAt(i);
            if (charToBeReplaced.equals(String.valueOf(c))) {
                stringToReplace = stringToReplace.substring(0, i) + substituteChar + stringToReplace.substring(i + 1);
            }
        }

        return stringToReplace;
    }

    /**
     * Strips leading characters (numbers, punctuation) from the start of the name,
     * unless the name starts with a configured exclusion.
     */
    private String processNumbers(String newName) {
        boolean allowed = true;
        // check whether the start of the name may be touched
        for (final String exclusion : this.exclusions.getExclusions()) {
            if (newName.startsWith(exclusion.toLowerCase())) {
                // the start of the name matches an exclusion, leave it untouched
                allowed = false;
            }
        }

        if (allowed) {
            // strip characters until the name starts with a lowercase letter (ascii 97 to 122)
            boolean repeat = true;
            while (repeat) {
                boolean keepStripping = true;
                String stripped = newName;
                int i = 0;
                while (keepStripping && i < newName.length()) {
                    final int ch = newName.charAt(i);
                    if (ch == 32 || (ch >= 97 && ch <= 122)) {
                        keepStripping = false;
                    } else {
                        stripped = newName.substring(i + 1);
                    }
                    i++;
                }
                newName = stripped.trim();
                final int ch = newName.charAt(0);
                if (ch >= 97 && ch <= 122) {
                    repeat = false;
                }
            }
        }
        return newName;
    }

    /**
     * Result of a rename run.
     */
    public class RenamingResult implements OperationResult {

        private static final long serialVersionUID = 610178010997711328L;

        private Map<String, String> renamedFiles;
        private Map<String, String> duplicatedFiles;
        private Directory currentDirectory;

        RenamingResult(final Directory currentDirectory) {
            this.currentDirectory = currentDirectory;

            this.renamedFiles = new HashMap<>();
            this.duplicatedFiles = new HashMap<>();
        }

        Map<String, String> getRenamedFiles() {
            return this.renamedFiles;
        }

        public Map<String, String> getDuplicatedFiles() {
            return this.duplicatedFiles;
        }

        Directory getCurrentDirectory() {
            return this.currentDirectory;
        }
    }

    public String getOperationName() {
        return "Rename Files";
    }

    public String getOperationID() {
        return OperationConstants.RENAME_OPERATION;
    }

}
