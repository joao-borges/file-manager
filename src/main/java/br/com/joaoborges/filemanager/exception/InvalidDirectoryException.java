/*
 * Copyright (c) 1999-2010 Touch Tecnologia e Informatica Ltda. 
 * 
 * R. Gomes de Carvalho, 1666, 3o. Andar, Vila Olimpia, Sao Paulo, SP, Brasil. 
 * 
 * Todos os direitos reservados. 
 * Este software e confidencial e de propriedade da Touch Tecnologia e Informatica Ltda. (Informacao Confidencial) 
 * As informacoes contidas neste arquivo nao podem ser publicadas, e seu uso esta limitado de acordo
 * com os termos do contrato de licenca.
 */

package br.com.joaoborges.filemanager.exception;

/**
 * @author JoãoGabriel
 */
public class InvalidDirectoryException extends FileManagerException {

	/**
	 * Construtor
	 */
	public InvalidDirectoryException() {
		super();

	}

	/**
	 * Construtor
	 * 
	 * @param message
	 * @param cause
	 */
	public InvalidDirectoryException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * Construtor
	 * 
	 * @param message
	 */
	public InvalidDirectoryException(String message) {
		super(message);

	}

	/**
	 * Construtor
	 * 
	 * @param cause
	 */
	public InvalidDirectoryException(Throwable cause) {
		super(cause);

	}

}
