package br.com.joaoborges.filemanager.operations.photoOrganization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import br.com.joaoborges.filemanager.operations.organization.OrganizationParamsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.PHOTO_ORGANIZATION_OPERATION;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.leftPad;

@Service(value = PHOTO_ORGANIZATION_OPERATION)
@Slf4j
public class PhotoOrganizator implements FileOperation<PhotoOrganizatorResult> {

    @Override
    public PhotoOrganizatorResult execute(final Map<String, Object> params) throws FileManagerException {
        Diretorio dirBase = (Diretorio) params.get(OrganizationParamsBuilder.BASE_DIR);
        Diretorio dirDest = (Diretorio) params.get(OrganizationParamsBuilder.DEST_DIR);
        FiltroExtensoes filtro = (FiltroExtensoes) params.get(FiltroExtensoes.class.getName());
        filtro = filtro != null ? filtro : FiltroExtensoes.allAcceptedFilter();
        PhotoOrganizatorResult resultado = new PhotoOrganizatorResult(dirBase, dirDest);

        Collection<File> conteudo = dirBase.listarConteudoRecursivo(filtro);

        conteudo.stream()
            .map(file -> prepareFile(file, dirDest))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(file -> {
                final File destFile = new File(file.getDestDir().getAbsolutePath() + File.separator + file.getFile().getName());
                final File movedFile = moveFile(file.getFile(), destFile, 0);
                resultado.getMovedFiles().put(file.getFile().getPath(), movedFile.getPath());
            });

        return resultado;
    }

    private File moveFile(final File from, final File to, int index) {
        int nextIndex = index + 1;
        try {
            FileUtils.moveFile(from, to);
        } catch (FileExistsException e) {
            return moveFile(
                from,
                new File(to.getParent() + File.separator + String.format("%s (%s).%s", getBaseName(from.getName()), nextIndex, getExtension(from.getName()))),
                nextIndex
            );
        } catch (IOException | RuntimeException e) {
            throw new FileManagerException(e.getMessage(), e);
        }
        return to;
    }

    private Optional<OrganizatorFile> prepareFile(final File file, final Diretorio dirDest) {
        try {
            return prepareFileBody(file, dirDest);
        } catch (RuntimeException e) {
            throw new FileManagerException(e.getMessage(), e);
        }
    }

    private Optional<OrganizatorFile> prepareFileBody(final File file, final Diretorio dirDest) {
        final Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            log.error("Error reading metadata for file {}", file, e);
            return Optional.empty();
        }

        final ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        Date date = null;
        if (directory != null) {
            date = directory.getDate(ExifDirectoryBase.TAG_DATETIME);
        }

        if (date == null) {
            log.warn("Using last modified date for {}", file);
            date = new Date(file.lastModified());
        }

        final Instant instantTaken = Instant.ofEpochMilli(date.getTime());
        final ZonedDateTime dateTaken = ZonedDateTime.ofInstant(instantTaken, ZoneId.of("UTC"));
        final String year = Integer.toString(dateTaken.get(ChronoField.YEAR));
        final String month = leftPad(Integer.toString(dateTaken.get(ChronoField.MONTH_OF_YEAR)), 2, "0");
        final String day = leftPad(Integer.toString(dateTaken.get(ChronoField.DAY_OF_MONTH)), 2, "0");

        final File destDir = Paths.get(dirDest.getPath(), year, String.format("%s-%s", day, month)).toFile();
        if (!destDir.exists()) {
            final boolean mkdirs = destDir.mkdirs();
            if (!mkdirs) {
                log.error("Could not create destination dir {} for file {}", destDir, file);
                return Optional.empty();
            }
        }

        return Optional.of(new OrganizatorFile(file, destDir));
    }

    @Override
    public String getOperationName() {
        return "Organizar Imagens por Data";
    }

    @Override
    public String getOperationID() {
        return PHOTO_ORGANIZATION_OPERATION;
    }

    @RequiredArgsConstructor
    @Getter
    private static class OrganizatorFile {

        private final File file;
        private final File destDir;

    }
}
