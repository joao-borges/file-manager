package ca.joaoborges.filemanager.operations.duplicateFinder;

import java.util.HashMap;
import java.util.Map;

import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.operations.interfaces.OperationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Result of the duplicate finder operation.
 */
@RequiredArgsConstructor
@Getter
public class DuplicateFinderResult implements OperationResult {

    private static final long serialVersionUID = -5609682745330701000L;

    private final Map<String, String> files = new HashMap<>();
    private final Directory directory;

}
