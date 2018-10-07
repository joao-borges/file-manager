package br.com.joaoborges.filemanager.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Representa um diretorio de arquivos do sistema.
 * <p>
 * Destinado a manter o conteudo dos mesmos e realizar operacoes sobre este conteudo.
 * 
 * @author Joao
 */
public class Diretorio {

	private String path;
	private Collection<File> allContent;
	private File dir;

	/**
	 * Constroi um diretorio no caminho especificado.
	 * 
	 * @param path
	 */
	public Diretorio(String path) {
		super();
		this.path = path;
		this.dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new UnsupportedOperationException("Nao é permitido instanciar um diretório em um caminho inválido.");
		}
		this.allContent = new TreeSet<>(FilenameComparator.ORDEM_NOME);
		this.allContent.addAll(Arrays.asList(dir.listFiles()));
	}

	public String getPath() {
		return path;
	}

	public Collection<File> getAllContent() {
		return allContent;
	}

	public File getDiretorio() {
		return dir;
	}

	/**
	 * Lista o conteudo do diretório.
	 *
	 * @param filter
	 * @return {@link Collection}
	 */
	public List<File> listarConteudo(FilenameFilter filter) {
		return listarConteudo(this.dir, filter, false);
	}

	public List<File> listarConteudoRecursivo(FilenameFilter filter) {
		return listarConteudo(this.dir, filter, true);
	}

	/**
	 * Lista o conteudo do diretório.
	 *
	 * @param filter
	 * @return {@link Collection}
	 */
	public List<String> listFileNames(FilenameFilter filter) {
		List<String> contentFiltered = new ArrayList<>();
		listarConteudo(filter).forEach(file -> contentFiltered.add(file.getName()));
		return contentFiltered;
	}

	private static List<File> listarConteudo(File dir, FilenameFilter filter, boolean recursive) {
		List<File> contentFiltered = new ArrayList<>();
		Stream.of(dir.listFiles(filter)).forEach(file -> {
			if (file.isDirectory() && recursive) {
				contentFiltered.addAll(listarConteudo(file, filter, true));
			} else {
				contentFiltered.add(file);
			}
		});
		Collections.sort(contentFiltered, FilenameComparator.ORDEM_NOME);
		return contentFiltered;
	}
}
