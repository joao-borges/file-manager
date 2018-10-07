package br.com.joaoborges.filemanager.exception;

/**
 * Excecao runtime do sistema.
 * 
 * @author Joao
 */
public class FileManagerRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6287017052968573415L;

	public FileManagerRuntimeException() {
		super();
	}

	public FileManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
