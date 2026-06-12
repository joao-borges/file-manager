package ca.joaoborges.filemanager.operations;

import ca.joaoborges.filemanager.type.ReplacingConstants;

public class NameUtils {

    /**
     * Splits the extension from the file name.
     *
     * @return an array with the base name at index 0 and the extension at index 1.
     */
    public static String[] splitExtension(final String newName) {
        final String[] parts = new String[2];

        parts[1] = newName.substring(newName.lastIndexOf(ReplacingConstants.POINT) + 1);
        parts[0] = newName.substring(0, newName.lastIndexOf(ReplacingConstants.POINT));
        return parts;
    }

    public static String getBlockBehindChar(final String fullString, final int i) {
        int beforeSpace = -1;
        int afterSpace = fullString.length();

        for (int j = i; j >= 0; j--) {
            if (fullString.charAt(j) == ReplacingConstants.SPACE.charAt(0)) {
                beforeSpace = j;
                break;
            }
        }
        for (int k = i; k < fullString.length(); k++) {
            if (fullString.charAt(k) == ReplacingConstants.SPACE.charAt(0)) {
                afterSpace = k;
                break;
            }
        }

        return fullString.substring(beforeSpace + 1, afterSpace);
    }

}
