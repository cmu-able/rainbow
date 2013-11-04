package org.sa.rainbow.core.gauges;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.HashCodeUtil;

public class GaugeState implements IGaugeState {

    private Set<TypedAttributeWithValue>            m_setupParams;
    private Set<IRainbowOperation> m_commands;
    private Set<TypedAttributeWithValue>            m_configParams;

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
