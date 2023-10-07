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

import java.util.HashMap;
import java.util.Map;

import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Resultado da operação de organização.
 *
 * @author JoãoGabriel
 */
@RequiredArgsConstructor
@Getter
@Setter
public class DuplicateFinderResult implements OperationResult {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5609682745330701000L;

    private final Map<String, String> files = new HashMap<>();
    private final Diretorio diretorio;

}
