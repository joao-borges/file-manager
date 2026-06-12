package ca.joaoborges.filemanager.operations.renaming;

import java.io.File;
import java.util.Collection;

import org.apache.commons.text.WordUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import ca.joaoborges.filemanager.exception.FileManagerException;
import ca.joaoborges.filemanager.model.FileDTO;
import ca.joaoborges.filemanager.operations.NameUtils;
import ca.joaoborges.filemanager.operations.renaming.Renamer.RenamingResult;
import ca.joaoborges.filemanager.type.FileType;
import ca.joaoborges.filemanager.type.ReplacingConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Post processor for audio files.
 * <p>
 * The caller decides whether this class or the generic processor should be used.
 */
@Service(value = AudioPostProcessor.AUDIO_POST_PROCESSOR)
@Slf4j
public class AudioPostProcessor implements PostProcessor {

    public static final String AUDIO_POST_PROCESSOR = BEAN_NAME_FORMAT + FileType.AUDIO;

    private static final long serialVersionUID = -3089259692194594010L;

    @Autowired
    private RenamingOperations renamingOperations;

    @Autowired
    private ExclusionManagerService exclusions;

    public String processFileName(String fileName) {
        log.debug("Custom name operations for " + fileName);
        final String[] name = this.splitName(fileName);

        if (name.length == 2) {
            final String namePart2 = name[1];
            if (namePart2.contains(ReplacingConstants.OPEN_PAR) && !namePart2.contains(ReplacingConstants.CLOSE_PAR)) {
                namePart2.concat(ReplacingConstants.CLOSE_PAR);
            }

            name[0] = this.renamingOperations.doReplaceAll(name[0], ReplacingConstants.TRACE, "");
            name[1] = this.renamingOperations.doReplaceAll(name[1], ReplacingConstants.TRACE, "");

            fileName = name[0].trim() + ReplacingConstants.SPACE + ReplacingConstants.TRACE + ReplacingConstants.SPACE
                    + name[1].trim();
        }

        return fileName;
    }

    /**
     * Finds the right position to split the name in two parts.
     */
    private String[] splitName(final String newName) {
        final String[] name = new String[2];

        int splitIndex = 0;
        boolean splitSuccessful = false;
        for (int i = 0; i < newName.length(); i++) {
            splitIndex = i;

            final char iChar = newName.charAt(i);
            if (iChar == ReplacingConstants.TRACE.charAt(0)) {
                final String part = NameUtils.getBlockBehindChar(newName, i);
                if (!this.exclusions.hasExclusionFor(part, false) || part.equals(ReplacingConstants.TRACE)) {
                    splitSuccessful = true;
                    break;
                }
            }
        }
        if (!splitSuccessful) {
            return new String[] { newName };
        }
        name[0] = newName.substring(0, splitIndex);
        name[1] = newName.substring(splitIndex + 1);

        return name;
    }

    @Override
    public void processFile(final FileDTO fileToRename, final RenamingResult result,
            final Collection<String> originalFileList) throws FileManagerException {
        log.debug("Custom file operations for " + fileToRename.getFile().getAbsolutePath());

        try {
            final AudioFile audioFile = AudioFileIO.read(fileToRename.getFile());
            Tag tag = audioFile.getTag();
            if (tag == null) {
                tag = audioFile.createDefaultTag();
            }
            // if the tags already contain artist and title, use them to rename the file
            if (!Strings.isNullOrEmpty(tag.getFirst(FieldKey.ARTIST))
                    && !Strings.isNullOrEmpty(tag.getFirst(FieldKey.TITLE))) {
                String artist = tag.getFirst(FieldKey.ARTIST);
                String title = tag.getFirst(FieldKey.TITLE);

                artist = Renamer.replaces(artist);
                title = Renamer.replaces(title);

                String newName = artist + ReplacingConstants.SPACE + ReplacingConstants.TRACE
                        + ReplacingConstants.SPACE + title + ReplacingConstants.POINT
                        + fileToRename.getExtension().getExtension();

                // title case for a nicer-looking name
                newName = WordUtils.capitalizeFully(newName);

                File newFile = new File(fileToRename.getFile().getPath() + File.separator + newName);
                if (originalFileList.contains(newName)) {
                    newName = "(" + System.currentTimeMillis() + ") " + newName;
                    newFile = new File(fileToRename.getFile().getPath() + File.separator + newName);
                }
                fileToRename.getFile().renameTo(newFile);

                result.getRenamedFiles().remove(fileToRename.getFile().getPath());
                result.getRenamedFiles().put(newFile.getPath(), fileToRename.getFile().getPath());

                log.debug("Final name: " + newName);
            } else {
                final String[] fileInfo = this.splitName(NameUtils.splitExtension(fileToRename.getFile().getName())[0]);
                tag.setField(FieldKey.ARTIST, fileInfo[0].trim());
                tag.setField(FieldKey.ALBUM_ARTIST, fileInfo[0].trim());
                if (fileInfo.length > 1) {
                    tag.setField(FieldKey.TITLE, fileInfo[1].trim());
                }
                tag.setField(FieldKey.ALBUM, "");
                tag.setField(FieldKey.GENRE, "");
                tag.setField(FieldKey.YEAR, "");
                tag.setField(FieldKey.TRACK, "0");
            }
            audioFile.setTag(tag);
            audioFile.commit();

        } catch (final Exception rethrown) {
            log.error("Error in the custom audio file operations.", rethrown);
            throw new FileManagerException("Could not update the audio file properties.", rethrown);
        }

    }

}
