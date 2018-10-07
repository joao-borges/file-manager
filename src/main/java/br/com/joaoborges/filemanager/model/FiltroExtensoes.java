package br.com.joaoborges.filemanager.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.com.joaoborges.filemanager.type.Extensao;

/**
 * Filtro de arquivos por extensao.
 * 
 * @author Joao
 */
public class FiltroExtensoes extends javax.swing.filechooser.FileFilter implements FilenameFilter, FileFilter,
		Comparable<FiltroExtensoes> {

	private String[] extensionsAccepted;
	private String description;
	private boolean acceptDirectories = false;

	/**
	 * Instancia um filtro que aceita todas as extensoes.
	 *
	 * @return {@link FiltroExtensoes}
	 */
	public static FiltroExtensoes allAcceptedFilter() {
		return new FiltroExtensoes(Extensao.asStrings());
	}

	/**
	 * Construtor que define quais sao as extensoes aceitas pelo filtro.
	 * 
	 * @param extensoes
	 *            Extnsoes aceitas.
	 */
	public FiltroExtensoes(List<String> extensoes) {
		if (extensoes == null || extensoes.size() == 0) {
			throw new UnsupportedOperationException(
					"Nao é permitido instanciar um Filtro de Extensões sem especificar as extensões aceitas.");
		}
		this.extensionsAccepted = extensoes.toArray(new String[0]);
	}

	/**
	 * Verifica se um arquivo e aceito, ou seja, verifica se sua extensao e permitida pelo filtro.
	 */
	public boolean accept(File dir, String name) {
		File file = new File(dir.getPath() + File.separator + name);
		if (file.isDirectory()) {
			return this.acceptDirectories;
		}

		boolean accepted = false;
		for (String extensao : this.extensionsAccepted) {
			if (name.toLowerCase().endsWith(extensao.toLowerCase())) {
				accepted = true;
				break;
			}
		}
		return accepted;
	}

	public boolean accept(File pathname) {
		return this.accept(pathname, pathname.getName());
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public int compareTo(FiltroExtensoes o) {
		return this.description.compareTo(o.description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addExtensao(String extensao) {
		List<String> extensoes = new ArrayList<>(Arrays.asList(this.extensionsAccepted));
		extensoes.add(extensao);
		this.extensionsAccepted = extensoes.toArray(new String[0]);
	}

	public Set<Extensao> getExtensionsAccepted() {
		Set<Extensao> extensoes = new TreeSet<>();
		for (String ext : this.extensionsAccepted) {
			extensoes.add(Extensao.valueOf(ext.toUpperCase()));
		}
		return extensoes;
	}

	@Override
	public String toString() {
		return "Filtro: "
				+ Arrays.toString(getExtensionsAccepted().toArray(new Extensao[0]));
	}

	/**
	 * @param acceptDirectories
	 */
	public void setAcceptDirectories(boolean acceptDirectories) {
		this.acceptDirectories = acceptDirectories;
	}
}
