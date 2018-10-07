package br.com.joaoborges.filemanager.model;

import java.io.File;
import java.util.Comparator;

/**
 * Comparador de nomes de arquivos, destinados a ordenar colecoes.
 * 
 * @author Joao
 */
public enum FilenameComparator implements Comparator<File> {

	ORDEM_NOME {

		@Override
		public int compare(File o1, File o2) {
			return o1.getName().toLowerCase().compareTo(
					o2.getName().toLowerCase());
		}

	};

	public abstract int compare(File o1, File o2);

}
