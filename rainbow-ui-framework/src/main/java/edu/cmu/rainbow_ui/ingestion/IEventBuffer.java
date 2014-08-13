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
import org.sa.rainbow.core.event.IRainbowMessage;

/**
 * Describes Rainbow UI Framework Internal Event Buffer interface, defining data
 * access methods.
 * 
 * <p>
 * Internal Event Buffer acts as a queue with two data access methods - push new
 * event to the queue and pop all events out of it.
 * </p>
 * 
 * <p>
 * For the performance considerations implementations should not allow direct
 * atomic access to internal data structures. The usage assumption is that
 * writes appears more often than reads.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public interface IEventBuffer {

    /**
     * Add all events from the buffer to the specified collection and empty the
     * buffer
     * 
     * @param collection collection to add to
     * @return the number of elements drained
     */
    public int drainToCollection(Collection<IRainbowMessage> collection);

    /**
     * Add new event to the buffer if the buffer is active.
     * 
     * @param event Rainbow message for the event
     */
    public void add(IRainbowMessage event);
    
    /**
     * Clear the event buffer.
     */
    public void clear();

    /**
     * Activate the event buffer. After activation the event buffer accepts new
     * events.
     */
    public void activate();

    /**
     * Deactivate the event buffer. After deactivation the event buffer doesn't
     * add new events.
     */
    public void deactivate();

    /**
     * Check whether the buffer is active.
     * 
     * @return active state
     */
    public boolean isActive();

}
