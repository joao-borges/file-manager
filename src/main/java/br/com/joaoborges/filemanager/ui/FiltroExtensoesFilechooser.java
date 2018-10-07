package br.com.joaoborges.filemanager.ui;

import java.io.File;
import java.util.List;

import br.com.joaoborges.filemanager.model.FiltroExtensoes;

public class FiltroExtensoesFilechooser extends FiltroExtensoes {

	public FiltroExtensoesFilechooser(List<String> extensoes) {
		super(extensoes);
	}

	@Override
	public boolean accept(File pathname) {
		boolean accepted = super.accept(pathname);
		if (!accepted && pathname.isDirectory()) {
			accepted = true;
		}
		return accepted;
	}
}
