
package br.com.joaoborges.filemanager.operations.renaming;

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
 * Gerencia o carregamento dos trechos de nome que devem ser excluidos da operacao.
 *
 * @author Joao
 * @version 01/08/2011 16:26:11
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Nao foi possivel iniciar o XStream.", e);
        }

        this.exclusions = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("exclusions.xml");
        while (resources.hasMoreElements()) {
            URL exclusionFile = resources.nextElement();
            log.debug("Parsing: " + exclusionFile.getFile());

            InputStream input = exclusionFile.openStream();
            String xml = IOUtils.toString(input);
            log.info(xml);
            ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

            ExclusionsBean bean = (ExclusionsBean) this.xStream.fromXML(stream);
            if (bean.getExclusions() != null) {
                for (Exclusion e : bean.getExclusions()) {
                    this.exclusions.add(e.getValue());
                }
            }
        }
    }

    public boolean hasExclusionFor(String str, boolean considerPartString) {
        for (String s : this.exclusions) {
            if (str.equalsIgnoreCase(s) || (considerPartString && str.contains(s.toLowerCase()))) {
                return true;
            }
        }

        return false;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

}
