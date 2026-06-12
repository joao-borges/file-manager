package ca.joaoborges.filemanager.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.joaoborges.filemanager.type.Extension;

/**
 * File filter that accepts files by extension.
 */
public final class ExtensionFilter implements FilenameFilter, FileFilter {

    private final String[] extensionsAccepted;
    private boolean acceptDirectories = false;

    /**
     * Creates a filter that accepts every supported extension.
     */
    public static ExtensionFilter allAcceptedFilter() {
        return new ExtensionFilter(Extension.asStrings());
    }

    /**
     * Creates a filter accepting only the given extensions.
     */
    public ExtensionFilter(final List<String> extensions) {
        if (extensions == null || extensions.size() == 0) {
            throw new UnsupportedOperationException(
                    "Cannot create an ExtensionFilter without specifying the accepted extensions.");
        }
        this.extensionsAccepted = extensions.toArray(new String[0]);
    }

    /**
     * Checks whether a file is accepted, i.e. whether its extension is allowed by this filter.
     */
    public boolean accept(final File dir, final String name) {
        final File file = new File(dir.getPath() + File.separator + name);
        if (file.isDirectory()) {
            return this.acceptDirectories;
        }

        boolean accepted = false;
        for (final String extension : this.extensionsAccepted) {
            if (name.toLowerCase().endsWith(extension.toLowerCase())) {
                accepted = true;
                break;
            }
        }
        return accepted;
    }

    public boolean accept(final File pathname) {
        return this.accept(pathname, pathname.getName());
    }

    public Set<Extension> getExtensionsAccepted() {
        final Set<Extension> extensions = new TreeSet<>();
        for (final String ext : this.extensionsAccepted) {
            extensions.add(Extension.valueOf(ext.toUpperCase()));
        }
        return extensions;
    }

    @Override
    public String toString() {
        return "Filter: " + Arrays.toString(getExtensionsAccepted().toArray(new Extension[0]));
    }

    public void setAcceptDirectories(final boolean acceptDirectories) {
        this.acceptDirectories = acceptDirectories;
    }

}
