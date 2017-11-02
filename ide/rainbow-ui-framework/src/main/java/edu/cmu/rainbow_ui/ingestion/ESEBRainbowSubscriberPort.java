/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
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
package edu.cmu.rainbow_ui.ingestion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

/**
 * An ESEB port passive event listeners.
 *
 * <p>
 * This port works only for event receiving. Multiple callback may be associated with a single port.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
class ESEBRainbowSubscriberPort extends AbstractESEBDisposablePort {

    /**
     * Event listeners callbacks
     */
    private final Set<IRainbowListenerCallback> callbacks;

    /**
     * Create a new port for a given channel.
     *
     * @param host Rainbow host
     * @param port Rainbow port
     * @param channel Events channel to listen
     * @throws IOException
     */
    ESEBRainbowSubscriberPort(String host, short port, ESEBConnector.ChannelT channel) throws IOException {
        super(host, port, channel);
        this.callbacks = new HashSet<>();
        getConnectionRole().addListener(new ESEBConnector.IESEBListener() {
            @Override
            public void receive(RainbowESEBMessage msg) {
                for (IRainbowListenerCallback callback : callbacks) {
                    callback.onEvent(msg);
                }
            }
        });
    }

    /**
     * Add new event listener callback.
     *
     * @param callback new callback
     */
    void subscribe(IRainbowListenerCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Remove an event listener callback
     *
     * @param callback callback to remove
     */
    void unsubscribe(IRainbowListenerCallback callback) {
        callbacks.remove(callback);
    }

}
