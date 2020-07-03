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
 * Created November 1, 2006.
 */
package org.sa.rainbow.translator.swim.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * Simple gauge: complete probe output passed as arg to command
 * 
 * @author gmoreno
 */
public class SimpleGauge extends RegularPatternGauge {

    public static final String NAME = "G - SimpleGauge";

    private static final String DEFAULT = "DEFAULT";

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public SimpleGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT, Pattern.compile (".*"));
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#initProperty(java.lang.String, java.lang.Object)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            String value = m.group(0);
            String commandName = m_commands.keySet().iterator().next();
            m_reportingPort.trace (getComponentType (),
            		"Updating component prop using command "
            				+  commandName + " and value = " + value);
            IRainbowOperation cmd = getCommand (commandName);
            Map<String, String> parameterMap = new HashMap<> ();
            parameterMap.put (cmd.getParameters ()[0], value);
            issueCommand (cmd, parameterMap);
        }
    }

}
