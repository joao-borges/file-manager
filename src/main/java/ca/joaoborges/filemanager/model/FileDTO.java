package ca.joaoborges.filemanager.model;

import java.io.File;
import java.io.Serializable;

import ca.joaoborges.filemanager.type.Extension;

/**
 * Groups the data of a single file.
 */
public final class FileDTO implements Serializable {

    private static final long serialVersionUID = 419275423063428717L;

    private final File file;
    private final Extension extension;
    private final Directory contentDirectory;

    public FileDTO(final File file, final Extension extension, final Directory contentDirectory) {
        this.file = file;
        this.extension = extension;
        this.contentDirectory = contentDirectory;
    }

    public File getFile() {
        return this.file;
    }

    public Extension getExtension() {
        return this.extension;
    }

    public Directory getContentDirectory() {
        return this.contentDirectory;
    }

}
