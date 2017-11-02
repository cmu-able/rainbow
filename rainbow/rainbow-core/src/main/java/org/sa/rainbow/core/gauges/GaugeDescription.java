/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    public final SortedMap<String, GaugeTypeDescription>     typeSpec;
    public final SortedMap<String, GaugeInstanceDescription> instSpec;

    /**
     * Default Constructor.
     */
    public GaugeDescription() {
        typeSpec = new TreeMap<> ();
        instSpec = new TreeMap<> ();
    }

    public List<GaugeInstanceDescription> instDescList () {
        List<GaugeInstanceDescription> instSpecs;
        synchronized (instSpec) {
            instSpecs = new ArrayList<> (instSpec.values ());
        }
        return instSpecs;
    }
}
