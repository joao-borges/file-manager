package br.com.joaoborges.filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgrupaPorMes {

    public static void main(String[] args) throws IOException {
        final Path base = Paths.get("/joao/fotos_organizar/organized/2021");
        final Stream<Path> list = Files.list(base);

        final Map<String, List<Path>> pathsByMonth = list.collect(Collectors.groupingBy(
            path -> path.getFileName().toString().split("-")[1]
        ));

        pathsByMonth.forEach((month, dirs) -> {
            Path monthDir = Paths.get(base.toString(), month);
            try {
                Files.createDirectories(monthDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dirs.forEach(dir -> {
                Path dest = Paths.get(monthDir.toString(), dir.getFileName().toString());

                System.out.println(String.format("moving %s to %s", dir, dest));

                try {
                    Files.move(dir, dest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
