package org.sa.rainbow.translator.probes;

import java.util.Map;

import org.sa.rainbow.core.IRainbowRunnable;


/**
 * Interface for a standard Probe, encoding its lifecycle states.
 * Valid probe transitions are:<ol>
 * <li> Create: NULL -> INACTIVE
 * <li> Destroy: INACTIVE -> NULL
 * <li> Activate: INACTIVE -> ACTIVE
 * <li> Deactivate: ACTIVE -> INACTIVE
 * </ol>
 * Any other transitions should trigger a BadLifecycleStepException.
 * <p>
 * A Probe has a unique ID, set at instantiation time in the form of
 * "{name}@{location}".  The {name} should be unique within the {location} in
 * which the Probe is deployed.  The {location} is usually the hostname where
 * the Probe is deployed.  If location is unknown, its value should be set to
 * <code>NULL_LOCATION</code>.
 * <p>
 * Identifiable.id() should return the unique ID of this Probe, as "name@location".
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IProbe extends IProbeIdentifier {

    public static enum State {
        /** Indicates a Probe that has not been created or has been destroyed. */
        NULL,
        /** Indicates a Probe that has been created by isn't active. */
        INACTIVE,
        /** Indicates a Probe that is active and reporting. */
        ACTIVE
    }

    public static enum Kind {
        /** An probe based on shell or Perl script */
        SCRIPT,
        /** An probe that relays from a script-based probe */
        RELAY,
        /** An probe implemented purely in Java */
        JAVA
    }

    public static enum Lifecycle {
        /**
         * A lifecycle event to transition the Probe state from
         * <code>State.NULL</code> to <code>State.INACTIVE</code>. */
        CREATE,
        /**
         * A lifecycle event to transition the Probe state from
         * <code>State.INACTIVE</code> to <code>State.ACTIVE</code>. */
        ACTIVATE,
        /**
         * A lifecycle event to transition the Probe state from
         * <code>State.ACTIVE</code> to <code>State.INACTIVE</code>. */
        DEACTIVATE,
        /**
         * A lifecycle event to transition the Probe state from
         * <code>State.INACTIVE</code> to <code>State.NULL</code>. */
        DESTROY
    }

    /** Milliseconds in duration to set the liveness beacon */
    public static final long INTERNAL_BEACON_DURATION = 10*IRainbowRunnable.LONG_SLEEP_TIME;
    public static final String LOCATION_SEP = "@";
    public static final String NULL_LOCATION = "NULL";
    /** The de-register signal to send to socket-based Probe */
    public static final String DEREGISTER_SIGNAL = ".DEREG.\n";
    /** The kill signal to send to socket-based Probe */
    public static final String KILL_SIGNAL = ".KILL.\n";
    /** The kill acknowledgement that should be sent back to the RainbowDelegate */
    public static final String KILL_ACK = "ACK kill";
    public static final String PROBE_REGISTER_PREFIX = "$$+";
    public static final String PROBE_ANNOUNCE_PREFIX = "$$*";
    public static final String PROBE_DEREGISTER_PREFIX = "$$-";
    public static final String PROBE_CMD_DELIMITER = ">";

    /**
     * Returns the {@link Kind} of probe, may be Java, Script, etc.
     * @return Kind  the implementation variant of the Probe 
     */
    public Kind kind ();

    public void create ();

    public void deactivate ();

    public void destroy ();

    /**
     * Makes a Lifecycle transition as requested by the argument; a convenience
     * method that calls one of
     * <code>IProbe.create()</code>, <code>IProbe.activate()</code>,
     * <code>IProbe.deactivate()</code>, or <code>IProbe.destroy()</code>.
     * @param lc the Lifecycle transition command
     */
    public void lcTransition (Lifecycle lc);

    /**
     * Returns the current lifecycle state of this probe.
     * @return One of the enumerated <code>State</code>s.
     */
    public State lcState ();

    /**
     * Convenience method to check whether this Probe is active.
     * @return <code>true</code> if this Probe is in <code>State.ACTIVE</code>, <code>false</code> otherwise.
     */
    public boolean isActive ();

    /**
     * Returns whether this Probe is active AND alive, according to some timed
     * notion of liveness, usually implemented using a beacon.  The default
     * implemention will check to see if Probe is activated, in which case
     * isAlive is equivalent to isActive(); otherwise, it is considered alive.
     * @return <code>true</code> if this Probe is alive and still reporting, <code>false</code> otherwise.
     */
    public boolean isAlive ();

    /**
     * Configures the Probe with a hash of key-value pairs.  Probe may use this
     * to handle runtime configuration input from the Rainbow infrastructure,
     * most likely from a Gauge.
     * @param configParams  the Map of config parameters keyed by a String label to an Object
     */
    public void configure (Map<String,Object> configParams);

    /**
     * Reports a line of data to the Gauge.
     * For a Probe that runs within the RainbowDelegate process, this method
     * queues the data for processing by the ProbeBusRelay.
     * For a standalone Probe, the data should eventually be forwarded on to the
     * Rainbow socket, which may be done by this method, or in a separate Thread.
     * @param data  the data String to report to Gauge(s)
     */
    public void reportData (String data);

    public void activate ();


}
