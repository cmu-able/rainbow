package org.sa.rainbow.core.management.ports;

import java.util.Properties;

/**
 * This interface specifies the port interface through which the Rainbow master interacts with a delegate. It is
 * intended that there be one port per delegate in the master.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IRainbowManagementPort {
    // identification information

    /**
     * 
     * @return an ID of the delegate
     */
    public String getDelegateId ();

    // Master --> Delegate

    /**
     * Sends configuration information to the delegate attached to this port. The configuration information is a set of
     * properties intended to specify how the delegate should connect to various busses and communication channels.
     * Other information (such as what gauges, probes, etc to start) may be passed through other methods in this
     * interface.
     * 
     * @param configuration
     */
    public void sendConfigurationInformation (Properties configuration);

    // Delegate --> Master
    /**
     * This is called by the delegate periodically to indicate that it is still alive. The intent is that this method
     * forward information to the Master
     */
    public void heartbeat ();

    /**
     * This is called by a delegate when it wants configuration information to be resent.
     */
    public void requestConfigurationInformation ();

    // Lifecycle commands
    // Master --> Delegate

    /**
     * Tells the delegate to start. This method will block until the delegate has successfully started (in which case it
     * will return true) or fails to start (in which case it will return false).
     * 
     * @throws IllegalStateException
     *             Thrown if the delegate is not in a state where it can be started. (e.g., it was terminated)
     */
    public boolean startDelegate () throws IllegalStateException;

    /**
     * Tells the delegate to pause, blocking until the delegate has successfully paused (in which case it will return
     * true) or fails to pause (returning false).
     * 
     * @return
     * @throws IllegalStateException
     *             Thrown if the delegate is not in a state where it can be paused.
     */
    public boolean pauseDelegate () throws IllegalStateException;

    /**
     * Tells the delegate to terminate, effectively shutting it down. Ideally, before terminating, the delegate will
     * disconnect from the master.
     * 
     * @return
     * @throws IllegalStateException
     */
    public boolean terminateDelegate () throws IllegalStateException;

    /**
     * Should be called when this port is no longer required. Implementors should dispose of all resources.
     */
    public void dispose ();

}
