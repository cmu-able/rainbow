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
package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.HashCodeUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GaugeState implements IGaugeState {


    private final Set<TypedAttributeWithValue> m_setupParams;

    private final Set<IRainbowOperation> m_commands;

    private final Set<TypedAttributeWithValue> m_configParams;

    public GaugeState (Collection<TypedAttributeWithValue> setupPArams,
                       Collection<TypedAttributeWithValue> configParams, Collection<IRainbowOperation> commands) {
        m_setupParams = new HashSet<> (setupPArams);
        m_configParams = new HashSet<> (configParams);
        m_commands = new HashSet<> (commands);
    }

    /*
     * (non-Javadoc)
     * @see org.sa.rainbow.gauges.IGaugeState#getSetupParams()
     */

    @Override
    public Collection<TypedAttributeWithValue> getSetupParams () {
        return m_setupParams;
    }

    /*
     * (non-Javadoc)
     * @see org.sa.rainbow.gauges.IGaugeState#getConfigParams()
     */

    @Override
    public Collection<TypedAttributeWithValue> getConfigParams () {
        return m_configParams;
    }

    /*
     * (non-Javadoc)
     * @see org.sa.rainbow.gauges.IGaugeState#getGaugeReports()
     */

    @Override
    public Collection<IRainbowOperation> getGaugeReports () {
        return m_commands;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) return true;
        if (obj instanceof GaugeState) {
            GaugeState g = (GaugeState )obj;
            return m_setupParams.equals (g.m_setupParams) && m_configParams.equals (g.m_setupParams)
                    && m_commands.equals (g.m_commands);
        }
        return false;
    }

    @Override
    public int hashCode () {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash (result, m_setupParams);
        result = HashCodeUtil.hash (result, m_configParams);
        result = HashCodeUtil.hash (result, m_commands);
        return result;
    }
}
