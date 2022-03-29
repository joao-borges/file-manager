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

package br.com.joaoborges.filemanager.operations.photoOrganization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.model.util.FileUtils;
import br.com.joaoborges.filemanager.operations.organization.OrganizationResult;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.operations.UIOperationResultProcessor;
import br.com.joaoborges.filemanager.ui.utils.FileInfo;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.PHOTO_ORGANIZATION_OPERATION;

/**
 * @author JoãoGabriel
 */
@Component(value = UIOperationResultProcessor.BEAN_NAME_FORMAT + PHOTO_ORGANIZATION_OPERATION)
public class PhotoOrganizationResultProcessor implements UIOperationResultProcessor<PhotoOrganizatorResult> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2413994266968479385L;

    public void processResult(PhotoOrganizatorResult result, TelaPrincipal tela) {
        // chave:novo nome, valor:nome original.
        Map<String, String> movedFiles = result.getMovedFiles();
        List<FileInfo> files = new ArrayList<FileInfo>();

        for (Map.Entry<String, String> entry : movedFiles.entrySet()) {
            String size = FileUtils.calcularTamanho(entry.getValue());
            files.add(new FileInfo(entry.getKey(), entry.getValue(), size));
        }

        Collections.sort(files);
        tela.setListaArquivos(files);
        tela.setTopLabel("Movendo de " + result.getDiretorioBase().getPath() + " para "
                         + result.getDiretorioDestino().getPath());

        JOptionPane.showMessageDialog(tela.getFrame(), files.size() + " arquivos organizados por data com sucesso.",
                                      "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
}
