package br.com.joaoborges.filemanager.ui.operations;

import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.exception.InvalidDirectoryException;
import br.com.joaoborges.filemanager.exception.OperationCancelledException;
import br.com.joaoborges.filemanager.operations.common.OperationRunner;
import br.com.joaoborges.filemanager.operations.common.SpringUtils;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;
import br.com.joaoborges.filemanager.ui.TelaPrincipal;
import br.com.joaoborges.filemanager.ui.utils.WaitingScreenControl;
import lombok.extern.slf4j.Slf4j;

/**
 * Processo que roda a operação.
 *
 * @author JoãoGabriel
 */
@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class OperationProcess implements Runnable {

	@Autowired
	private TelaPrincipal tela;
	@Autowired
	private OperationRunner operationRunner;
	private String actionID;

	public OperationProcess forAction(String actionID) {
		this.actionID = actionID;
		return this;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		FileOperation<OperationResult> operation = SpringUtils.getContext().getBean(this.actionID, FileOperation.class);
		UIOperationResultProcessor<OperationResult> processor = SpringUtils.getContext().getBean(
				UIOperationResultProcessor.BEAN_NAME_FORMAT + this.actionID, UIOperationResultProcessor.class);
		WaitingScreenControl control = new WaitingScreenControl(this.tela);
		try {
			OperationResult result = this.operationRunner.execute(operation, this.tela.getFrame());
			processor.processResult(result, this.tela);
			control.interruptWaiting();
			this.operationRunner.notifySuccess(this.tela.getFrame());
		} catch (InvalidDirectoryException e) {
			control.interruptWaiting();
			JOptionPane.showMessageDialog(this.tela.getFrame(), "Selecione um diretorio valido.", "Erro",
					JOptionPane.ERROR_MESSAGE);
		} catch (OperationCancelledException e) {
			control.interruptWaiting();
			JOptionPane.showMessageDialog(this.tela.getFrame(), "Operacao cancelada pelo Usuario.", "Informacao",
					JOptionPane.WARNING_MESSAGE);
		} catch (RuntimeException | Error e) {
			control.interruptWaiting();
			this.operationRunner.notifyFail(e, this.tela.getFrame());
			log.warn(e.getMessage(), e);
		}

	}
}
