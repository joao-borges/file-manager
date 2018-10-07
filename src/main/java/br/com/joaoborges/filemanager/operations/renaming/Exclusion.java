
package br.com.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Exclusao.
 * 
 * @author Joao
 * @version 01/08/2011 16:40:30
 */
@XStreamAlias("exclusion")
public class Exclusion implements Serializable {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 4670548162086147533L;
	private String value;
	
	public String getValue () {
		return value;
	}
	
	public void setValue (String value) {
		this.value = value;
	}
	
}
