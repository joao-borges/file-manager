
package br.com.joaoborges.filemanager.ui.utils;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * Mensagem de aguarde para o usu�rio enquanto e feita a operacao.
 * 
 * @author Jo�o
 * @version 04/01/2011 11:38:02
 */
public class TelaEsperando implements Runnable {
	
	private JFrame framePai;
	private JDialog dialog;
	private JOptionPane pane;
	
	public TelaEsperando (JFrame framePai) {
		super();
		this.framePai = framePai;
	}
	
	public void run () {
		this.pane = new JOptionPane("Aguarde...", JOptionPane.INFORMATION_MESSAGE);
		this.pane.setOptions(new Object[0]);
		this.dialog = new JDialog(this.framePai, "Aguarde", true);
		this.dialog.setContentPane(this.pane);
		this.dialog.setModal(true);
		this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.dialog.setSize(400, 300);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(this.framePai);
		
		Thread controlThread = new Thread(new InternalUpdateProcess());
		controlThread.start();
		
		this.dialog.setVisible(true);
	}
	
	public void close () {
		this.dialog.setVisible(false);
	}
	
	private class InternalUpdateProcess implements Runnable {
		
		public void run () {
			while (true) {
				try {
	                Thread.sleep(500);
                } catch (InterruptedException e) {
                	// algum fdp nao me deixa dormir q saco
                }
				String message = (String) TelaEsperando.this.pane.getMessage();
				if (message.substring(7).length() < 5) {
					message = message + ".";
				} else {
					message = message.substring(0, 7);
				}
				TelaEsperando.this.pane.setMessage(message);
			}
		}
	}
}
