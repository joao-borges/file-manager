package ca.joaoborges.filemanager.exception;

/**
 * Thrown when a running operation is cancelled before completion.
 */
public class OperationCancelledException extends FileManagerException {

    private static final long serialVersionUID = 9067395625895850572L;

    public OperationCancelledException() {
        super();
    }

    public OperationCancelledException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OperationCancelledException(final String message) {
        super(message);
    }

    public OperationCancelledException(final Throwable cause) {
        super(cause);
    }

}
