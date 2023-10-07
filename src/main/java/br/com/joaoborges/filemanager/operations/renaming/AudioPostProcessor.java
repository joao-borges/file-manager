package br.com.joaoborges.filemanager.operations.renaming;

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

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.FileDTO;
import br.com.joaoborges.filemanager.operations.Utilitario;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador.RenamingResult;
import br.com.joaoborges.filemanager.type.FileType;
import br.com.joaoborges.filemanager.type.ReplacingConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Renomeador de arquivos de audio.
 * <p>
 * O processor de interface que deve decidir se esta classe deve ser instanciada, ou o processor generico.
 * 
 * @author João
 */
@Service(value = AudioPostProcessor.Audio_Post_Processor)
@Slf4j
public class AudioPostProcessor implements PostProcessor {

	/** Audio_Post_Processor */
	public static final String Audio_Post_Processor = BEAN_NAME_FORMAT + FileType.AUDIO;
	private static final long serialVersionUID = -3089259692194594010L;

	@Autowired
	private RenamingOperations renamingOperations;
	@Autowired
	private ExclusionManagerService exclusions;

	public String processFileName(String fileName) {
		log.debug("Operacoes customizadas para " + fileName);
		String[] name = this.splitName(fileName);

		if (name.length == 2) {
			String namePart2 = name[1];
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
	 * Encontra o local certo para partir o nome em 2 partes.
	 * 
	 * @param newName
	 * @return
	 */
	private String[] splitName(String newName) {
		String[] name = new String[2];

		int splitIndex = 0;
		boolean splitSuccessful = false;
		for (int i = 0; i < newName.length(); i++) {
			splitIndex = i;

			char iChar = newName.charAt(i);
			if (iChar == ReplacingConstants.TRACE.charAt(0)) {
				String part = Utilitario.getBlockBehindChar(newName, i);
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
		name[1] = newName.substring(splitIndex + 1, newName.length());

		return name;
	}

	@Override
	public void processFile(FileDTO fileToRename, RenamingResult result, Collection<String> originalFileLis)
			throws FileManagerException {
		log.debug("Operacoes customizadas de arquivo para " + fileToRename.getFile().getAbsolutePath());

		try {
			AudioFile f = AudioFileIO.read(fileToRename.getFile());
			Tag tag = f.getTag();
			if (tag == null) {
				tag = f.createDefaultTag();
			}
			// se tinha o nome do mano e da musica nas propredade, usa e renomeia o arquivo.
			if (!Strings.isNullOrEmpty(tag.getFirst(FieldKey.ARTIST))
					&& !Strings.isNullOrEmpty(tag.getFirst(FieldKey.TITLE))) {
				String artist = tag.getFirst(FieldKey.ARTIST);
				String title = tag.getFirst(FieldKey.TITLE);

				artist = Renomeador.replaces(artist);
				title = Renomeador.replaces(title);

				String newName = artist + ReplacingConstants.SPACE + ReplacingConstants.TRACE
						+ ReplacingConstants.SPACE + title + ReplacingConstants.POINT
						+ fileToRename.getExtension().getExtensao();

				// titlecase pra ficar mais bonitinho
				newName = WordUtils.capitalizeFully(newName);

				File newFile = new File(fileToRename.getFile().getPath() + File.separator + newName);
				if (originalFileLis.contains(newName)) {
					newName = "(" + System.currentTimeMillis() + ") " + newName;
					newFile = new File(fileToRename.getFile().getPath() + File.separator + newName);
				}
				fileToRename.getFile().renameTo(newFile);

				result.getRenamedFiles().remove(fileToRename.getFile().getPath());
				result.getRenamedFiles().put(newFile.getPath(), fileToRename.getFile().getPath());

				log.debug("Nome final: " + newName);
			} else {
				String[] fileInfo = this.splitName(Utilitario.splitExtension(fileToRename.getFile().getName())[0]);
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
			f.setTag(tag);
			f.commit();

		} catch (Exception e) {
			log.error("Erro nas operacoes customizadas!");
			log.error(e.getMessage(), e);
			throw new FileManagerException("Erro para alterar as propriedades do arquivo de audio.", e);
		}

	}
}
