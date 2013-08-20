package org.sa.rainbow.gauges;

import java.util.Collection;

import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

/**
 * This interface defines the set of methods for returning the state of gauge. The state of the gauge is defined as all
 * of the setup parameters, the current configuration parameters, and the last set of commands that were issued.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeState {
    /**
     * 
     * @return The collection of setup parameters that were used to set up this gauge.
     */
    Collection<? extends TypedAttributeWithValue> getSetupParams ();

    /**
     * 
     * @return The set of configuration parameters (and their current values) with which the gauge has been configured.
     */
    Collection<? extends TypedAttributeWithValue> getConfigParams ();

    /**
     * 
     * @return The set of commands that were most recently issued (one entry per command kind).
     */
    Collection<? extends IRainbowModelCommandRepresentation> getGaugeReports ();
}
