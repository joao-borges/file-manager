package ca.joaoborges.filemanager.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ca.joaoborges.filemanager.model.util.Message;

/**
 * File extensions supported by the system.
 */
public enum Extension {

    MP3(0, FileType.AUDIO),
    WMA(1, FileType.AUDIO),
    WMV(3, FileType.VIDEO),
    MPEG(3, FileType.VIDEO),
    MPG(3, FileType.VIDEO),
    WAV(4, FileType.AUDIO),
    JPG(5, FileType.IMAGE),
    JPEG(5, FileType.IMAGE),
    BMP(6, FileType.IMAGE),
    PNG(6, FileType.IMAGE),
    MOV(3, FileType.VIDEO),
    MP4(3, FileType.VIDEO),
    AVI(3, FileType.VIDEO),
    TXT(7, FileType.TEXT);

    private final String description;
    private final int group;
    private final String type;
    private final String extension;

    Extension(final int group, final String type) {
        this.group = group;
        this.extension = name().toLowerCase();
        this.description = Message.getMessage(Message.EXTENSION_GROUPS, Integer.toString(this.group));
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public int getGroup() {
        return this.group;
    }

    public String getExtension() {
        return this.extension;
    }

    public static List<String> asStrings() {
        return Arrays.stream(Extension.values()).map(Extension::getExtension).sorted().collect(Collectors.toList());
    }

}
