package org.sa.rainbow.gauges;

import java.util.List;

import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * This interface represents the API through which gauges might be configured at runtime
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeConfigurationInterface {
    /**
     * Configures the parameters of this Gauge using the supplied configuration parameter values.
     * 
     * @param configParams
     *            list of type-name-value triples of configuration parameters
     * @return boolean <code>true</code> if configuration succeeds, <code>false</code> otherwise
     */
    public boolean configureGauge (List<TypedAttributeWithValue> configParams);

    /**
     * Causes the IGauge to call configureGauge on itself using its existing config parameters. This method is currently
     * used to reconnect to IProbes.
     * 
     * @return boolean <code>true</code> if configure call succeed, <code>false</code> otherwise
     */
    public boolean reconfigureGauge ();

}
