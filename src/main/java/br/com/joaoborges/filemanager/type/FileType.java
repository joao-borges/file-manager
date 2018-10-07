package br.com.joaoborges.filemanager.type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.exception.FileManagerRuntimeException;

/**
 * Tipos genericos de arquivos que classificam um servico.
 * 
 * @author Joao
 */
public class FileType {

	private static final Logger LOGGER = Logger.getLogger(FileType.class);

	public static final String ALL_FILES = "ALL_FILES";
	public static final String AUDIO = "AUDIO";
	public static final String VIDEO = "VIDEO";
	public static final String TEXT = "TEXT";
	public static final String IMAGE = "IMAGE";

	/**
	 * Retorna o tipo em int.
	 * 
	 * @param type
	 * @return
	 * @throws FileManagerException
	 */
	public static int asInteger(String type) {
		try {
			return Integer.parseInt(type);
		} catch (NumberFormatException e) {
			LOGGER.debug(type + " nao e um tipo valido");
			LOGGER.debug(e.getMessage(), e);
			throw new FileManagerRuntimeException("Tipo inválido: " + type, e);
		}
	}

	/**
	 * Retorna descricao do tipo.
	 * 
	 * @param type
	 * @return
	 * @throws FileManagerException
	 */
	public static String getDescription(String type) {
		try {
			Field[] types = FileType.class.getFields();
			String fieldName = null;
			for (Field f : types) {
				if (Modifier.isFinal(f.getModifiers()) && f.getType().equals(String.class) && f.get(null).equals(type)) {
					fieldName = f.get(null).toString();
				}
			}
			return fieldName;
		} catch (Exception e) {
			LOGGER.error("Erro para interpretar o tipo");
			LOGGER.error(e.getMessage(), e);
			throw new FileManagerRuntimeException(e.getMessage(), e);
		}
	}
}
