/*
 * Copyright (c) 1999-2010 Touch Tecnologia e Informatica Ltda. 
 * 
 * R. Gomes de Carvalho, 1666, 3o. Andar, Vila Olimpia, Sao Paulo, SP, Brasil. 
 * 
 * Todos os direitos reservados. 
 * Este software e confidencial e de propriedade da Touch Tecnologia e Informatica Ltda. (Informacao Confidencial) 
 * As informacoes contidas neste arquivo nao podem ser publicadas, e seu uso esta limitado de acordo
 * com os termos do contrato de licenca.
 */

package br.com.joaoborges.filemanager.operations.renaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.operations.Utilitario;

/**
 * Operações de renomeio.
 * 
 * @author JoãoGabriel
 */
@Component
public class RenamingOperations {

	@Autowired
	private ExclusionManagerService exclusions;

	public String doReplaceAll(String stringToReplace, String charToBeReplaced, String substituteChar) {
		for (int i = 0; i < stringToReplace.length(); i++) {
			char c = stringToReplace.charAt(i);
			if (charToBeReplaced.equals(new String(new char[] { c }))) {
				if (!this.exclusions.hasExclusionFor(Utilitario.getBlockBehindChar(stringToReplace, i), true)) {
					stringToReplace = stringToReplace.substring(0, i) + substituteChar
							+ stringToReplace.substring(i + 1);
				}
			}
		}
		return stringToReplace;
	}
}
