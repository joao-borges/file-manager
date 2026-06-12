package ca.joaoborges.filemanager.model.util;

import java.io.File;

import ca.joaoborges.filemanager.type.ReplacingConstants;

/**
 * Utility operations for files.
 */
public class FileUtils {

    /**
     * Computes a human-readable file size (KB or MB) for the given file name.
     */
    public static String calculateSize(final String fileName) {
        final File file = new File(fileName);
        String size = "";
        final double fileSize = file.length() / 1024;
        if (fileSize <= 1024) {
            size = fileSize + " KB";
        } else {
            final double fileSizeInMb = fileSize / 1024;
            size += fileSizeInMb;
            final int index = size.lastIndexOf(".");
            if (size.substring(index).length() > 1) {
                size = size.substring(0, index) + ReplacingConstants.POINT + size.substring(index + 1, index + 2) + " MB";
            }
        }
        return size;
    }

}
