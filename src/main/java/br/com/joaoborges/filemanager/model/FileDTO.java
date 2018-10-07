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

package br.com.joaoborges.filemanager.model;

import java.io.File;
import java.io.Serializable;

import br.com.joaoborges.filemanager.type.Extensao;

/**
 * Agrupa dados de um arquivo.
 * 
 * @author JoãoGabriel
 */
public class FileDTO implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 419275423063428717L;

	private File file;
	private Extensao extension;

	private Diretorio contentDirectory;

	/**
	 * Construtor
	 * 
	 * @param file
	 * @param extension
	 * @param contentDirectory
	 */
	public FileDTO(File file, Extensao extension, Diretorio contentDirectory) {
		super();
		this.file = file;
		this.extension = extension;
		this.contentDirectory = contentDirectory;
	}

	/**
	 * @return File
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return Extensao
	 */
	public Extensao getExtension() {
		return this.extension;
	}

	/**
	 * @return Diretorio
	 */
	public Diretorio getContentDirectory() {
		return this.contentDirectory;
	}

}
