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

package br.com.joaoborges.filemanager.operations.organization;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.common.DirectoryReadResult;
import br.com.joaoborges.filemanager.operations.common.DirectoryReader;
import br.com.joaoborges.filemanager.operations.common.OperationConstants;
import br.com.joaoborges.filemanager.operations.interfaces.OperationParamsBuilder;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

/**
 * Monta os parämetros para a operacao de organizacao.
 * 
 * @author JoãoGabriel
 */
@Component(value = OperationParamsBuilder.BEAN_NAME_FORMAT + OperationConstants.ORGANIZATION_OPERATION)
public class OrganizationParamsBuilder implements OperationParamsBuilder {

	/** serialVersionUID */
	private static final long serialVersionUID = 4829235059186660489L;

	/** BASE_DIR */
	public static final String BASE_DIR = "BASE_DIR";
	/** DEST_DIR */
	public static final String DEST_DIR = "DEST_DIR";

	@Autowired
	private TelaPrincipal tela;

	// sistema é single-user entao eu guardar o estado aqui nao é nada mal.
	private Diretorio lastReadBaseDir;
	private Diretorio lastReadDestDir;

	public Map<String, Object> buildParams() throws FileManagerException {
		DirectoryReadResult directoryBase = DirectoryReader.readFolder("Selecione o diretório de origem",
				this.tela.getFrame(), this.lastReadBaseDir);
		this.lastReadBaseDir = directoryBase.getDiretorio();
		DirectoryReadResult directoryDestination = DirectoryReader.readFolder("Selecione o diretório de destino",
				this.tela.getFrame(), this.lastReadDestDir);
		this.lastReadDestDir = directoryDestination.getDiretorio();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(BASE_DIR, directoryBase.getDiretorio());
		params.put(DEST_DIR, directoryDestination.getDiretorio());
		params.put(FiltroExtensoes.class.getName(), directoryBase.getFiltro());

		return params;
	}

}
