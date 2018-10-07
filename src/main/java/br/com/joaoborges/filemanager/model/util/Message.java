package br.com.joaoborges.filemanager.model.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Localizador central de mensagens em catalogos.
 * 
 * @author Joao
 */
public class Message {

	public final static Locale LOCALE_DEFAULT = new Locale("pt", "BR");

	/**
	 * Prefixo para todos os catalogos
	 */
	public final static String PREFIX = "br.com.joaoborges.filemanager.resources.";
	
	public final static String EXTENSOES = PREFIX + "Extensoes";
	public final static String GRUPOSEXTENSOES = PREFIX + "GruposExtensoes";
	public final static String STRINGSTOFILTER = PREFIX + "StringsToFilter";
	public final static String REGEXESTOFILTER = PREFIX + "RegexesToFilter";
	public final static String APP_INFO = "application";

	/**
	 * Cache de Resource Bundles
	 */
	private static Map<String, ResourceBundle> cache = new HashMap<String, ResourceBundle>();

	/**
	 * Constructor for the Message object
	 */
	public Message() {
		super();
	}

	/**
	 * Retorna um localizador {@link ResourceBundle} associado ao catalogo
	 * informado, no locale enviado como parâmetro.
	 * 
	 * @param catalogo
	 *            Catalogo
	 * @param locale
	 *            Locale
	 * @return
	 * @throws MissingResourceException
	 */
	public static ResourceBundle getLocalizer(String catalogo, Locale locale)
			throws MissingResourceException {
		String chave = catalogo;

		ResourceBundle bundle = cache.get(chave);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(chave);
			cache.put(chave, bundle);
		}
		return bundle;
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente.
	 * Nao considera o locale, pega no idioma default.
	 * 
	 * @param catalogo
	 * @param msgID
	 * @return
	 */
	public static String getMessage(String catalogo, String msgID) {
		return getMessage(catalogo, msgID, (Locale) null);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente, e
	 * num locale especificado.
	 * 
	 * @param catalogo
	 * @param msgID
	 * @param locale
	 * @return
	 */
	public static String getMessage(String catalogo, String msgID, Locale locale) {
		if (locale == null) {
			locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT
					.getCountry());
		}

		String message = "";
		try {
			message = getLocalizer(catalogo, locale).getString(msgID);
		} catch (MissingResourceException msExc) {
			if (locale.equals(LOCALE_DEFAULT)) {
				return "Mensagem Nao encontrada. MensagemID: " + msgID
						+ " no catalogo: " + catalogo + ", no idioma: "
						+ locale.toString();
			}
		}

		if (message.length() == 0) {
			try {
				message = getLocalizer(catalogo, LOCALE_DEFAULT).getString(
						msgID);
			} catch (MissingResourceException msExc) {
				if (locale.equals(LOCALE_DEFAULT)) {
					return "Mensagem Nao encontrada. MensagemID: " + msgID
							+ " no catalogo: " + catalogo + ", no idioma: "
							+ locale.toString() + " e nem no idioma padrao.";
				}
			}
		}
		return message;
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente
	 * Metodo recebe um array de objects para serem inseridas na string de
	 * retorno, nos locais determinados
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem
	 * @param msgID
	 *            ID da mensagem no catalogo
	 * @param args
	 *            Argumentos para a mensagem
	 * @return Mensagem formatada com os parâmetros, no idioma solicitado.
	 */
	public static String getMessage(String catalogo, String msgID, Object[] args) {
		return getMessage(catalogo, msgID, args, null);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um cedigo, e na lingua corrente
	 * Metodo recebe um array de objects para serem inseridas na string de
	 * retorno, nos locais determinados
	 * <p>
	 * Considera um locale para obtencao da mensagem em algum idioma especifico.
	 * Caso o locale seja nulo, ou a mensagem Nao exista, pega no idioma
	 * default.
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem
	 * @param msgID
	 *            ID da mensagem no catalogo
	 * @param args
	 *            Argumentos para a mensagem
	 * @param locale
	 *            Locale para idioma
	 * @return Mensagem formatada com os parâmetros, no idioma solicitado.
	 */
	public static String getMessage(String catalogo, String msgID,
			Object[] args, Locale locale) {
		return MessageFormat.format(getMessage(catalogo, msgID, locale), args);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente
	 * Metodo recebe um object para ser inserido na string de retorno, no local
	 * determinado
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem
	 * @param msgID
	 *            ID da mensagem no catalogo
	 * @param arg
	 *            Argumento para a mensagem
	 * @return Mensagem formatada com os parâmetros, no idioma solicitado.
	 */
	public static String getMessage(String catalogo, String msgID, Object arg) {
		return getMessage(catalogo, msgID, arg, null);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente
	 * Metodo recebe um object para ser inserido na string de retorno, no local
	 * determinado
	 * <p>
	 * Considera um locale para obtencao da mensagem em algum idioma especcfico.
	 * Caso o locale seja nulo, ou a mensagem Nao exista, pega no idioma
	 * default.
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem
	 * 
	 * @param msgID
	 *            ID da mensagem no catalogo
	 * @param arg
	 *            Argumento para a mensagem
	 * @param locale
	 *            Locale para idioma
	 * @return Mensagem formatada com os parâmetros, no idioma solicitado.
	 */
	public static String getMessage(String catalogo, String msgID, Object arg,
			Locale locale) {
		Object[] args = { arg };
		return getMessage(catalogo, msgID, args);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente.
	 * <p>
	 * Se Nao encontrar, retorna nulo, em vez de uma mensagem padrao.
	 * <p>
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem.
	 * @param msgID
	 *            ID da mensagem.
	 * @return Mensagem.
	 */
	public static String getMessageOrNull(String catalogo, String msgID) {
		return getMessageOrNull(catalogo, msgID, null);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua
	 * especificada.
	 * <p>
	 * Se Nao encontrar, retorna nulo, em vez de uma mensagem padrao.
	 * <p>
	 * 
	 * @param catalogo
	 *            Catalogo onde se encontra a mensagem.
	 * @param msgID
	 *            ID da mensagem.
	 * @param locale
	 *            Locale do idioma selecionado.
	 * @return Mensagem.
	 */
	public static String getMessageOrNull(String catalogo, String msgID,
			Locale locale) {
		if (locale == null) {
			locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT
					.getCountry());
		}

		String message = "";
		try {
			message = getLocalizer(catalogo, locale).getString(msgID);

		} catch (MissingResourceException msExc) {
			message = null;
		}
		return message;
	}

	/**
	 * Retorna uma configuracao sem especificar o locale.
	 * 
	 * @param catalogo
	 *            Catalogo.
	 * @param msgID
	 *            iD da mensagem.
	 * @return Mensagem de configuracao.
	 */
	public static String getConfigurationOrNull(String catalogo, String msgID) {
		return getConfigurationOrNull(catalogo, msgID, null);
	}

	/**
	 * Metodo retorna uma mensagem baseado em um codigo, e na lingua corrente.
	 * <p>
	 * Se Nao encontrar, retorna nulo, em vez de uma mensagem padrao.
	 * <p>
	 * 
	 * @param catalogo
	 *            Catalogo.
	 * @param msgID
	 *            iD da mensagem.
	 * @param locale
	 *            Locale para a mensagem.
	 * @return
	 */
	public static String getConfigurationOrNull(String catalogo, String msgID,
			Locale locale) {
		if (locale == null) {
			locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT
					.getCountry());
		}
		String message = "";
		try {
			ResourceBundle rs = getLocalizer(catalogo, locale);
			if (rs instanceof PropertyResourceBundle) {
				message = (String) ((PropertyResourceBundle) rs)
						.handleGetObject(msgID);
			} else if (rs instanceof ListResourceBundle) {
				message = (String) ((ListResourceBundle) rs)
						.handleGetObject(msgID);
			} else {
				message = getLocalizer(catalogo, locale).getString(msgID);
			}
		} catch (MissingResourceException msExc) {
			message = null;
		}
		return message;
	}
}
