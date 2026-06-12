package ca.joaoborges.filemanager.model.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Central locator for messages in resource catalogs.
 */
public class Message {

    public static final Locale LOCALE_DEFAULT = Locale.ENGLISH;

    /**
     * Prefix for all catalogs.
     */
    public static final String PREFIX = "ca.joaoborges.filemanager.resources.";

    public static final String EXTENSION_GROUPS = PREFIX + "ExtensionGroups";
    public static final String STRINGS_TO_FILTER = PREFIX + "StringsToFilter";
    public static final String REGEXES_TO_FILTER = PREFIX + "RegexesToFilter";
    public static final String APP_INFO = "application";

    private static final Map<String, ResourceBundle> cache = new HashMap<>();

    /**
     * Returns the {@link ResourceBundle} for the given catalog.
     */
    public static ResourceBundle getLocalizer(final String catalog, final Locale locale) throws MissingResourceException {
        ResourceBundle bundle = cache.get(catalog);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(catalog);
            cache.put(catalog, bundle);
        }
        return bundle;
    }

    /**
     * Returns a message by its id, using the default locale.
     */
    public static String getMessage(final String catalog, final String msgID) {
        return getMessage(catalog, msgID, (Locale) null);
    }

    /**
     * Returns a message by its id, in the given locale; falls back to the default locale.
     */
    public static String getMessage(final String catalog, final String msgID, Locale locale) {
        if (locale == null) {
            locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT.getCountry());
        }

        String message = "";
        try {
            message = getLocalizer(catalog, locale).getString(msgID);
        } catch (final MissingResourceException ignored) {
            if (locale.equals(LOCALE_DEFAULT)) {
                return "Message not found. Message ID: " + msgID + " in catalog: " + catalog + ", for locale: "
                        + locale.toString();
            }
        }

        if (message.length() == 0) {
            try {
                message = getLocalizer(catalog, LOCALE_DEFAULT).getString(msgID);
            } catch (final MissingResourceException ignored) {
                if (locale.equals(LOCALE_DEFAULT)) {
                    return "Message not found. Message ID: " + msgID + " in catalog: " + catalog + ", for locale: "
                            + locale.toString() + " nor in the default locale.";
                }
            }
        }
        return message;
    }

    /**
     * Returns a message by its id, formatted with the given arguments, in the default locale.
     */
    public static String getMessage(final String catalog, final String msgID, final Object[] args) {
        return getMessage(catalog, msgID, args, null);
    }

    /**
     * Returns a message by its id, formatted with the given arguments.
     * <p>
     * If the locale is null, or the message does not exist in it, the default locale is used.
     */
    public static String getMessage(final String catalog, final String msgID, final Object[] args, final Locale locale) {
        return MessageFormat.format(getMessage(catalog, msgID, locale), args);
    }

    /**
     * Returns a message by its id, formatted with the given argument, in the default locale.
     */
    public static String getMessage(final String catalog, final String msgID, final Object arg) {
        return getMessage(catalog, msgID, arg, null);
    }

    /**
     * Returns a message by its id, formatted with the given argument.
     * <p>
     * If the locale is null, or the message does not exist in it, the default locale is used.
     */
    public static String getMessage(final String catalog, final String msgID, final Object arg, final Locale locale) {
        final Object[] args = { arg };
        return getMessage(catalog, msgID, args);
    }

    /**
     * Returns a message by its id, in the default locale.
     * <p>
     * Returns null when the message is not found, instead of a fallback message.
     */
    public static String getMessageOrNull(final String catalog, final String msgID) {
        return getMessageOrNull(catalog, msgID, null);
    }

    /**
     * Returns a message by its id, in the given locale.
     * <p>
     * Returns null when the message is not found, instead of a fallback message.
     */
    public static String getMessageOrNull(final String catalog, final String msgID, Locale locale) {
        if (locale == null) {
            locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT.getCountry());
        }

        String message = "";
        try {
            message = getLocalizer(catalog, locale).getString(msgID);
        } catch (final MissingResourceException ignored) {
            message = null;
        }
        return message;
    }

    /**
     * Returns a configuration value without specifying the locale.
     */
    public static String getConfigurationOrNull(final String catalog, final String msgID) {
        return getConfigurationOrNull(catalog, msgID, null);
    }

    /**
     * Returns a configuration value by its id, in the given locale.
     * <p>
     * Returns null when the value is not found, instead of a fallback message.
     */
    public static String getConfigurationOrNull(final String catalog, final String msgID, Locale locale) {
        if (locale == null) {
            locale = new Locale(LOCALE_DEFAULT.getLanguage(), LOCALE_DEFAULT.getCountry());
        }
        String message = "";
        try {
            final ResourceBundle bundle = getLocalizer(catalog, locale);
            if (bundle instanceof PropertyResourceBundle) {
                message = (String) ((PropertyResourceBundle) bundle).handleGetObject(msgID);
            } else if (bundle instanceof ListResourceBundle) {
                message = (String) ((ListResourceBundle) bundle).handleGetObject(msgID);
            } else {
                message = getLocalizer(catalog, locale).getString(msgID);
            }
        } catch (final MissingResourceException ignored) {
            message = null;
        }
        return message;
    }

}
