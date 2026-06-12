package ca.joaoborges.filemanager.type;

import java.io.Serializable;
import java.util.List;

/**
 * Content of a directory; either a subdirectory or a file.
 */
public final class FolderContent implements Serializable {

    private static final long serialVersionUID = -6016471172439929351L;

    private String name;
    private List<FolderContent> contentInside;
    private boolean isDirectory;

    public FolderContent(final String name, final boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<FolderContent> getContentInside() {
        return this.contentInside;
    }

    public void setContentInside(final List<FolderContent> contentInside) {
        this.contentInside = contentInside;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(final boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

}
