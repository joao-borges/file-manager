package br.com.joaoborges.filemanager.operations.renaming;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.model.Diretorio;
import br.com.joaoborges.filemanager.model.FileDTO;
import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.model.util.Message;
import br.com.joaoborges.filemanager.operations.Utilitario;
import br.com.joaoborges.filemanager.operations.common.OperationConstants;
import br.com.joaoborges.filemanager.operations.common.SpringUtils;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;
import br.com.joaoborges.filemanager.operations.renaming.Renomeador.RenamingResult;
import br.com.joaoborges.filemanager.type.Extensao;
import br.com.joaoborges.filemanager.type.ReplacingConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Servico responsavel por aplicar as regras e devolver os nomes novos para os arquivos.
 *
 * @author Joao
 */
@Service(value = OperationConstants.RENAME_OPERATION)
@Slf4j
public class Renomeador implements FileOperation<RenamingResult> {

	static final String INCLUDE_SUB_DIRECTORIES = "INCLUDE_SUB_DIRECTORIES";

	private static final long serialVersionUID = -8287266807900801749L;

	@Autowired
	private ExclusionManagerService exclusions;

	/**
	 * Renomeia os arquivos contidos na pasta.
	 *
	 * @throws FileManagerException
	 *             em caso de erros.
	 */
	public RenamingResult execute(Map<String, Object> params) throws FileManagerException {
		Diretorio contentDirectory = (Diretorio) params.get(Diretorio.class.getName());
		FiltroExtensoes filtroSolicitado = (FiltroExtensoes) params.get(FiltroExtensoes.class.getName());
		Boolean includeSubDirectories = (Boolean) params.get(INCLUDE_SUB_DIRECTORIES);
		RenamingResult result = new RenamingResult(contentDirectory);

		// se nao for especificado nao inclui
		if (includeSubDirectories == null) {
			includeSubDirectories = false;
		}
		if (filtroSolicitado == null) {
			filtroSolicitado = FiltroExtensoes.allAcceptedFilter();
			filtroSolicitado.setAcceptDirectories(includeSubDirectories);
		}

		this.renomearConteudoDiretorio(contentDirectory, filtroSolicitado, result);

		return result;
	}

	private void renomearConteudoDiretorio(Diretorio contentDirectory, FiltroExtensoes filtroSolicitado,
			RenamingResult result) {
		Collection<String> originalFileList = contentDirectory.listFileNames(filtroSolicitado);
		Collection<File> conteudo = contentDirectory.listarConteudo(filtroSolicitado);
		log.debug("Total " + conteudo.size());
		// itera sobre a colecao de arquivos para renomea-los
		for (File fileToRename : conteudo) {
			if (fileToRename.isDirectory()) {
				Diretorio subDir = new Diretorio(fileToRename.getPath());
				this.renomearConteudoDiretorio(subDir, filtroSolicitado, result);
			} else if (!fileToRename.isHidden() && fileToRename.canRead() && fileToRename.canWrite()) {
				try {
					// transacao, para voltar atras caso cague
					String originalName = fileToRename.getName();
					log.debug("Renomeando: " + originalName);

					String newName = originalName;
					newName = newName.trim().toLowerCase();
					String[] nameSeparated = Utilitario.splitExtension(newName);
					String extensao = nameSeparated[1];
					Extensao ext = Extensao.valueOf(extensao.toUpperCase());
					// tenta achar um postprocessor valido
					PostProcessor postProcessor = SpringUtils.getContext().getBean(PostProcessor.BEAN_NAME_FORMAT + ext.getTipo(), PostProcessor.class);

					newName = nameSeparated[0];
					newName = newName.trim();
					newName = replaces(newName);
					newName = newName.trim();
					newName = processNumbers(newName);
					newName = newName.trim();
					newName = postProcessor.processFileName(newName);
					newName = newName.trim();
					// monta o nome final e salva
					newName += ReplacingConstants.POINT + extensao;

					// retira caracteres escapados
					newName = StringEscapeUtils.unescapeXml(newName);

					// titlecase pra ficar mais bonitinho
					newName = WordUtils.capitalizeFully(newName);

					File renamedFile = new File(contentDirectory.getPath() + File.separator + newName);
					if (!originalName.equals(newName)) {
						if (originalFileList.contains(newName)) {
							newName = "(" + System.currentTimeMillis() + ") " + newName;
							renamedFile = new File(contentDirectory.getPath() + File.separator + newName);
						}
						fileToRename.renameTo(renamedFile);

						result.renamedFiles.put(renamedFile.getPath(), fileToRename.getPath());
						log.debug("Nome final: " + newName);
						// chama a rotina de operacoes adicionais, se houver
						postProcessor.processFile(new FileDTO(renamedFile, ext, contentDirectory), result, originalFileList);
					}

				} catch (Exception e) {
					log.error("ERRO!!!!");
					log.error(e.getMessage(), e);
					log.error("Há um problema com o arquivo " + fileToRename.getName());
				}
			}
		}
	}

	/**
	 * Agrupa as operações de correçao no nome.
	 */
	static String replaces(String newName) {
		newName = simpleReplacements(newName);
		newName = regexReplacements(newName);
		return newName;
	}

	/**
	 * Faz as substituicoes de caracteres indevidos.
	 */
	private static String simpleReplacements(String newName) {
		newName = doReplacement(newName, ReplacingConstants.PLUS, ReplacingConstants.SPACE);

		Enumeration<String> filterStrings = Message.getLocalizer(Message.STRINGSTOFILTER, null).getKeys();

		while (filterStrings.hasMoreElements()) {
			String key = filterStrings.nextElement();
			String filter = Message.getMessage(Message.STRINGSTOFILTER, key);

			if (newName.lastIndexOf(filter) != -1) {
				newName = newName.replaceAll(filter, ReplacingConstants.SPACE);
			}
			newName = newName.trim();
		}
		return newName;
	}

	/**
	 * Faz as substituicoes de caracteres indevidos.
	 *
	 * @param newName
	 * @return String
	 */
	private static String regexReplacements(String newName) {
//		Enumeration<String> filterStrings = Message.getLocalizer(Message.REGEXESTOFILTER, null).getKeys();
//
//		while (filterStrings.hasMoreElements()) {
//			String regexKey = filterStrings.nextElement();
//			Pattern pattern = Pattern.compile(Message.getMessage(Message.REGEXESTOFILTER, regexKey));
//			Matcher matcher = pattern.matcher(newName);
//
//			if (matcher.find()) {
//				newName = matcher.replaceAll(ReplacingConstants.SPACE);
//			}
//			newName = newName.trim();
//		}
		return newName;
	}

	private static String doReplacement(String stringToReplace, String charToBeReplaced, String substituteChar) {
		for (int i = 0; i < stringToReplace.length(); i++) {
			char c = stringToReplace.charAt(i);
			if (charToBeReplaced.equals(new String(new char[] { c }))) {
				stringToReplace = stringToReplace.substring(0, i) + substituteChar + stringToReplace.substring(i + 1);
			}
		}

		return stringToReplace;
	}

	/**
	 * Processa os numeros no inicio.
	 */
	private String processNumbers(String newName) {

		boolean ok = true;
		// verifico se posso mexer no comeco do nome
		for (String exclusion : this.exclusions.getExclusions()) {
			if (newName.startsWith(exclusion.toLowerCase())) {
				// nao posso mexer no comeco do nome porque e uma das exclusoes
				ok = false;
			}
		}

		// ok posso mexer
		if (ok) {
			// itero sobre a string do nome
			// pra ver se comeca com os caracteres letra minuscula (ascii: 97 a
			// 122)
			boolean repetir = true;
			while (repetir) {
				boolean continuar = true;
				String temp = newName;
				int i = 0;
				while (continuar && i < newName.length()) {
					int ch = newName.charAt(i);
					if (ch == 32 || (ch >= 97 && ch <= 122)) {
						continuar = false;
					} else {
						temp = newName.substring(i + 1);
					}
					i++;
				}
				newName = temp.trim();
				int ch = newName.charAt(0);
				if (ch >= 97 && ch <= 122) {
					repetir = false;
				}
			}
		}
		return newName;
	}

	/**
	 * Resultado
	 *
	 * @author JoãoGabriel
	 */
	public class RenamingResult implements OperationResult {

		/** serialVersionUID */
		private static final long serialVersionUID = 610178010997711328L;
		/**
		 * Set de arquivos renomeados.
		 */
		private Map<String, String> renamedFiles;
		private Map<String, String> duplicatedFiles;
		private Diretorio currentDirectory;

		/**
		 * Construtor
		 */
		RenamingResult(Diretorio currentDirectory) {
			super();
			this.currentDirectory = currentDirectory;

			this.renamedFiles = new HashMap<>();
			this.duplicatedFiles = new HashMap<>();
		}

		/**
		 * @return Map<String,String>
		 */
		Map<String, String> getRenamedFiles() {
			return this.renamedFiles;
		}

		/**
		 * @return Map<String,String>
		 */
		public Map<String, String> getDuplicatedFiles() {
			return this.duplicatedFiles;
		}

		/**
		 * @return Diretorio
		 */
		Diretorio getCurrentDirectory() {
			return this.currentDirectory;
		}
	}

	public String getOperationName() {
		return "Renomear Arquivos";
	}

	public String getOperationID() {
		return OperationConstants.RENAME_OPERATION;
	}
}
