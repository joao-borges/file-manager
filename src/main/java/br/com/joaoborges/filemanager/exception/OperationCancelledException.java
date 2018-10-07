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
public class OperationCancelledException extends FileManagerException {

	/**  serialVersionUID */
	private static final long serialVersionUID = 9067395625895850572L;

	/**
	 * Construtor
	 */
	public OperationCancelledException() {
		super();

	}

	/**
	 * Construtor
	 * 
	 * @param message
	 * @param cause
	 */
	public OperationCancelledException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * Construtor
	 * 
	 * @param message
	 */
	public OperationCancelledException(String message) {
		super(message);

	}

	/**
	 * Construtor
	 * 
	 * @param cause
	 */
	public OperationCancelledException(Throwable cause) {
		super(cause);

	}

}
