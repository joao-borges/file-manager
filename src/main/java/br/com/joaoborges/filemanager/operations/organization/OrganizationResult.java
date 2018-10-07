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

import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;

/**
 * Resultado da operação de organização.
 * 
 * @author JoãoGabriel
 */
public class OrganizationResult implements OperationResult {

	/** serialVersionUID */
	private static final long serialVersionUID = -5609682745330701000L;

	private Map<String, String> movedFiles;
	private Diretorio diretorioBase;
	private Diretorio diretorioDestino;

	/**
	 * Construtor
	 * 
	 * @param diretorioBase
	 * @param diretorioDestino
	 */
	public OrganizationResult(Diretorio diretorioBase, Diretorio diretorioDestino) {
		super();
		this.diretorioBase = diretorioBase;
		this.diretorioDestino = diretorioDestino;
		this.movedFiles = new HashMap<>();
	}

	/**
	 * @return Map<String,String>
	 */
	public Map<String, String> getMovedFiles() {
		return this.movedFiles;
	}

	/**
	 * @param movedFiles
	 */
	public void setMovedFiles(Map<String, String> movedFiles) {
		this.movedFiles = movedFiles;
	}

	/**
	 * @return Diretorio
	 */
	public Diretorio getDiretorioBase() {
		return this.diretorioBase;
	}

	/**
	 * @param diretorioBase
	 */
	public void setDiretorioBase(Diretorio diretorioBase) {
		this.diretorioBase = diretorioBase;
	}

	/**
	 * @return Diretorio
	 */
	public Diretorio getDiretorioDestino() {
		return this.diretorioDestino;
	}

	/**
	 * @param diretorioDestino
	 */
	public void setDiretorioDestino(Diretorio diretorioDestino) {
		this.diretorioDestino = diretorioDestino;
	}

}
