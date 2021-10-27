package br.com.joaoborges.filemanager.operations.common;

import java.util.Objects;

import org.springframework.context.ApplicationContext;

/**
 * Utilitario padrao para funcoes comuns e recorrentes do uso do Spring.
 * 
 * @author Joao
 */
public class SpringUtils {

	private static ApplicationContext applicationContext;

	public static void setApplicationContext(final ApplicationContext applicationContext) {
		SpringUtils.applicationContext = applicationContext;
	}

	public static ApplicationContext getContext() {
		return applicationContext;
	}

	public static <T> T getBean(Class<T> clazz) {
		return Objects.requireNonNull(applicationContext).getBean(clazz);
	}


}
