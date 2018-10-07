package br.com.joaoborges.filemanager.app;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import br.com.joaoborges.filemanager.operations.common.SpringUtils;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

public class FileManager {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

		URL log4jconfig = Thread.currentThread().getContextClassLoader().getResource("META-INF/log4j.xml");
		DOMConfigurator.configure(log4jconfig);
		try {
			SpringUtils.initContext();
			TelaPrincipal tela = SpringUtils.getBean(TelaPrincipal.class);
			tela.build();
			tela.open();
		} catch (Exception e) {
			Logger log = Logger.getLogger(FileManager.class);
			log.fatal("Nao foi possivel iniciar app.", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
