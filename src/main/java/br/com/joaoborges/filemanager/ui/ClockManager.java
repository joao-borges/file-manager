package br.com.joaoborges.filemanager.ui;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.JLabel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClockManager implements Runnable {

	private JLabel labelToUpdate;
	private SimpleDateFormat sdf;

	private ClockManager(JLabel labelToUpdate) {
		this.labelToUpdate = labelToUpdate;
		this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	public void run() {
		log.debug("Iniciando ClockManager");
		while (true) {
			labelToUpdate
					.setText(sdf.format(new GregorianCalendar().getTime()));
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				log.warn("Thread que atualiza o relogio foi interrompida!");
				log.warn(e.getMessage(), e);
				break;
			}
		}
	}

	public static void start(JLabel labelToUpdate) {
		new Thread(new ClockManager(labelToUpdate)).start();
	}
}
