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
 * Monta parametros para a execucao da operacao.
 * 
 * @author JoãoGabriel
 */
public interface OperationParamsBuilder extends Serializable {

	/** BEAN_NAME_FORMAT */
	String BEAN_NAME_FORMAT = "OperationParamsBuilder#";

	/**
	 * Monta os parâmetros para uma operação.
	 *
	 * @return String
	 * @throws FileManagerException
	 */
	Map<String, Object> buildParams() throws FileManagerException;
}
