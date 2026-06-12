package ca.joaoborges.filemanager.type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ca.joaoborges.filemanager.exception.FileManagerRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic file types that classify a service.
 */
@Slf4j
public class FileType {

    public static final String ALL_FILES = "ALL_FILES";
    public static final String AUDIO = "AUDIO";
    public static final String VIDEO = "VIDEO";
    public static final String TEXT = "TEXT";
    public static final String IMAGE = "IMAGE";

    /**
     * Returns the type as an int.
     */
    public static int asInteger(final String type) {
        try {
            return Integer.parseInt(type);
        } catch (final NumberFormatException wrapped) {
            log.debug(type + " is not a valid type");
            log.debug(wrapped.getMessage(), wrapped);
            throw new FileManagerRuntimeException("Invalid type: " + type, wrapped);
        }
    }

    /**
     * Returns the description of the type.
     */
    public static String getDescription(final String type) {
        try {
            final Field[] types = FileType.class.getFields();
            String fieldName = null;
            for (final Field field : types) {
                if (Modifier.isFinal(field.getModifiers()) && field.getType().equals(String.class)
                        && field.get(null).equals(type)) {
                    fieldName = field.get(null).toString();
                }
            }
            return fieldName;
        } catch (final Exception wrapped) {
            log.error("Failed to interpret the type");
            log.error(wrapped.getMessage(), wrapped);
            throw new FileManagerRuntimeException(wrapped.getMessage(), wrapped);
        }
    }

}
