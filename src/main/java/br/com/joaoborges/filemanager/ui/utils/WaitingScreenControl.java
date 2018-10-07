package br.com.joaoborges.filemanager.ui.utils;

import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.utils.TelaEsperando;

/**
 * Controle da tela de aguardo.
 * 
 * @author Joao
 * @version 10/03/2012 16:09:16
 */
public class WaitingScreenControl {

	private TelaEsperando te;
	private Thread telaEsperando;

	public WaitingScreenControl(TelaPrincipal tela) {
		this.te = new TelaEsperando(tela.getFrame());
		this.telaEsperando = new Thread(this.te);
		this.telaEsperando.start();
	}

	public void interruptWaiting() {
		this.te.close();
		this.telaEsperando.interrupt();
	}
}
