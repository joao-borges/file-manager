package br.com.joaoborges.filemanager.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static br.com.joaoborges.filemanager.model.FilenameComparator.ORDEM_NOME;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Representa um diretorio de arquivos do sistema.
 * <p>
 * Destinado a manter o conteudo dos mesmos e realizar operacoes sobre este conteudo.
 *
 * @author Joao
 */
public class Diretorio {

    private static final FileFilter DIRECTORY_FILTER = File::isDirectory;

    private String path;
    private Collection<File> allContent;
    private File dir;

    /**
     * Constroi um diretorio no caminho especificado.
     */
    public Diretorio(String path) {
        super();
        this.path = path;
        this.dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new UnsupportedOperationException("Nao é permitido instanciar um diretório em um caminho inválido.");
        }
        this.allContent = new TreeSet<>(ORDEM_NOME);
        this.allContent.addAll(Arrays.asList(dir.listFiles()));
    }

    public String getPath() {
        return path;
    }

    public Collection<File> getAllContent() {
        return allContent;
    }

    public File getDiretorio() {
        return dir;
    }

    /**
     * Lista o conteudo do diretório.
     *
     * @return {@link Collection}
     */
    public List<File> listarConteudo(FilenameFilter filter) {
        return listarConteudoInterno(filter, dir);
    }

    public List<File> listarConteudoRecursivo(FilenameFilter filter) {
        final List<File> files = new ArrayList<>();
        listarConteudoRecursivo(dir, filter, files);
        files.sort(ORDEM_NOME);
        return files;
    }

    /**
     * Lista o conteudo do diretório.
     *
     * @return {@link Collection}
     */
    public List<String> listFileNames(FilenameFilter filter) {
        List<String> contentFiltered = new ArrayList<>();
        listarConteudo(filter).forEach(file -> contentFiltered.add(file.getName()));
        return contentFiltered;
    }

    private static List<File> listarConteudoInterno(final FilenameFilter filter, final File directory) {
        return Optional.ofNullable(directory.listFiles(filter)).map(Stream::of).map(s -> s.sorted(ORDEM_NOME).collect(toList())).orElse(emptyList());
    }

    private static void listarConteudoRecursivo(File dir, FilenameFilter filter, List<File> fileList) {
        fileList.addAll(listarConteudoInterno(filter, dir));

        Optional.ofNullable(dir.listFiles(DIRECTORY_FILTER)).map(Stream::of).ifPresent(s -> s.forEach(subdir -> listarConteudoRecursivo(subdir, filter, fileList)));
    }

}
