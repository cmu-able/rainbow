package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;

/**
 * This port is the interface for subscribing to messages on the change bus Clients to this port register a
 * subscription, and a callback to be called when a message matching the subscription appears on the bus.
 * 
 */
public interface IModelChangeBusSubscriberPort extends IDisposablePort {

    /**
     * Represents a subscription
     * 
     */
    public interface IRainbowChangeBusSubscription {
        /**
         * Returns true if the message matches the subscription
         * 
         * @param message
         * @return
         */
        public boolean matches (IRainbowMessage message);
    }

    /**
     * Represents a callback. Implementations must be passed when subscribing on the port.
     * 
     * @param <T>
     *            The type of the model
     */
    public interface IRainbowModelChangeCallback<T> {
        /**
         * The method will to be called. The message represents the event. The model is the model in Rainbow that the
         * event is associated with.
         * 
         * @param model
         * @param message
         */
        public void onEvent (ModelReference reference, IRainbowMessage message);
    }

    /**
     * Subscribe to the change bus
     * 
     * @param subscription
     *            The subscription
     * @param callback
     *            The callback to call when a message of the subscription matches
     */
    public void subscribe (IRainbowChangeBusSubscription subscription, IRainbowModelChangeCallback callback);

    /**
     * Unsubscribes the callback from the bus
     * 
     * @param callback
     */
    public void unsubscribe (IRainbowModelChangeCallback callback);

}
