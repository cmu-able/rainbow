/**
 * Created January 31, 2007.
 */
package org.sa.rainbow.core.gauges;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class holds gauge specification information parsed from its description
 * file (usu. Yaml).
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GaugeDescription {

    public SortedMap<String,GaugeTypeDescription> typeSpec = null;
    public SortedMap<String,GaugeInstanceDescription> instSpec = null;

    /**
     * Default Constructor.
     */
    public GaugeDescription() {
        typeSpec = new TreeMap<String,GaugeTypeDescription>();
        instSpec = new TreeMap<String,GaugeInstanceDescription>();
    }

    public List<GaugeInstanceDescription> instDescList () {
        List<GaugeInstanceDescription> instSpecs = null;
        synchronized (instSpec) {
            instSpecs = new ArrayList<GaugeInstanceDescription>(instSpec.values());
        }
        return instSpecs;
    }
}
