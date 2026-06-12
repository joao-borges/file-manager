package ca.joaoborges.filemanager.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import static ca.joaoborges.filemanager.model.FilenameComparator.NAME_ORDER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Represents a file system directory.
 * <p>
 * Holds the directory contents and performs operations over them.
 */
public class Directory {

    private static final FileFilter DIRECTORY_FILTER = File::isDirectory;

    private final String path;
    private final Collection<File> allContent;
    private final File dir;

    public Directory(final String path) {
        this.path = path;
        this.dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Cannot create a Directory for an invalid path: " + path);
        }
        this.allContent = new TreeSet<>(NAME_ORDER);
        this.allContent.addAll(Arrays.asList(dir.listFiles()));
    }

    public String getPath() {
        return path;
    }

    public Collection<File> getAllContent() {
        return allContent;
    }

    public File getDirectory() {
        return dir;
    }

    /**
     * Lists the directory contents matching the given filter, sorted by name.
     */
    public List<File> listContents(final FilenameFilter filter) {
        return listContentsInternal(filter, dir);
    }

    public List<File> listContentsRecursively(final FilenameFilter filter) {
        final List<File> files = new ArrayList<>();
        listContentsRecursively(dir, filter, files);
        files.sort(NAME_ORDER);
        return files;
    }

    /**
     * Lists the names of the directory contents matching the given filter, sorted by name.
     */
    public List<String> listFileNames(final FilenameFilter filter) {
        final List<String> names = new ArrayList<>();
        for (final File file : listContents(filter)) {
            names.add(file.getName());
        }
        return names;
    }

    private static List<File> listContentsInternal(final FilenameFilter filter, final File directory) {
        final File[] files = directory.listFiles(filter);
        if (files == null) {
            return emptyList();
        }
        return Stream.of(files).sorted(NAME_ORDER).collect(toList());
    }

    private static void listContentsRecursively(final File dir, final FilenameFilter filter, final List<File> fileList) {
        fileList.addAll(listContentsInternal(filter, dir));

        final File[] subdirectories = dir.listFiles(DIRECTORY_FILTER);
        if (subdirectories != null) {
            for (final File subdirectory : subdirectories) {
                listContentsRecursively(subdirectory, filter, fileList);
            }
        }
    }

}
