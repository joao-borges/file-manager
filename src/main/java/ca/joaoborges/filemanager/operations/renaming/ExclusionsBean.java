package ca.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;
import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Bean holding the list of exclusions loaded from exclusions.xml.
 */
@XStreamAlias("exclusions")
public class ExclusionsBean implements Serializable {

    private static final long serialVersionUID = -4672026331605868526L;

    @XStreamImplicit(itemFieldName = "exclusion")
    private ArrayList<Exclusion> exclusions = new ArrayList<>();

    public ArrayList<Exclusion> getExclusions() {
        return exclusions;
    }

    public void setExclusions(final ArrayList<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

}
