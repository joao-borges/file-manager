package ca.joaoborges.filemanager.operations.interfaces;

import java.io.Serializable;
import java.util.Map;

import ca.joaoborges.filemanager.exception.FileManagerException;

/**
 * Generic operation executed over files.
 *
 * @param <R> the result type
 */
public interface FileOperation<R extends OperationResult> extends Serializable {

    /**
     * Executes the operation.
     *
     * @param params the operation parameters
     * @return the result, or null when the operation produces none
     * @throws FileManagerException when the operation fails
     */
    R execute(Map<String, Object> params) throws FileManagerException;

    /**
     * Returns the human-readable name of the operation.
     */
    String getOperationName();

    /**
     * Returns the identifier of the operation.
     */
    String getOperationID();

}
