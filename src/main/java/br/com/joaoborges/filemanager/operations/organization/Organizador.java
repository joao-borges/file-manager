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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.common.OperationConstants;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JoãoGabriel
 */
@Service(value = OperationConstants.ORGANIZATION_OPERATION)
@Slf4j
public class Organizador implements FileOperation<OrganizationResult> {

	/** serialVersionUID */
	private static final long serialVersionUID = 2135777385329465832L;

	public OrganizationResult execute(Map<String, Object> params) throws FileManagerException {
		Diretorio dirBase = (Diretorio) params.get(OrganizationParamsBuilder.BASE_DIR);
		Diretorio dirDest = (Diretorio) params.get(OrganizationParamsBuilder.DEST_DIR);
		FiltroExtensoes filtro = (FiltroExtensoes) params.get(FiltroExtensoes.class.getName());
		filtro = filtro != null ? filtro : FiltroExtensoes.allAcceptedFilter();
		OrganizationResult resultado = new OrganizationResult(dirBase, dirDest);

		Collection<File> conteudo = dirBase.listarConteudo(filtro);

		// mapeia o conteudo pela inicial do nome do arquivo
		Map<String, Collection<File>> mappedFiles = new TreeMap<String, Collection<File>>(String.CASE_INSENSITIVE_ORDER);
		// itera sobre os arquivos e organiza por ordem alfabetica
		for (File file : conteudo) {
			String firstChar = file.getName().substring(0, 1);
			if (!mappedFiles.containsKey(firstChar)) {
				mappedFiles.put(firstChar, new ArrayList<File>());
			}
			mappedFiles.get(firstChar).add(file);
		}

		// agora organizados, vamos mover par o diretorio de destino
		// cria as pastas por letra caso necessário
		File dirAsFile = dirDest.getDiretorio();
		for (Map.Entry<String, Collection<File>> destFolderContent : mappedFiles.entrySet()) {
			// Cria o diretorio caso nao exista
			File organizedDir = new File(dirAsFile.getAbsolutePath() + File.separator + destFolderContent.getKey());
			if (!organizedDir.exists()) {
				organizedDir.mkdirs();
			}

			// move os arquivos
			for (File originalFile : destFolderContent.getValue()) {
				File destFile = new File(organizedDir.getAbsolutePath() + File.separator + originalFile.getName());
				try {
					FileUtils.moveFile(originalFile, destFile);
				} catch (FileExistsException e) {
					log.warn(e.getMessage());
				} catch (IOException e) {
					throw new FileManagerException(e.getMessage(), e);
				}
				resultado.getMovedFiles().put(originalFile.getPath(), destFile.getPath());
			}
		}

		return resultado;
	}

	public String getOperationName() {
		return "Organizar Arquivos";
	}

	public String getOperationID() {
		return OperationConstants.ORGANIZATION_OPERATION;
	}

}
