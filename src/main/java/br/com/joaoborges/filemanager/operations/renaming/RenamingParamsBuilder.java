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

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.RENAME_OPERATION;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.common.DirectoryReadResult;
import br.com.joaoborges.filemanager.operations.common.DirectoryReader;
import br.com.joaoborges.filemanager.operations.interfaces.OperationParamsBuilder;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

/**
 * Responsavel por montar o mapa de parametros para renomeio. Le o diretorio e obtem o filtro de extensoes.
 * 
 * @author JoãoGabriel
 */
@Component(value = OperationParamsBuilder.BEAN_NAME_FORMAT + RENAME_OPERATION)
public class RenamingParamsBuilder implements OperationParamsBuilder {

	/** serialVersionUID */
	private static final long serialVersionUID = 3942563355002905568L;

	@Autowired
	private TelaPrincipal tela;

	// sistema é single-user entao eu guardar o estado aqui nao é nada mal.
	private Diretorio lastReadDirectory;

	public Map<String, Object> buildParams() throws FileManagerException {
		DirectoryReadResult directoryResult = DirectoryReader.readFolder("Selecione o diretório com os arquivos",
				this.tela.getFrame(), this.lastReadDirectory);
		this.lastReadDirectory = directoryResult.getDiretorio();

		int option = JOptionPane.showConfirmDialog(this.tela.getFrame(), "Incluir sub-diretórios?", "Pergunta",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		boolean includeSubDirectories = option == JOptionPane.YES_OPTION;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(Diretorio.class.getName(), directoryResult.getDiretorio());
		params.put(FiltroExtensoes.class.getName(), directoryResult.getFiltro());
		params.put(Renomeador.INCLUDE_SUB_DIRECTORIES, includeSubDirectories);

		return params;
	}
}
