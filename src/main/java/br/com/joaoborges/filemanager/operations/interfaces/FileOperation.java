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

package br.com.joaoborges.filemanager.operations.interfaces;

import java.io.Serializable;
import java.util.Map;

import br.com.joaoborges.filemanager.exception.FileManagerException;

/**
 * Operacao generica que é executada em cima de arquivos.
 * 
 * @author João
 * @param <R>
 *            tipo do resultado
 */
public interface FileOperation<R extends OperationResult> extends Serializable {

	/**
	 * Executa a operação .
	 * 
	 * @param params
	 * @return retorno, ou nulo caso nao possua
	 * @throws FileManagerException
	 */
	public R execute(Map<String, Object> params) throws FileManagerException;

	/**
	 * Nome da operação.
	 *
	 * @return String
	 */
	public String getOperationName();

	/**
	 * Retorna o identificador da operação.
	 *
	 * @return String
	 */
	public String getOperationID();

}
