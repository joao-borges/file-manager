
package br.com.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Bean que contem a lista de exclusoes.
 * 
 * @author Joao
 * @version 01/08/2011 16:41:19
 */
@XStreamAlias("exclusions")
public class ExclusionsBean implements Serializable {
	
	/** serialVersionUID */
	private static final long serialVersionUID = -4672026331605868526L;

	@XStreamImplicit(itemFieldName = "exclusion")
	private ArrayList<Exclusion> exclusions = new ArrayList<>();
	
	public ArrayList<Exclusion> getExclusions () {
		return exclusions;
	}
	
	public void setExclusions (ArrayList<Exclusion> exclusions) {
		this.exclusions = exclusions;
	}
	
}
