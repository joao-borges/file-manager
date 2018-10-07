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

package br.com.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;
import java.util.Collection;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.FileDTO;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador.RenamingResult;

/**
 * Marca um pós-processador, que pdoe ser interpretado para determinado tipo de arquivo.
 * 
 * @author JoãoGabriel
 */
public interface PostProcessor extends Serializable {

	/** BEAN_NAME_FORMAT */
	public static final String BEAN_NAME_FORMAT = "PostProcessor#";

	/**
	 * Aplica o pós-processamento sobre o nome do arquivo.
	 *
	 * @param fileName
	 * @return String pos-processada
	 */
	public String processFileName(String fileName);

	/**
	 * Processa o arquiuvo em si aplicando as modificações desejadas
	 *
	 * @param file
	 * @param result
	 * @param originalFileList
	 * @throws FileManagerException
	 */
	public void processFile(FileDTO file, RenamingResult result, Collection<String> originalFileList)
			throws FileManagerException;

}
