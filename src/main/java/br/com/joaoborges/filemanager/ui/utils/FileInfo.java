package br.com.joaoborges.filemanager.ui.utils;

import java.io.Serializable;

/**
 * Objeto que encapsula informacoes dos arquivos renomeados.
 * 
 * @author Joao
 */
public class FileInfo implements Serializable, Comparable<FileInfo>, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8445992229455963249L;
	private String originalName;
	private String newName;
	private String size;

	public FileInfo(String originalName, String newName, String size) {
		super();
		this.originalName = originalName;
		this.newName = newName;
		this.size = size;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int compareTo(FileInfo o) {
		int comp = this.newName.compareTo(o.newName);
		if (comp == 0) {
			comp = this.originalName.compareTo(o.originalName);
			if (comp == 0) {
				comp = this.size.compareTo(o.size);
			}
		}
		return comp;
	}

}
