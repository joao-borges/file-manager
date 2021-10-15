/*
 * Copyright (c) 1999-2018 Touch Tecnologia e Informatica Ltda.
 *
 * R. Gomes de Carvalho, 1666, 3o. Andar, Vila Olimpia, Sao Paulo, SP, Brasil.
 *
 * Todos os direitos reservados.
 * Este software e confidencial e de propriedade da Touch Tecnologia e Informatica Ltda. (Informacao Confidencial)
 * As informacoes contidas neste arquivo nao podem ser publicadas, e seu uso esta limitado de acordo
 * com os termos do contrato de licenca.
 */
package br.com.joaoborges.filemanager.operations.extraction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileExistsException;
import org.springframework.stereotype.Service;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.common.OperationConstants;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import br.com.joaoborges.filemanager.operations.organization.OrganizationParamsBuilder;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.EXTRACTION_OPERATION;
import static org.apache.commons.io.FileUtils.moveFile;

/**
 * @author joaoborges
 */
@Service(value = OperationConstants.EXTRACTION_OPERATION)
@Slf4j
public class Extrator implements FileOperation<ExtractionResult> {

    @Override
    public ExtractionResult execute(Map<String, Object> params) throws FileManagerException {
        Diretorio dirBase = (Diretorio) params.get(OrganizationParamsBuilder.BASE_DIR);
        Diretorio dirDest = (Diretorio) params.get(OrganizationParamsBuilder.DEST_DIR);
        FiltroExtensoes filtro = (FiltroExtensoes) params.get(FiltroExtensoes.class.getName());
        filtro.setAcceptDirectories(true);
        ExtractionResult result = new ExtractionResult(dirBase, dirDest);

        // varre recursivamente o diretorio base procurando arquivos nas extensoes informadas
        // caso ache, move para o mesmo caminho no diretorio de destino
        dirBase.listarConteudoRecursivo(filtro).forEach(file -> {
            String newFileDirPath = file.getPath().replace(dirBase.getPath(), dirDest.getPath());
            newFileDirPath = newFileDirPath.substring(0, newFileDirPath.lastIndexOf(File.separator));
            File newFileDir = new File(newFileDirPath);
            newFileDir.mkdirs();
            File newFile = new File(newFileDir.getPath() + File.separator + file.getName());
            try {
                moveFile(file, newFile);
                result.getMovedFiles().put(file.getPath(), newFile.getPath());
            } catch (FileExistsException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                throw new FileManagerException(e.getMessage(), e);
            }
        });
        return result;
    }

    @Override
    public String getOperationName() {
        return "Extrair Arquivos de Determinado Tipo";
    }

    @Override
    public String getOperationID() {
        return EXTRACTION_OPERATION;
    }
}
