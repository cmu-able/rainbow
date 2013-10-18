package org.sa.rainbow.core.ports;

public interface IProbeReportSubscriberPort {

    /**
     * Indicate interest in reports from a particular probe, indicated by type and location. This is cumulative, i.e.,
     * if the port is already subscribed to a probe and this method is called with a different probe, then the original
     * probe(s) will still report information.
     * 
     * @param probeType
     *            The type of the probe to express interest in
     * @param location
     *            The location of the probe to express interest in. If this is null, then probes of the indicated type
     *            reporting from any location are subscribed to.
     */
    public abstract void subscribeToProbe (String probeType, String location);

    /**
     * Indicates that reports should no longer be received from the indicated probe at the location, through this port
     * 
     * @param probeType
     *            The type of the probe no longer interested in
     * @param location
     *            The location of the probe no longer interested in. If the original subscription specified null for
     *            this probe (meaning it was interested in all locations), then probes at all other locations will still
     *            be subscribed to. If this value is null, then probe types from all locations will be unsubscribed.
     */
    public abstract void unsubscribeToProbe (String probeType, String location);

}
