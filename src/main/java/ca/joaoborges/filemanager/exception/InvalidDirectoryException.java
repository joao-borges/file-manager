package ca.joaoborges.filemanager.exception;

/**
 * Thrown when a directory is missing, inaccessible, or otherwise invalid for an operation.
 */
public class InvalidDirectoryException extends FileManagerException {

    public InvalidDirectoryException() {
        super();
    }

    public InvalidDirectoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidDirectoryException(final String message) {
        super(message);
    }

    public InvalidDirectoryException(final Throwable cause) {
        super(cause);
    }

}
