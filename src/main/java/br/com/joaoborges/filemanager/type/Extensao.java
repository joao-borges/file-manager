package br.com.joaoborges.filemanager.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.joaoborges.filemanager.model.FiltroExtensoes;
import br.com.joaoborges.filemanager.model.util.Message;
import br.com.joaoborges.filemanager.ui.FiltroExtensoesFilechooser;

/**
 * Extensoes suportadas pelo sistema.
 *
 * @author Joao
 */
public enum Extensao {

	/**
	 *
	 */
	MP3(0, FileType.AUDIO),
	/**
	 *
	 */
	WMA(1, FileType.AUDIO),
	/**
	 *
	 */
	WMV(3, FileType.VIDEO),
	/**
	 *
	 */
	MPEG(3, FileType.VIDEO),
	/**
	 *
	 */
	MPG(3, FileType.VIDEO),
	/**
	 *
	 */
	WAV(4, FileType.AUDIO),
	/**
	 *
	 */
	JPG(5, FileType.IMAGE),
	/**
	 *
	 */
	JPEG(5, FileType.IMAGE),
	/**
	 *
	 */
	BMP(6, FileType.IMAGE),
	PNG(6, FileType.IMAGE),
	MOV(3, FileType.VIDEO),
	MP4(3, FileType.VIDEO),
	AVI(3, FileType.VIDEO),
	TXT(7, FileType.TEXT);

	private final String descricao;
	private final int grupo;
	private final String tipo;
	private final String extensao;

	private Extensao(int grupo, String tipo) {
		this.grupo = grupo;
		this.extensao = name().toLowerCase();
		this.descricao = Message.getMessage(Message.GRUPOSEXTENSOES, Integer.toString(this.grupo));
		this.tipo = tipo;
	}

	public String getTipo() {
		return this.tipo;
	}

	public String getDescricao() {
		return this.descricao;
	}

	public int getGrupo() {
		return this.grupo;
	}

	public String getExtensao() {
		return this.extensao;
	}

	public static List<String> asStrings() {
		return Arrays.stream(Extensao.values()).map(e -> e.extensao).sorted().collect(Collectors.toList());
	}

	public static List<FiltroExtensoes> getFCFilters() {
		Map<Integer, FiltroExtensoes> mapa = new HashMap<Integer, FiltroExtensoes>();
		for (Extensao e : values()) {
			if (mapa.get(e.getGrupo()) != null) {
				mapa.get(e.getGrupo()).addExtensao(e.extensao);
			} else {
				FiltroExtensoes fe = new FiltroExtensoesFilechooser(Arrays.asList(new String[] { e.extensao }));
				fe.setDescription(e.getDescricao());
				mapa.put(e.getGrupo(), fe);
			}
		}
		List<FiltroExtensoes> filtros = new ArrayList<FiltroExtensoes>();
		filtros.addAll(mapa.values());
		Collections.sort(filtros);
		return filtros;
	}

	public static List<FiltroExtensoes> getFCFiltersByFileType(String type) {
		Map<Integer, FiltroExtensoes> mapa = new HashMap<Integer, FiltroExtensoes>();
		for (Extensao e : values()) {
			if (e.getTipo().equals(type)) {
				if (mapa.get(e.getGrupo()) != null) {
					mapa.get(e.getGrupo()).addExtensao(e.extensao);
				} else {
					FiltroExtensoes fe = new FiltroExtensoesFilechooser(Arrays.asList(new String[] { e.extensao }));
					fe.setDescription(e.getDescricao());
					mapa.put(e.getGrupo(), fe);
				}
			}
		}
		List<FiltroExtensoes> filtros = new ArrayList<FiltroExtensoes>();
		filtros.addAll(mapa.values());
		Collections.sort(filtros);
		return filtros;
	}

	public Extensao getExtensao(String extensaoAsString) {
		for (Extensao ext : values()) {
			if (ext.getExtensao().equalsIgnoreCase(extensaoAsString)) {
				return ext;
			}
		}
		throw new RuntimeException("Valor invalido para Extensao.");
	}

	public static Extensao[] getAll() {
		return Extensao.values();
	}

	public static List<String> getAsStringByTipo(String type) {
		List<String> lista = new ArrayList<String>();
		for (Extensao ext : getAll()) {
			if (ext.getTipo().equals(type)) {
				lista.add(ext.extensao);
			}
		}
		return lista;
	}
}
