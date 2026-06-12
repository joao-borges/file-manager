import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;

public class PhotoLogExifFixer {

    public static void main(final String[] args) throws Exception {
        final File logFile = new File("C:\\Users\\joao_\\Fotos.log");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile.toPath())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\u0000", "");
                line = line.replace((char) 65533, ' ').trim();
                if (line.startsWith("A            PU      ")) {
                    final boolean updated = updateFile(line.substring(line.indexOf("C:")));
                    if (updated) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean updateFile(final String filePath) {
        if (!"jpg".equals(FilenameUtils.getExtension(filePath))) {
            return false;
        }
        System.out.println(filePath);
        final File file = new File(filePath);
        final File renamedFile = new File(file.getParentFile().toString() + File.separator + file.getName().toLowerCase());

        return file.renameTo(renamedFile);
    }

}
