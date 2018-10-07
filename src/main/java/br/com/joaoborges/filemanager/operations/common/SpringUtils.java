package br.com.joaoborges.filemanager.operations.common;

import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utilitario padrao para funcoes comuns e recorrentes do uso do Spring.
 * 
 * @author Joao
 */
public class SpringUtils {

	private static ApplicationContext ctx;

	/**
	 * Inicia o contexto spring da aplicacao.
	 */
	public static void initContext() {
		ctx = new ClassPathXmlApplicationContext("META-INF/spring-config.xml");
	}

	/**
	 * Devolve o contexto de aplicacao, que corresponde ao container do Spring
	 * 
	 * @return o contexto de aplicacao
	 */
	public static ApplicationContext getContext() {
		return ctx;
	}

	/**
	 * Recupera um bean de uma determinada classe ou interface a partir de um
	 * contexto. Caso nao seja possivel definir qual implementacao utilizar
	 * retorna null.
	 */
	public static <T> T getBean(Class<T> clazz) {
		T x = getBean(getContext(), clazz, null);
		return x;
	}

	/**
	 * Recupera um bean de uma determinada classe ou interface a partir de um
	 * contexto e do nome da implementacao desejada.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(ApplicationContext ctx, Class<T> clazz,
			String name) {
		Map map = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, clazz);

		// Busca por name e tipo
		if (name != null) {
			return (T) map.get(name);
		}

		// Busca apenas por tipo
		if (map.size() == 1) {
			// apenas uma implementacao
			return (T) map.values().iterator().next();
		} else {
			// mais de uma implementacao encontrada
			for (Object bean : map.values()) {
				if (clazz.equals(bean.getClass())) {
					return (T) bean;
				}
			}
		}

		return null;
	}

}
