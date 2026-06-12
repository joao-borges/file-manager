package ca.joaoborges.filemanager.operations.renaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.joaoborges.filemanager.operations.NameUtils;

/**
 * Shared renaming operations.
 */
@Component
public class RenamingOperations {

    @Autowired
    private ExclusionManagerService exclusions;

    public String doReplaceAll(String stringToReplace, final String charToBeReplaced, final String substituteChar) {
        for (int i = 0; i < stringToReplace.length(); i++) {
            final char c = stringToReplace.charAt(i);
            if (charToBeReplaced.equals(String.valueOf(c))) {
                if (!this.exclusions.hasExclusionFor(NameUtils.getBlockBehindChar(stringToReplace, i), true)) {
                    stringToReplace = stringToReplace.substring(0, i) + substituteChar
                            + stringToReplace.substring(i + 1);
                }
            }
        }
        return stringToReplace;
    }

}
