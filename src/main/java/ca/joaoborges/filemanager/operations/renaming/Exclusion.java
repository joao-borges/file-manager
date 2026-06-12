package ca.joaoborges.filemanager.operations.renaming;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A single name fragment that must be excluded from the renaming operation.
 */
@XStreamAlias("exclusion")
public class Exclusion implements Serializable {

    private static final long serialVersionUID = 4670548162086147533L;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

}
