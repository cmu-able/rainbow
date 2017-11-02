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

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

/**
 * Unit tests for EventBuffer class of the Rainbow UI Framework.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class EventBufferTest {

    /**
     * Test of drainToCollection method, of class EventBuffer.
     *
     * Tests draining of all events from the buffer to given collection.
     *
     * @throws org.sa.rainbow.core.error.RainbowException
     */
    @Test
    public void testDrainToCollection() throws RainbowException {
        System.out.println("drainToCollection");
        List<IRainbowMessage> collection = new ArrayList<>();
        EventBuffer instance = new EventBuffer(10);
        instance.activate();
        IRainbowMessage event1 = new RainbowESEBMessage();
        IRainbowMessage event2 = new RainbowESEBMessage();
        event1.setProperty(ESEBConstants.MSG_CHANNEL_KEY, "MODEL_DS");
        event2.setProperty(ESEBConstants.MSG_CHANNEL_KEY, "MODEL_US");
        instance.add(event1);
        instance.add(event2);
        int num = instance.drainToCollection(collection);
        assertEquals(2, num);
        IRainbowMessage result1 = collection.get(0);
        IRainbowMessage result2 = collection.get(1);
        assertEquals(event1.toString(), result1.toString());
        assertEquals(event2.toString(), result2.toString());
    }

    /**
     * Test of add method, of class EventBuffer.
     * 
     * <p>
     * Tests addition of new events and addition of events over the buffer capacity.
     * </p>
     *
     * @throws org.sa.rainbow.core.error.RainbowException
     */
    @Test
    public void testAdd() throws RainbowException {
        System.out.println("add");
        IRainbowMessage event1 = new RainbowESEBMessage();
        IRainbowMessage event2 = new RainbowESEBMessage();
        event1.setProperty(ESEBConstants.MSG_CHANNEL_KEY, "MODEL_DS");
        event2.setProperty(ESEBConstants.MSG_CHANNEL_KEY, "MODEL_US");
        EventBuffer instance = new EventBuffer(1);
        instance.activate();
        instance.add(event1);
        /* Second addition should not fail, but the event should not be added */
        instance.add(event2);
        List<IRainbowMessage> collection = new ArrayList<>();
        int num = instance.drainToCollection(collection);
        assertEquals(1, num);
        IRainbowMessage result = collection.get(0);
        assertEquals(event1.toString(), result.toString());

    }

    /**
     * Test of activation, of class EventBuffer.
     *
     * <p>
     * Tests following methods: activate, deactivate, isActive and add.
     * </p>
     *
     * <p>
     * This tests performs activation and deactivation of the event buffer and checks that the event
     * buffer doesn't accept events when it is not active.
     * </p>
     */
    @Test
    public void testActivation() {
        System.out.println("activate");
        EventBuffer instance = new EventBuffer(10);
        assertEquals(false, instance.isActive());
        instance.activate();
        assertEquals(true, instance.isActive());
        instance.add(new RainbowESEBMessage());
        int num = instance.drainToCollection(new ArrayList<IRainbowMessage>());
        assertEquals(1, num);
        instance.deactivate();
        assertEquals(false, instance.isActive());
        instance.add(new RainbowESEBMessage());
        num = instance.drainToCollection(new ArrayList<IRainbowMessage>());
        assertEquals(0, num);
    }
}
