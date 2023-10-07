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

package br.com.joaoborges.filemanager.operations;

import br.com.joaoborges.filemanager.type.ReplacingConstants;

/**
 * @author JoãoGabriel
 */
public class Utilitario {

	/**
	 * Efetua as operacoes basicas e separa a extensao do nome.
	 * 
	 * @param newName
	 * @return String[]
	 */
	public static String[] splitExtension(String newName) {
		String[] retorno = new String[2];

		retorno[1] = newName.substring(newName.lastIndexOf(ReplacingConstants.POINT) + 1);
		retorno[0] = newName.substring(0, newName.lastIndexOf(ReplacingConstants.POINT));
		return retorno;
	}

	public static String getBlockBehindChar(String fullString, int i) {
		int beforeSpace = -1;
		int afterSpace = fullString.length();

		for (int j = i; j >= 0; j--) {
			if (fullString.charAt(j) == ReplacingConstants.SPACE.charAt(0)) {
				beforeSpace = j;
				break;
			}
		}
		for (int k = i; k < fullString.length(); k++) {
			if (fullString.charAt(k) == ReplacingConstants.SPACE.charAt(0)) {
				afterSpace = k;
				break;
			}
		}

		String part = fullString.substring(beforeSpace + 1, afterSpace);
		return part;
	}
}
