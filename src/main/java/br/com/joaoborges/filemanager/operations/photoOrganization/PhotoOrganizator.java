package br.com.joaoborges.filemanager.operations.photoOrganization;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;

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

@Service(value = PHOTO_ORGANIZATION_OPERATION)
@Slf4j
public class PhotoOrganizator implements FileOperation<PhotoOrganizatorResult> {

    private final DecimalFormat monthFormatter = new DecimalFormat("00");

    @Override
    public PhotoOrganizatorResult execute(final Map<String, Object> params) throws FileManagerException {
        Diretorio dirBase = (Diretorio) params.get(OrganizationParamsBuilder.BASE_DIR);
        Diretorio dirDest = (Diretorio) params.get(OrganizationParamsBuilder.DEST_DIR);
        FiltroExtensoes filtro = (FiltroExtensoes) params.getOrDefault(FiltroExtensoes.class.getName(), FiltroExtensoes.allAcceptedFilter());

        PhotoOrganizatorResult resultado = new PhotoOrganizatorResult(dirBase, dirDest);

        Collection<File> conteudo = dirBase.listarConteudoRecursivo(filtro);

        log.info("Organizing {} files", conteudo.size());
        conteudo.parallelStream()
                .filter(f -> !f.isDirectory())
                .map(file -> prepareFile(file, dirDest))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(file -> {
                    final File destFile = new File(file.getDestDir().getAbsolutePath() + File.separator + file.getFile().getName());
                    final File movedFile = moveFile(file.getFile(), destFile, 0);
                    log.info("Moving {} to {}", file.getFile(), destFile);
                    resultado.getMovedFiles().put(file.getFile().getPath(), movedFile.getPath());
                });

        return resultado;
    }

    private File moveFile(final File from, final File to, int index) {
        int nextIndex = index + 1;
        try {
            FileUtils.moveFile(from, to);
        } catch (FileExistsException | FileAlreadyExistsException e) {
            return moveFile(
                    from,
                    new File(to.getParent() + File.separator + String.format("%s (%s).%s",
                                                                             getBaseName(from.getName()),
                                                                             nextIndex,
                                                                             getExtension(from.getName()))),
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

    private ZonedDateTime readDateTag(final Directory directory,
                                      final int tag) {
        if (directory == null) {
            return null;
        }
        final Date date = directory.getDate(tag, TimeZone.getDefault());
        if (date == null) {
            return null;
        }
        final ZonedDateTime dateTaken = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.of("UTC"));
        if (dateTaken.getYear() < 2000) {
            return null;
        }
        return dateTaken;
    }

    private Optional<OrganizatorFile> prepareFileBody(final File file, final Diretorio dirDest) {
        log.info("Preparing file {}", file);
        final Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            log.error("Error reading metadata for file {}", file, e);
            return Optional.empty();
        }

        final ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        ZonedDateTime dateTaken = null;
        if (directory != null) {
            dateTaken = readDateTag(directory, ExifDirectoryBase.TAG_DATETIME);
        }

        if (dateTaken == null) {
            final Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            dateTaken = readDateTag(mp4Directory, Mp4Directory.TAG_CREATION_TIME);
        }

        if (dateTaken == null) {
            final QuickTimeDirectory quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
            dateTaken = readDateTag(quickTimeDirectory, QuickTimeDirectory.TAG_CREATION_TIME);
        }

        boolean ignoreMonth = false;
        if (dateTaken == null) {
            try {
                final FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dateTaken = creationTime != null ? creationTime.toInstant().atZone(ZoneId.of("UTC")) : null;
                if (dateTaken != null && dateTaken.getYear() < 2000) {
                    dateTaken = null;
                }
                ignoreMonth = true;
            } catch (IOException e) {
                log.warn("Cannot read creationTime for {}: {}", file, e.getMessage());
            }
        }

        if (dateTaken == null) {
            log.warn("Using last modified date for {}", file);
            dateTaken = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC"));
            ignoreMonth = true;
        }

        final String year = Integer.toString(dateTaken.get(ChronoField.YEAR));
        final String month = ignoreMonth ? "outros" : String.format(
                "%s-%s",
                monthFormatter.format(dateTaken.getMonth().getValue()),
                dateTaken.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")));
        //        final String day = leftPad(Integer.toString(dateTaken.get(ChronoField.DAY_OF_MONTH)), 2, "0");

        final File destDir = Paths.get(dirDest.getPath(), year, month).toFile();
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
