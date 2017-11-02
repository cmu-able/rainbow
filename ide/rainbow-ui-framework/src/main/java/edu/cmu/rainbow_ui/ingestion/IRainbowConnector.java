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

import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

/**
 * Describes a Rainbow UI Framework interface for Rainbow system connectors.
 * 
 * <p>
 * The Attach() method is called after Event Listener creation and starts
 * listening to events on the Rainbow event bus and delegating their processing
 * to event handlers. The Detach() methods stops listening to events and
 * terminates connection to Rainbow event bus.
 * </p>
 * 
 * <p>
 * getRemoteModel method is used to get the model instance with the given name
 * from the Rainbow system.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public interface IRainbowConnector {

    /**
     * Connect to the event bus and start listening to events.
     * @throws java.io.IOException throws this exception when the attachment cannot be instantiated.
     */
    public void attachEventListeners() throws IOException;

    /**
     * Disconnect from the event bus.
     */
    public void detachEventListeners();

    /**
     * Obtain a model from the Rainbow system.
     * 
     * @param modelName name of the model
     * @return Model instance
     * @throws java.io.IOException
     * @throws edu.cmu.cs.able.eseb.participant.ParticipantException
     */
    public IModelInstance<?> getRemoteModel(String modelName)
            throws IOException, ParticipantException;
}
