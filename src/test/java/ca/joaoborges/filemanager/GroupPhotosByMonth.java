package ca.joaoborges.filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupPhotosByMonth {

    public static void main(final String[] args) throws IOException {
        final Path base = Paths.get("/joao/fotos_organizar/organized/2021");
        final Stream<Path> list = Files.list(base);

        final Map<String, List<Path>> pathsByMonth = list.collect(Collectors.groupingBy(
            path -> path.getFileName().toString().split("-")[1]
        ));

        pathsByMonth.forEach((month, dirs) -> {
            final Path monthDir = Paths.get(base.toString(), month);
            try {
                Files.createDirectories(monthDir);
            } catch (final IOException wrapped) {
                throw new RuntimeException(wrapped);
            }

            dirs.forEach(dir -> {
                final Path dest = Paths.get(monthDir.toString(), dir.getFileName().toString());

                System.out.println(String.format("moving %s to %s", dir, dest));

                try {
                    Files.move(dir, dest);
                } catch (final IOException wrapped) {
                    throw new RuntimeException(wrapped);
                }
            });
        });
    }

}
