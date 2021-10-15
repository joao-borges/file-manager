package br.com.joaoborges.filemanager.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractButton;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.operations.common.SpringUtils;
import br.com.joaoborges.filemanager.ui.operations.OperationProcess;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener que é disparado quando o usuario solicita uma operacao.
 * 
 * @author Joao
 */
@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class OperationExecuteListener implements ActionListener {

	private ExecutorService threadPool = Executors.newFixedThreadPool(10);

	public void actionPerformed(ActionEvent event) {
		log.debug("Executando!");
		AbstractButton source = (AbstractButton) event.getSource();

		String actionID = source.getActionCommand();
		this.threadPool.submit(SpringUtils.getBean(OperationProcess.class).forAction(actionID));
	}

}
