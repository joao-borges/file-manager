package ca.joaoborges.filemanager.model;

import java.io.File;
import java.util.Comparator;

/**
 * Compares files by name, case-insensitively, for sorting collections.
 */
public enum FilenameComparator implements Comparator<File> {

    NAME_ORDER;

    @Override
    public int compare(final File first, final File second) {
        return first.getName().toLowerCase().compareTo(second.getName().toLowerCase());
    }

}
