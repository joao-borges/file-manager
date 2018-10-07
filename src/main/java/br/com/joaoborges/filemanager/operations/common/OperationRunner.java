package br.com.joaoborges.filemanager.operations.common;

import static br.com.joaoborges.filemanager.operations.interfaces.OperationParamsBuilder.BEAN_NAME_FORMAT;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.exception.FileManagerException;
import br.com.joaoborges.filemanager.operations.interfaces.FileOperation;
import br.com.joaoborges.filemanager.operations.interfaces.OperationParamsBuilder;
import br.com.joaoborges.filemanager.operations.interfaces.OperationResult;

/**
 * Servico que roda as operacoes fazendo a selecao de arquivos e controlando o ciclo de vida da operação.
 * 
 * @author Joao
 * @version 10/03/2012 15:50:13
 */
@Component
public class OperationRunner {

	private static final Logger LOGGER = Logger.getLogger(OperationRunner.class);

	/**
	 *
	 * @param operation
	 * @param parentReference
	 * @return OperationResult
	 * @throws FileManagerException
	 */
	public <T extends OperationResult> T execute(FileOperation<T> operation, java.awt.Component parentReference)
			throws FileManagerException {
		OperationParamsBuilder builder = SpringUtils.getContext().getBean(
				BEAN_NAME_FORMAT + operation.getOperationID(), OperationParamsBuilder.class);

		var params = builder.buildParams();
		if (params.isEmpty()) {
			return null;
		}
		T result = operation.execute(params);
		return result;
	}

	/**
	 *
	 * @param parentReference
	 */
	public void notifySuccess(java.awt.Component parentReference) {
		JOptionPane.showMessageDialog(parentReference, "Operacao executada com sucesso", "Informacao",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 *
	 * @param e
	 * @param parentReference
	 */
	public void notifyFail(FileManagerException e, java.awt.Component parentReference) {
		LOGGER.error("ERRO!!!!!");
		LOGGER.error(e.getMessage(), e);
		JOptionPane.showMessageDialog(parentReference, "Ocorreu um erro Inesperado.\n\n" + e.getMessage(), "Erro!",
				JOptionPane.ERROR_MESSAGE);
	}

}
