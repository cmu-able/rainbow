/**
 * Created March 18, 2007.
 */
package org.sa.rainbow.core.models;

import java.util.SortedSet;
import java.util.TreeSet;

import org.sa.rainbow.translator.probes.IProbe;

/**
 * This class holds probe description information parsed from its description
 * file (usu. Yaml).
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ProbeDescription {

    public static class ProbeAttributes extends DescriptionAttributes {
        public String alias = null;
        public IProbe.Kind kind = null;
    }

    public SortedSet<ProbeAttributes> probes = null;

    /**
     * Default Constructor.
     */
    public ProbeDescription() {
        probes = new TreeSet<ProbeAttributes>();
    }

}
