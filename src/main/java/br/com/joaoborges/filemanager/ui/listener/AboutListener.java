package br.com.joaoborges.filemanager.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import br.com.joaoborges.filemanager.model.util.Message;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;

/**
 * Listener que e executado quando o usuario deseja ver os creditos.
 * 
 * @author Joao
 */
public class AboutListener implements ActionListener {

	private final TelaPrincipal tela;

	public AboutListener(TelaPrincipal tela) {
		this.tela = tela;
	}

	public void actionPerformed(ActionEvent e) {
		URL imagem = Thread.currentThread().getContextClassLoader().getResource("imagens/bunda.JPG");
		ImageIcon icon = new ImageIcon(imagem);
		JOptionPane.showMessageDialog(tela.getFrame(), "Gerenciador de Arquivos " + this.getVersion()
				+ "\n\nPor João Gabriel Borges Caldeira - 2010", "Créditos", JOptionPane.INFORMATION_MESSAGE, icon);
		return;
	}

	private String getVersion() {
		return Message.getMessage(Message.APP_INFO, "version");
	}
}
