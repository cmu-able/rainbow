package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;


/**
 * Interface for a gauge...
 * The Identifiable.id() returns the unique ID of this Gauge. 
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IGauge extends IGaugeIdentifier, IGaugeConfigurationPort, IGaugeQueryPort {

    public static final String SETUP_LOCATION = "targetIP";
    public static final String SETUP_BEACON_PERIOD = "beaconPeriod";
    public static final String SETUP_JAVA_CLASS = "javaClass";
    public static final String CONFIG_PROBE_MAPPING = "targetProbeType";
    public static final String CONFIG_PROBE_MAPPING_LIST = "targetProbeList";
    public static final String CONFIG_SAMPLING_FREQUENCY = "samplingFrequency";
    /** Determines the max number of updates to do per instance of sleep, to
     *  prevent one Gauge from hogging CPU in case of massive updates. */
    public static final int MAX_UPDATES_PER_SLEEP = 100;

    public static final String ALL_LOCATIONS             = "*";

    /**
     * Returns the Gauge liveness beacon period, in milliseconds.
     * @return int  the beacon period in milliseconds
     */
    public long beaconPeriod ();




}
