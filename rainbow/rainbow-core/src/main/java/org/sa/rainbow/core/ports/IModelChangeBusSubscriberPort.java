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
    interface IRainbowChangeBusSubscription {
        /**
         * Returns true if the message matches the subscription
         * 
         * @param message
         * @return
         */
        boolean matches (IRainbowMessage message);
    }

    /**
     * Represents a callback. Implementations must be passed when subscribing on the port.
     * 
     * @param <T>
     *            The type of the model
     */
    interface IRainbowModelChangeCallback {
        /**
         * The method will to be called. The message represents the event. The model is the model in Rainbow that the
         * event is associated with.
         * 
         * @param model
         * @param message
         */
        void onEvent (ModelReference reference, IRainbowMessage message);
    }

    /**
     * Subscribe to the change bus
     * 
     * @param subscription
     *            The subscription
     * @param callback
     *            The callback to call when a message of the subscription matches
     */
    void subscribe (IRainbowChangeBusSubscription subscription, IRainbowModelChangeCallback callback);

    /**
     * Unsubscribes the callback from the bus
     * 
     * @param callback
     */
    void unsubscribe (IRainbowModelChangeCallback callback);

}
