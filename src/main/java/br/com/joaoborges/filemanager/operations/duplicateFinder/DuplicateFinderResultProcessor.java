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

package br.com.joaoborges.filemanager.operations.duplicateFinder;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.model.util.FileUtils;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.operations.UIOperationResultProcessor;
import br.com.joaoborges.filemanager.ui.utils.FileInfo;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.DUPLICATE_FINDER_OPERATION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 * @author JoãoGabriel
 */
@Component(value = UIOperationResultProcessor.BEAN_NAME_FORMAT + DUPLICATE_FINDER_OPERATION)
@Slf4j
public class DuplicateFinderResultProcessor implements UIOperationResultProcessor<DuplicateFinderResult> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2413994266968479385L;

    public void processResult(DuplicateFinderResult result, TelaPrincipal tela) {
        // chave:novo nome, valor:nome original.
        List<FileInfo> files = result.getFiles()
            .entrySet()
            .stream()
            .map(entry -> new FileInfo(entry.getKey(), entry.getValue(), FileUtils.calcularTamanho(entry.getValue())))
            .sorted()
            .collect(Collectors.toList());

        tela.setListaArquivos(files);
        tela.setTopLabel("Arquivos duplicados em " + result.getDiretorio().getPath());

        final int confirmResult = showConfirmDialog(tela.getFrame(), files.size() + " arquivos afetados. Deseja apagá-los?",
                                                    "Informação", JOptionPane.YES_NO_OPTION);

        if (confirmResult == JOptionPane.NO_OPTION) {
            return;
        }

        files.forEach(file -> {
            log.debug("Apagando {}", file.getOriginalName());
            deleteQuietly(new File(file.getOriginalName()));
        });

        JOptionPane.showMessageDialog(tela.getFrame(), files.size() + " arquivos apagados com sucesso.",
                                      "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
}
