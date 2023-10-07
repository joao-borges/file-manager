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

package br.com.joaoborges.filemanager.operations.renaming;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.RENAME_OPERATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.model.util.FileUtils;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador.RenamingResult;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.operations.UIOperationResultProcessor;
import br.com.joaoborges.filemanager.ui.utils.FileInfo;

/**
 * Processador de saída do resultado da operaçao de renomear
 * 
 * @author JoãoGabriel
 */
@Component(value = UIOperationResultProcessor.BEAN_NAME_FORMAT + RENAME_OPERATION)
public class RenamingResultProcessor implements UIOperationResultProcessor<RenamingResult> {

	/** serialVersionUID */
	private static final long serialVersionUID = 7421930351019628390L;

	public void processResult(RenamingResult result, TelaPrincipal tela) {
		// chave:novo nome, valor:nome original.
		Map<String, String> renamedFiles = result.getRenamedFiles();
		List<FileInfo> files = new ArrayList<FileInfo>();

		for (Map.Entry<String, String> entry : renamedFiles.entrySet()) {
			String size = FileUtils.calcularTamanho(entry.getKey());
			files.add(new FileInfo(entry.getValue(), entry.getKey(), size));
		}

		Collections.sort(files);
		tela.setListaArquivos(files);
		tela.setTopLabel(result.getCurrentDirectory().getPath());

		JOptionPane.showMessageDialog(tela.getFrame(), files.size() + " arquivos renomeados com sucesso.",
				"Informação", JOptionPane.INFORMATION_MESSAGE);
	}

}
