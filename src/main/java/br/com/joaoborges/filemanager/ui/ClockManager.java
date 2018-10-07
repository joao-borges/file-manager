package br.com.joaoborges.filemanager.ui;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

public class ClockManager implements Runnable {

	private JLabel labelToUpdate;
	private SimpleDateFormat sdf;
	private static final Logger LOGGER = Logger.getLogger(ClockManager.class);

	private ClockManager(JLabel labelToUpdate) {
		this.labelToUpdate = labelToUpdate;
		this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	public void run() {
		LOGGER.debug("Iniciando ClockManager");
		while (true) {
			labelToUpdate
					.setText(sdf.format(new GregorianCalendar().getTime()));
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				LOGGER.warn("Thread que atualiza o relogio foi interrompida!");
				LOGGER.warn(e.getMessage(), e);
				break;
			}
		}
	}

	public static void start(JLabel labelToUpdate) {
		new Thread(new ClockManager(labelToUpdate)).start();
	}
}
