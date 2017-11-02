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

import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;


/**
 * Interface for a gauge...
 * The Identifiable.id() returns the unique ID of this Gauge. 
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IGauge extends IGaugeIdentifier, IGaugeConfigurationPort, IGaugeQueryPort {

    String SETUP_LOCATION = "targetIP";
    String SETUP_BEACON_PERIOD = "beaconPeriod";
    String SETUP_JAVA_CLASS = "javaClass";
    String CONFIG_PROBE_MAPPING = "targetProbeType";
    String CONFIG_PROBE_MAPPING_LIST = "targetProbeList";
    String CONFIG_SAMPLING_FREQUENCY = "samplingFrequency";
    /** Determines the max number of updates to do per instance of sleep, to
     *  prevent one Gauge from hogging CPU in case of massive updates. */
    int MAX_UPDATES_PER_SLEEP = 100;

    String ALL_LOCATIONS = "*";

    /**
     * Returns the Gauge liveness beacon period, in milliseconds.
     * @return int  the beacon period in milliseconds
     */
    long beaconPeriod ();




}
