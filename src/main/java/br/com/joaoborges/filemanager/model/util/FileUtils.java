
package br.com.joaoborges.filemanager.model.util;

import java.io.File;
import br.com.joaoborges.filemanager.type.ReplacingConstants;

/**
 * Classe com operacoes uteis para arquivos.
 * 
 * @author Joao
 * @version 28/09/2010 20:11:50
 */
public class FileUtils {
	
	/**
	 * Calcula o tamanho de um arquivo, em MB.
	 * 
	 * @param fileName
	 * @return
	 */
	public static String calcularTamanho (String fileName) {
		File renamedFile = new File(fileName);
		String size = "";
		double fileSize = renamedFile.length() / 1024;
		if (fileSize <= 1024) {
			size = fileSize + " KB";
		} else {
			double fileSizeD = fileSize / 1024;
			size += fileSizeD;
			int index = size.lastIndexOf(".");
			if (size.substring(index).length() > 1) {
				size = size.substring(0, index) + ReplacingConstants.POINT + size.substring(index + 1, index + 2) + " MB";
			}
		}
		return size;
	}
	
}
