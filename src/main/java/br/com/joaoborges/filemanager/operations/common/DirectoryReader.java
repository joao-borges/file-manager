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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.exception.InvalidDirectoryException;
import br.com.joaoborges.filemanager.exception.OperationCancelledException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.type.Extensao;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JoãoGabriel
 */
@Slf4j
public class DirectoryReader {

	/**
	 * Lê um diretorio.
	 * 
	 * @param title
	 * @param parentReference
	 * @param baseDir
	 * @return {@link DirectoryReadResult}
	 * @throws FileManagerException
	 */
	public static DirectoryReadResult readFolder(String title, java.awt.Component parentReference, Diretorio baseDir)
			throws FileManagerException {
		File currentDirectory;
		String dirBase = System.getProperty("user.home");
		if (baseDir != null) {
			dirBase = baseDir.getPath();
		}
		JFileChooser fc = new JFileChooser(dirBase);
		for (FiltroExtensoes filter : Extensao.getFCFilters()) {
			fc.addChoosableFileFilter(filter);
		}

		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setDialogTitle(title);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		fc.setApproveButtonText("Selecionar");
		fc.setApproveButtonToolTipText("Seleciona o diretório");
		fc.setMultiSelectionEnabled(false);
		int option = fc.showDialog(parentReference, "Selecionar");
		if (option == JFileChooser.APPROVE_OPTION) {
			currentDirectory = fc.getSelectedFile();
			if (currentDirectory == null) {
				log.debug("diretorio invalido");
				throw new InvalidDirectoryException();
			}
			log.debug(currentDirectory.getAbsolutePath());
			FileFilter filter = fc.getFileFilter();
			FiltroExtensoes filtro = null;
			if (filter instanceof FiltroExtensoes) {
				filtro = (FiltroExtensoes) filter;
			}
			return new DirectoryReadResult(new Diretorio(currentDirectory.getAbsolutePath()), filtro);
		}
		throw new OperationCancelledException();
	}

}
