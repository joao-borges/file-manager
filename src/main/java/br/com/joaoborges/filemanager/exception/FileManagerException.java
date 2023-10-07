package br.com.joaoborges.filemanager.exception;

/**
 * Excecao do sistema.
 * 
 * @author Joao
 */
public class FileManagerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9198338493432644416L;

	public FileManagerException() {
		super();
	}

	public FileManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileManagerException(String message) {
		super(message);
	}

	public FileManagerException(Throwable cause) {
		super(cause);
	}

}
