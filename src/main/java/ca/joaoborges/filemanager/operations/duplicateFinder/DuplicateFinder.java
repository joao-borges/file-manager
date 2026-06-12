package ca.joaoborges.filemanager.operations.duplicateFinder;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ca.joaoborges.filemanager.operations.common.OperationConstants.DUPLICATE_FINDER_OPERATION;

@Service(value = DUPLICATE_FINDER_OPERATION)
@Slf4j
public class DuplicateFinder implements FileOperation<DuplicateFinderResult> {

    private static final Pattern FILE_WITH_INDEX = Pattern.compile("(.*)\\s\\(\\d*\\)");

    @Override
    public DuplicateFinderResult execute(final Map<String, Object> params) throws FileManagerException {
        final Directory directory = (Directory) params.get(Directory.class.getName());
        final DuplicateFinderResult result = new DuplicateFinderResult(directory);

        final File md5File = new File(directory.getDirectory(), "md5sumfiles.txt");
        if (!md5File.exists()) {
            log.info("md5 file not found");
            return result;
        }

        final List<FileWithHash> files = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(md5File)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] md5AndPath = line.trim().split("\\s", 2);
                if (md5AndPath.length != 2) {
                    log.error("invalid line: {}", line);
                    continue;
                }
                files.add(new FileWithHash(new File(directory.getDirectory() + File.separator + md5AndPath[1].trim()), md5AndPath[0]));
            }
        } catch (final IOException wrapped) {
            throw new FileManagerException(wrapped.getMessage(), wrapped);
        }

        log.info("checking a total of {} files", files.size());
        files.stream()
                .collect(Collectors.groupingBy(FileWithHash::md5sum))
                .values()
                .stream()
                .filter(group -> group.size() > 1)
                .flatMap(List::stream)
                .filter(file -> FILE_WITH_INDEX.matcher(FilenameUtils.getBaseName(file.file().getName())).matches())
                .forEach(file -> result.getFiles().put(file.file().toString(), file.md5sum()));

        return result;
    }

    @Override
    public String getOperationName() {
        return "Clean Duplicate Files";
    }

    @Override
    public String getOperationID() {
        return DUPLICATE_FINDER_OPERATION;
    }

    private record FileWithHash(File file, String md5sum) {}

}
