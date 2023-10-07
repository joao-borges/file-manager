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

package br.com.joaoborges.filemanager.operations.common;

import java.io.Serializable;

import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;

/**
 * Resultado da leitura de um diretorio.
 * 
 * @author JoãoGabriel
 */
public class DirectoryReadResult implements Serializable {

	/**  serialVersionUID */
	private static final long serialVersionUID = 3890394800413023116L;
	private Diretorio diretorio;
	private FiltroExtensoes filtro;

	/**
	 * Construtor
	 * 
	 * @param diretorio
	 * @param filtro
	 */
	public DirectoryReadResult(Diretorio diretorio, FiltroExtensoes filtro) {
		super();
		this.diretorio = diretorio;
		this.filtro = filtro;
	}

	/**
	 * @return Diretorio
	 */
	public Diretorio getDiretorio() {
		return this.diretorio;
	}

	/**
	 * @return FiltroExtensoes
	 */
	public FiltroExtensoes getFiltro() {
		return this.filtro;
	}

}
