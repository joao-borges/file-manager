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

package br.com.joaoborges.filemanager.ui.operations;

import java.io.Serializable;

import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

/**
 * Encapsula o tratamento do resultado da operação relacionado a interface de usuário.
 * 
 * @author JoãoGabriel
 * @param <R>
 */
public interface UIOperationResultProcessor<R extends OperationResult> extends Serializable {

	/** BEAN_NAME_FORMAT */
	public static final String BEAN_NAME_FORMAT = "UIOperationResultProcessor#";

	/**
	 * Processa o resultado para enviar para tela.
	 *
	 * @param result
	 * @param tela
	 */
	public void processResult(R result, TelaPrincipal tela);

}
