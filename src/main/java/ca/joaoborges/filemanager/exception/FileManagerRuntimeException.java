package ca.joaoborges.filemanager.exception;

/**
 * Runtime exception for unexpected application failures.
 */
public class FileManagerRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6287017052968573415L;

    public FileManagerRuntimeException() {
        super();
    }

    public FileManagerRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
