package ca.joaoborges.filemanager.operations.photoOrganization;

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
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.Directory;
import ca.joaoborges.filemanager.model.ExtensionFilter;
import ca.joaoborges.filemanager.operations.interfaces.FileOperation;
import lombok.extern.slf4j.Slf4j;

import static ca.joaoborges.filemanager.operations.common.OperationConstants.PHOTO_ORGANIZATION_OPERATION;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Service(value = PHOTO_ORGANIZATION_OPERATION)
@Slf4j
public class PhotoOrganizer implements FileOperation<PhotoOrganizerResult> {

    private final DecimalFormat monthFormatter = new DecimalFormat("00");

    @Override
    public PhotoOrganizerResult execute(final Map<String, Object> params) throws FileManagerException {
        final Directory dirBase = (Directory) params.get("BASE_DIR");
        final Directory dirDest = (Directory) params.get("DEST_DIR");
        final ExtensionFilter filter = (ExtensionFilter) params.getOrDefault(ExtensionFilter.class.getName(), ExtensionFilter.allAcceptedFilter());

        final PhotoOrganizerResult result = new PhotoOrganizerResult(dirBase, dirDest);

        final Collection<File> contents = dirBase.listContentsRecursively(filter);

        log.info("Organizing {} files", contents.size());
        contents.parallelStream()
                .filter(file -> !file.isDirectory())
                .map(file -> prepareFile(file, dirDest))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(prepared -> {
                    final File destFile = new File(prepared.destDir().getAbsolutePath() + File.separator + prepared.file().getName());
                    final File movedFile = moveFile(prepared.file(), destFile, 0);
                    log.info("Moving {} to {}", prepared.file(), destFile);
                    result.getMovedFiles().put(prepared.file().getPath(), movedFile.getPath());
                });

        return result;
    }

    private File moveFile(final File from, final File to, final int index) {
        final int nextIndex = index + 1;
        try {
            FileUtils.moveFile(from, to);
        } catch (final FileExistsException | FileAlreadyExistsException ignored) {
            return moveFile(
                    from,
                    new File(to.getParent() + File.separator + String.format("%s (%s).%s",
                                                                             getBaseName(from.getName()),
                                                                             nextIndex,
                                                                             getExtension(from.getName()))),
                    nextIndex
            );
        } catch (final IOException | RuntimeException wrapped) {
            throw new FileManagerException(wrapped.getMessage(), wrapped);
        }
        return to;
    }

    private Optional<OrganizerFile> prepareFile(final File file, final Directory dirDest) {
        try {
            return prepareFileBody(file, dirDest);
        } catch (final RuntimeException wrapped) {
            throw new FileManagerException(wrapped.getMessage(), wrapped);
        }
    }

    private ZonedDateTime readDateTag(final com.drew.metadata.Directory directory,
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

    private Optional<OrganizerFile> prepareFileBody(final File file, final Directory dirDest) {
        log.info("Preparing file {}", file);
        final Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (final ImageProcessingException | IOException logged) {
            log.error("Error reading metadata for file {}", file, logged);
            return Optional.empty();
        }

        ZonedDateTime dateTaken = null;

        final ExifSubIFDDirectory subDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subDirectory != null) {
            dateTaken = readDateTag(subDirectory, ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
        }

        if (dateTaken == null) {
            final ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null) {
                dateTaken = readDateTag(directory, ExifDirectoryBase.TAG_DATETIME);
            }
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
            } catch (final IOException logged) {
                log.warn("Cannot read creationTime for {}: {}", file, logged.getMessage());
            }
        }

        if (dateTaken == null) {
            log.warn("Using last modified date for {}", file);
            dateTaken = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC"));
            ignoreMonth = true;
        }

        final String year = Integer.toString(dateTaken.get(ChronoField.YEAR));
        // Folder names stay in Portuguese so new runs match the existing organized tree.
        final String month = ignoreMonth ? "outros" : String.format(
                "%s-%s",
                monthFormatter.format(dateTaken.getMonth().getValue()),
                dateTaken.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")));

        final File destDir = Paths.get(dirDest.getPath(), year, month).toFile();
        if (!destDir.exists()) {
            final boolean mkdirs = destDir.mkdirs();
            if (!mkdirs) {
                log.error("Could not create destination dir {} for file {}", destDir, file);
                return Optional.empty();
            }
        }

        return Optional.of(new OrganizerFile(file, destDir));
    }

    @Override
    public String getOperationName() {
        return "Organize Photos by Date";
    }

    @Override
    public String getOperationID() {
        return PHOTO_ORGANIZATION_OPERATION;
    }

    private record OrganizerFile(File file, File destDir) {}

}
