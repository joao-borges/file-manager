package ca.joaoborges.filemanager.exception;

/**
 * Base application exception.
 */
public class FileManagerException extends RuntimeException {

    private static final long serialVersionUID = -9198338493432644416L;

    public FileManagerException() {
        super();
    }

    public FileManagerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FileManagerException(final String message) {
        super(message);
    }

    public FileManagerException(final Throwable cause) {
        super(cause);
    }

}
