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

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Describes Rainbow UI Framework Runtime Aggregator interface, defining event handlers methods and
 * method to access internal model and buffer.
 *
 * <p>
 * All events are split into two categories - model updates and streaming events. They are processed
 * by according event handlers. The interface defines access methods for internal model and internal
 * streaming events buffer.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 * @param <T> type of internal model
 */
public interface IRuntimeAggregator<T> {

    /**
     * Start the runtime aggregator.
     * <p>
     * Connects to Rainbow, starts listening to events and processing them.
     * </p>
     *
     * <p>
     * This action may fail with an exception thrown. Since it may be called from the running system
     * this failure should be handled appropriately on the caller side.
     * </p>
     *
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    public void start() throws RuntimeAggregatorException;

    /**
     * Stop the runtime aggregator.
     * <p>
     * Disconnects from Rainbow.
     * </p>
     */
    public void stop();

    /**
     * Process event from particular channel.
     *
     * @param channel - string id of the channel
     * @param event - Rainbow event message
     * @throws edu.cmu.rainbow_ui.ingestion.EventProcessingException
     */
    public void processEvent(String channel, IRainbowMessage event)
            throws EventProcessingException;

    /**
     * Get reference to internal model.
     *
     * @return rainbow model instance
     */
    public IModelInstance<T> getInternalModel();

    /**
     * Copy internal model.
     *
     * The returned copy is disconnected from the internal model, i.e. it will not be changed with
     * event updates. The data in the model is consistent on the time it was requested.
     *
     * @return copy of the internal model
     * @throws org.sa.rainbow.core.error.RainbowCopyException
     */
    public IModelInstance<T> copyInternalModel() throws RainbowCopyException;

    /**
     * Get internal buffer for events
     *
     * @return event buffer
     */
    public IEventBuffer getEventBuffer();
}
