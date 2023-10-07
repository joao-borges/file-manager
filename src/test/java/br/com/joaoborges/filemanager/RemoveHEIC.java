package br.com.joaoborges.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoveHEIC {

    public static void main(String[] args) throws IOException {
        final List<File> allFiles = Stream.of(
            Files.list(Paths.get("/joao/Fotos")),
            Files.list(Paths.get("/joao/fotos_organizar"))
        ).flatMap(Function.identity())
            .map(Path::toFile)
            .map(file -> {
                if (file.isDirectory()) {
                    log.debug("adding directory {}", file);

                    return new ArrayList<>(FileUtils.listFiles(file, null, true));
                }

                log.debug("adding file {}", file);
                return List.of(file);
            }).flatMap(Collection::stream)
            .filter(file -> file.getName().contains(".HEIC"))
            .collect(Collectors.toList());

        log.debug("Total {} Files with HEIC", allFiles.size());

        allFiles.forEach(file -> {
            final File newFile = new File(file.getParent(), file.getName().replace(".HEIC", ""));
            log.debug("renaming {} to {}", file, newFile);
            file.renameTo(newFile);
        });
    }
}
