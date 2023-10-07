package br.com.joaoborges.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncontrarDuplicadosMultiplo {

    public static void main(String[] args) throws IOException {
        final JdbcTemplate template = createDB();
        createTable(template, true);

        final List<File> allFiles = retrieveAllFiles();

        final List<File> allFilesWithNoMd5 = filterFilesWithoutMD5(template, allFiles);

        generateMD5AndInsertDB(template, allFilesWithNoMd5);

        countDBFiles(template);

        final List<FileGroupByMd5> fileGroups = getMd5DuplicatedFiles(template);
        fileGroups.forEach(FileGroupByMd5::removeDuplicates);
    }

    private static JdbcTemplate createDB() {
        final DataSource db = new DriverManagerDataSource("jdbc:hsqldb:file:/opt/workspace/file-manager/db", "sa", "");
        return new JdbcTemplate(db);
    }

    private static void createTable(final JdbcTemplate template, boolean drop) {
        log.debug("create table");
        if (drop) {
            template.execute("drop table MD5_FILES");
        }
        template.execute("create table if not exists MD5_FILES(file_path varchar(2000), md5_sum varchar(1024))");
    }

    private static List<File> retrieveAllFiles() throws IOException {
        final List<File> allFiles = Stream.of(
                Files.list(Paths.get("/joao/Fotos/2019")),
                Files.list(Paths.get("/joao/Fotos/2020")),
                Files.list(Paths.get("/joao/Fotos/2021")),
                Files.list(Paths.get("/joao/fotos_organizar/organized/2019")),
                Files.list(Paths.get("/joao/fotos_organizar/organized/2020")),
                Files.list(Paths.get("/joao/fotos_organizar/organized/2021"))
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
            .collect(Collectors.toList());

        log.debug("Total files: {}", allFiles.size());
        return allFiles;
    }

    private static List<File> filterFilesWithoutMD5(final JdbcTemplate template, final List<File> allFiles) {
        final String query = "select count(1) from md5_files where file_path = '%s'";
        final List<File> allFilesWithNoMd5 = allFiles.stream()
            .filter(file -> template.queryForList(String.format(query, file.toString()), Integer.class).stream().findFirst().orElse(0) == 0)
            .collect(Collectors.toList());

        log.debug("Total files to generate md5: {}", allFilesWithNoMd5.size());
        return allFilesWithNoMd5;
    }

    private static void generateMD5AndInsertDB(final JdbcTemplate template, final List<File> allFilesWithNoMd5) {
        final String insertQuery = "insert into md5_files (file_path, md5_sum) values ('%s', '%s')";
        final List<String> inserts = allFilesWithNoMd5.stream()
            .map(FileWithHash::new)
            .filter(f -> f.getMd5sum() != null)
            .map(f -> String.format(insertQuery, f.filePath, f.md5sum))
            .collect(Collectors.toList());

        inserts.forEach(insert -> {
            log.debug("inserting: {}", insert);
            template.execute(insert);
        });
    }

    private static void countDBFiles(final JdbcTemplate template) {
        log.debug("Total files with md5 DB: {}",
                  template.query("select count(1) from md5_files", (ResultSetExtractor<Integer>) rs -> {
                      rs.next();
                      return rs.getInt(1);
                  }));
    }

    @Data
    private static class FileWithHash {

        private String filePath;
        private String md5sum;

        public FileWithHash(final File file) {
            this.filePath = file.toString();
            log.debug("generating md5 {}", filePath);

            try {
                final Process process = new ProcessBuilder("md5sum", filePath).start();
                process.waitFor();

                final String output = IOUtils.toString(process.getInputStream());
                final String[] md5File = output.split(" ");
                if (md5File.length < 2) {
                    log.error("could not generate md5 for {}, invalid output: {}", filePath, output);
                }
                this.md5sum = md5File[0];
            } catch (IOException | InterruptedException e) {
                log.error("could not generate md5 for {}: {}", filePath, e.getMessage());
            }
        }
    }

    private static List<FileGroupByMd5> getMd5DuplicatedFiles(final JdbcTemplate template) {
        // query the files with md5 duplicated
        final String groupQuery = "select md5_sum, array_agg(file_path) as paths from md5_files group by md5_sum";
        final List<FileGroupByMd5> fileGroups = template.queryForList(groupQuery)
            .stream()
            .map(FileGroupByMd5::new)
            .filter(fg -> fg.files.size() > 1)
            .collect(Collectors.toList());

        log.debug("we have {} duplicated files", fileGroups.size());
        return fileGroups;
    }

    @Getter
    @Setter
    public static class FileGroupByMd5 {
        private String md5Sum;
        private List<File> files;

        @SneakyThrows
        public FileGroupByMd5(final Map<String, Object> dbResult) {
            this.md5Sum = dbResult.get("md5_sum").toString();

            final Array array = (Array) dbResult.get("paths");
            final Object[] files = Optional.ofNullable((Object[]) array.getArray()).orElse(ArrayUtils.EMPTY_OBJECT_ARRAY);

            this.files = Arrays.stream(files).map(Object::toString).map(File::new).collect(Collectors.toList());
        }

        void removeDuplicates() {
            final List<File> toBeRemoved = new ArrayList<>();
            // try to keep the file with better name
            files.forEach(file -> {
                if (StringUtils.containsIgnoreCase(file.getName(), "heic")) {
                    toBeRemoved.add(file);
                }

                if (!file.getName().startsWith("IMG")) {
                    toBeRemoved.add(file);
                }

                if (file.getAbsolutePath().contains("fotos_organizar")) {
                    toBeRemoved.add(file);
                }
            });

            boolean choicesDone = toBeRemoved.size() == (files.size() - 1);
            // no good 1st
            if (!choicesDone) {
                toBeRemoved.clear();

                files.forEach(file -> {
                    if (!file.getName().startsWith("IMG")) {
                        toBeRemoved.add(file);
                    }

                    if (file.getName().contains("fotos_organizar")) {
                        toBeRemoved.add(file);
                    }
                });
            }

            choicesDone = toBeRemoved.size() == (files.size() - 1);
            // still no good
            if (!choicesDone) {
                toBeRemoved.clear();

                files.forEach(file -> {
                    if (file.getName().contains("fotos_organizar")) {
                        toBeRemoved.add(file);
                    }
                });
            }

            choicesDone = toBeRemoved.size() == (files.size() - 1);
            // gave up, choose the 1st
            if (!choicesDone) {
                toBeRemoved.clear();

                for (int i = 1; i < files.size(); i++) {
                    toBeRemoved.add(files.get(i));
                }
            }

            log.debug("removing files {}", toBeRemoved);
            toBeRemoved.forEach(file -> {
                FileUtils.deleteQuietly(file);
                try {
                    if (Files.list(file.getParentFile().toPath()).findAny().isEmpty()) {
                        FileUtils.deleteDirectory(file.getParentFile());
                    }
                } catch (IOException e) {
                    log.error("Could not delete directory {}: {}", file.getParentFile(), e.getMessage());
                }
            });
        }
    }
}
