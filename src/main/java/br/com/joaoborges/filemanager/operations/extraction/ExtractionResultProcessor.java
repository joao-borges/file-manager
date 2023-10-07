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

package br.com.joaoborges.filemanager.operations.extraction;

import static br.com.joaoborges.filemanager.model.util.FileUtils.calcularTamanho;
import static br.com.joaoborges.filemanager.operations.common.OperationConstants.EXTRACTION_OPERATION;
import static br.com.joaoborges.filemanager.ui.operations.UIOperationResultProcessor.BEAN_NAME_FORMAT;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.operations.organization.OrganizationResult;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.operations.UIOperationResultProcessor;
import br.com.joaoborges.filemanager.ui.utils.FileInfo;

/**
 * @author JoãoGabriel
 */
@Component(value = BEAN_NAME_FORMAT + EXTRACTION_OPERATION)
public class ExtractionResultProcessor implements UIOperationResultProcessor<ExtractionResult> {

	/** serialVersionUID */
	private static final long serialVersionUID = 2413994266968479385L;

	public void processResult(ExtractionResult result, TelaPrincipal tela) {
		// chave:novo nome, valor:nome original.
		Map<String, String> movedFiles = result.getMovedFiles();
		List<FileInfo> files = new ArrayList<>();

		movedFiles.entrySet().forEach(entry -> {
			files.add(new FileInfo(entry.getValue(), entry.getKey(), calcularTamanho(entry.getValue())));
		});

		Collections.sort(files);
		tela.setListaArquivos(files);
		tela.setTopLabel("Movendo de " + result.getDiretorioBase().getPath() + " para "
				+ result.getDiretorioDestino().getPath());

		showMessageDialog(tela.getFrame(), files.size() + " arquivos movidos com sucesso.","Informação", INFORMATION_MESSAGE);
	}
}
