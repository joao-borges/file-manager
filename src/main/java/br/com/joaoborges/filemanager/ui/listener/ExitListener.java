package br.com.joaoborges.filemanager.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import br.com.joaoborges.filemanager.ui.TelaPrincipal;

/**
 * Listener que executa quando o usuario deseja sair da aplicacao.
 * 
 * @author Joao
 */
public class ExitListener implements ActionListener {

	private TelaPrincipal tela;

	public ExitListener(TelaPrincipal tela) {
		this.tela = tela;
	}

	/**
	 * Efetua a acao de saida.
	 */
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(tela.getFrame(), "Até Logo.", "Saindo",
				JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}

}
