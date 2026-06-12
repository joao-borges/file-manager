package ca.joaoborges.filemanager.operations.renaming;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

import lombok.extern.slf4j.Slf4j;

import static com.thoughtworks.xstream.XStream.NO_REFERENCES;

/**
 * Manages loading of the name fragments that must be excluded from the renaming operation.
 */
@Component
@Slf4j
public class ExclusionManagerService implements InitializingBean {

    private XStream xStream;
    private List<String> exclusions;

    public void afterPropertiesSet() throws Exception {
        try {
            this.xStream = new XStream();
            xStream.addPermission(AnyTypePermission.ANY);
            xStream.ignoreUnknownElements();
            xStream.setMode(NO_REFERENCES);
            xStream.autodetectAnnotations(true);
            this.xStream.processAnnotations(ExclusionsBean.class);
        } catch (final Exception rethrown) {
            log.error(rethrown.getMessage(), rethrown);
            throw new RuntimeException("Could not initialize XStream.", rethrown);
        }

        this.exclusions = new ArrayList<>();
        final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("exclusions.xml");
        while (resources.hasMoreElements()) {
            final URL exclusionFile = resources.nextElement();
            log.debug("Parsing: " + exclusionFile.getFile());

            final InputStream input = exclusionFile.openStream();
            final String xml = IOUtils.toString(input);
            log.info(xml);
            final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

            final ExclusionsBean bean = (ExclusionsBean) this.xStream.fromXML(stream);
            if (bean.getExclusions() != null) {
                for (final Exclusion exclusion : bean.getExclusions()) {
                    this.exclusions.add(exclusion.getValue());
                }
            }
        }
    }

    public boolean hasExclusionFor(final String str, final boolean considerPartString) {
        for (final String exclusion : this.exclusions) {
            if (str.equalsIgnoreCase(exclusion) || (considerPartString && str.contains(exclusion.toLowerCase()))) {
                return true;
            }
        }

        return false;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

}
