package org.sa.rainbow.translator.probes;

import org.sa.rainbow.core.Identifiable;

public interface IProbeIdentifier extends Identifiable {

    /**
     * The name of this Probe, unique within the deployment location.
     * @return String  the name of this Probe
     */
    public String name ();

    /**
     * The location name where this Probe is deployed, usually the hostname.
     * The location is set via the string ID at Probe instantiation time.
     * @return String  the name of the location where the Probe is deployed.
     */
    public String location ();

    /**
     * The type of the Probe (aka "alias" with RelayProbes) is used to map
     * Gauges to Probes. 
     * @return String  the type name of the Probe 
     */
    public String type ();

}
