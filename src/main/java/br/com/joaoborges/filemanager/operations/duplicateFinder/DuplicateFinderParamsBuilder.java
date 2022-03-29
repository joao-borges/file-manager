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

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.common.DirectoryReadResult;
import br.com.joaoborges.filemanager.operations.common.OperationConstants;
import br.com.joaoborges.filemanager.operations.interfaces.OperationParamsBuilder;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

import static br.com.joaoborges.filemanager.operations.common.DirectoryReader.readFolder;

/**
 * Monta os parämetros para a operacao de organizacao.
 *
 * @author JoãoGabriel
 */
@Component(value = OperationParamsBuilder.BEAN_NAME_FORMAT + OperationConstants.DUPLICATE_FINDER_OPERATION)
public class DuplicateFinderParamsBuilder implements OperationParamsBuilder {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4829235059186660489L;

    @Autowired
    private TelaPrincipal tela;

    // sistema é single-user entao eu guardar o estado aqui nao é nada mal.
    private Diretorio lastReadDirectory;

    public Map<String, Object> buildParams() throws FileManagerException {
        DirectoryReadResult directoryBase = readFolder("Selecione o diretório", this.tela.getFrame(), this.lastReadDirectory);
        this.lastReadDirectory = directoryBase.getDiretorio();

        return Map.of(Diretorio.class.getName(),
                      lastReadDirectory,
                      FiltroExtensoes.class.getName(),
                      Optional.ofNullable(directoryBase.getFiltro()).orElse(FiltroExtensoes.allAcceptedFilter()));
    }

}
