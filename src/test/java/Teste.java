import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileSystemDirectory;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * N/A
 *
 * @author JoãoGabriel
 */
public class Teste {

    public static void main(String[] args) throws Exception {
        final File logFile = new File("C:\\Users\\joao_\\Fotos.log");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile.toPath())))) {
			String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\u0000", "");
                line = line.replace((char) 65533, ' ').trim();
                if (line.startsWith("A            PU      ")) {
                    boolean updated = updateFile(line.substring(line.indexOf("C:")));
                    if (updated) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean updateFile(String filePath) throws ImageProcessingException, IOException {
        if (!"jpg".equals(FilenameUtils.getExtension(filePath))){
            return false;
        }
        System.out.println(filePath);
        final File file = new File(filePath);
        final File file2 = new File(file.getParentFile().toString() + File.separator + file.getName().toLowerCase());

        return file.renameTo(file2);
    }
}
