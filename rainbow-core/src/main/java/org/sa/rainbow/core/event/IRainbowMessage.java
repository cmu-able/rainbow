/**
 * Created November 27, 2006.
 */
package org.sa.rainbow.core.event;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;

/**
 * Interface for an event message in the Rainbow framework.  The underlying
 * form of a message is a set of key-value pairs.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IRainbowMessage {

    public static final String CHANNEL = "channel";
    public static final String MESSAGE_CREATED = "msg-created";
    public static final String MESSAGE_SENT = "msg-sent";
    public static final String START_STRATEGY_TYPE      = "START_STRATEGY";
    public static final String END_STRATEGY_TYPE        = "END_STRATEGY";
    public static final String START_TACTIC_TYPE        = "START_TACTIC";
    public static final String END_TACTIC_TYPE          = "END_TACTIC";
    public static final String AWAIT_TACTIC_SETTLE_TYPE = "AWAIT_TACTIC_SETTLE";

    public List<String> getPropertyNames ();

    public Object getProperty (String id);

    public void setProperty (String id, Object prop) throws RainbowException;

}
