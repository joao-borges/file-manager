package br.com.joaoborges.filemanager.operations.duplicateFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.DUPLICATE_FINDER_OPERATION;

@Service(value = DUPLICATE_FINDER_OPERATION)
@Slf4j
public class DuplicateFinder implements FileOperation<DuplicateFinderResult> {

    private static final Pattern FILE_WITH_INDEX = Pattern.compile("(.*)\\s\\(\\d*\\)");

    @Override
    public DuplicateFinderResult execute(final Map<String, Object> params) throws FileManagerException {
        Diretorio diretorio = (Diretorio) params.get(Diretorio.class.getName());
        DuplicateFinderResult resultado = new DuplicateFinderResult(diretorio);

        final File md5File = new File(diretorio.getDiretorio(), "md5sumfiles.txt");
        if (!md5File.exists()) {
            log.info("Arquivo de md5 não encontrado");
            return resultado;
        }

        final List<FileWithHash> files = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(md5File)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] md5AndPath = line.trim().split("\\s", 2);
                if (md5AndPath.length != 2) {
                    log.error("linha inválida: {}", line);
                    continue;
                }
                files.add(new FileWithHash(new File(diretorio.getDiretorio() + File.separator + md5AndPath[1].trim()), md5AndPath[0]));
            }
        } catch (IOException e) {
            throw new FileManagerException(e.getMessage(), e);
        }

        log.info("verificando total de {} arquivos", files.size());
        files.stream()
            .collect(Collectors.groupingBy(FileWithHash::getMd5sum))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> {
                entry.getValue()
                    .stream()
                    .filter(file -> FILE_WITH_INDEX.matcher(FilenameUtils.getBaseName(file.getFile().getName())).matches())
                    .forEach(file -> resultado.getFiles().put(file.getFile().toString(), file.getMd5sum()));
            });

        return resultado;
    }

    @Override
    public String getOperationName() {
        return "Limpar Arquivos Duplicados";
    }

    @Override
    public String getOperationID() {
        return DUPLICATE_FINDER_OPERATION;
    }

    @Getter
    @RequiredArgsConstructor
    private static class FileWithHash {

        private final File file;
        private final String md5sum;
    }
}
