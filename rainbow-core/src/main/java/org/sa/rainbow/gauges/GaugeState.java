package org.sa.rainbow.gauges;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public class GaugeState implements IGaugeState {

    private Set<TypedAttributeWithValue>            m_setupParams;
    private Set<IRainbowModelCommandRepresentation> m_commands;
    private Set<TypedAttributeWithValue>            m_configParams;

    public GaugeState (Collection<TypedAttributeWithValue> setupPArams,
            Collection<TypedAttributeWithValue> configParams, Collection<IRainbowModelCommandRepresentation> commands) {
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
    public Collection<IRainbowModelCommandRepresentation> getGaugeReports () {
        return m_commands;
    }

}
