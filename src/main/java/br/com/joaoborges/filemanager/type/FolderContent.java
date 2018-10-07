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

package br.com.joaoborges.filemanager.type;

import java.io.Serializable;
import java.util.List;

/**
 * Conteudo de um diretorio, pode serum subdiretorio ou um arquivo.
 * 
 * @author João
 */
public class FolderContent implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -6016471172439929351L;
	private String name;
	private List<FolderContent> contentInside;
	private boolean isDirectory;

	/**
	 * Construtor
	 * 
	 * @param name
	 * @param isDirectory
	 */
	public FolderContent(String name, boolean isDirectory) {
		super();
		this.name = name;
		this.isDirectory = isDirectory;
	}

	/**
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return List<FolderContent>
	 */
	public List<FolderContent> getContentInside() {
		return this.contentInside;
	}

	/**
	 * @param contentInside
	 */
	public void setContentInside(List<FolderContent> contentInside) {
		this.contentInside = contentInside;
	}

	/**
	 * @return boolean
	 */
	public boolean isDirectory() {
		return this.isDirectory;
	}

	/**
	 * @param isDirectory
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

}
