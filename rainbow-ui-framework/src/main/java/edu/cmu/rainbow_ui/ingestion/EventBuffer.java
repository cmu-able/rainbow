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

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.sa.rainbow.core.event.IRainbowMessage;

/**
 * Implementation of the EventBuffer for the Rainbow UI Framework.
 *
 * <p>
 * The event buffer has a fixed capacity. On attempt to add an event to the full queue a warning
 * will be logged. On each buffer drain the capacity of the buffer resets to zero.
 * </p>
 * <p>
 * The event buffer access is blocking, i.e. no two consumer may use it for write or read
 * simultaneously. This is done to work in the presence of multiple threads.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class EventBuffer implements IEventBuffer {

    /**
     * The blocking queue for event storage
     */
    private final BlockingQueue<IRainbowMessage> queue;

    /**
     * Active state
     */
    private boolean isActive;

    /**
     * Create new event buffer with the fixed size
     *
     * @param size buffer size
     */
    public EventBuffer(int size) {
        queue = new LinkedBlockingQueue<>(size);
        isActive = false;
    }

    @Override
    public int drainToCollection(Collection<IRainbowMessage> collection) {
        return queue.drainTo(collection);
    }

    @Override
    public void add(IRainbowMessage event) {
        if (isActive) {
            if (!queue.offer(event)) {
                Logger.getLogger(EventBuffer.class.getName()).warning(
                        "Cannot add an event. The buffer is full.");
            }
        }
    }
    
    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public void activate() {
        isActive = true;
    }

    @Override
    public void deactivate() {
        isActive = false;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

}
