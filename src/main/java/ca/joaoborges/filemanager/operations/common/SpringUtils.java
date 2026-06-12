package ca.joaoborges.filemanager.operations.common;

import java.util.Objects;

import org.springframework.context.ApplicationContext;

/**
 * Utility for common, recurring Spring context lookups.
 */
public class SpringUtils {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(final ApplicationContext applicationContext) {
        SpringUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }

    public static <T> T getBean(final Class<T> clazz) {
        return Objects.requireNonNull(applicationContext).getBean(clazz);
    }

}
