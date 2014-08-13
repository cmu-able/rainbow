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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConnector;

/**
 * Generic callback to process events from a given channel by the Runtime Aggregator.
 *
 * <p>
 * This class simply redirects an event to the Runtime Aggregator processEvent method.
 * </p>
 * 
 * @see IRuntimeAggregator
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
class GenericRuntimeAggregatorCallback implements IRainbowListenerCallback {
    /**
     * Channel to listen events from
     */
    private final ESEBConnector.ChannelT channel;
    /**
     * Runtime aggregator used for events processing
     */
    private final IRuntimeAggregator<?> runtimeAggregator;

    /**
     * Constructor for a given runtime aggregator.
     * @param ra Runtime Aggregator
     * @param ch Channel
     */
    GenericRuntimeAggregatorCallback(IRuntimeAggregator<?> ra, ESEBConnector.ChannelT ch) {
        channel = ch;
        runtimeAggregator = ra;
    }

    @Override
    public void onEvent(IRainbowMessage msg) {
        try {
            runtimeAggregator.processEvent(channel.toString(), msg);
        } catch (EventProcessingException ex) {
            Logger.getLogger(ESEBRainbowConnector.class.getName()).log(Level.SEVERE, "Cannot process event from channel " + channel.toString(), ex);
        }
    }

}
