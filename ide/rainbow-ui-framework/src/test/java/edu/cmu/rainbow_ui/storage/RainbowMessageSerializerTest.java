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
package edu.cmu.rainbow_ui.storage;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

/**
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class RainbowMessageSerializerTest {

    public RainbowMessageSerializerTest() {
    }

    /**
     * Test of serialize method, of class RainbowMessageSerializer.
     *
     * @throws org.sa.rainbow.core.error.RainbowException
     */
    @Test
    public void testSerializer() throws RainbowException {
        /* Check serialization */
        System.out.println("serialize");
        IRainbowMessage message = new RainbowESEBMessage();
        message.setProperty(ESEBConstants.MSG_SENT, "1");
        List<Pair<String, String>> expResult = new LinkedList<>();
        expResult.add(new ImmutablePair<>("ESEBmsgsent", "1"));
        List<Pair<String, String>> result = RainbowMessageSerializer.serialize(message);
        assertEquals(expResult, result);
        /* Check deserialization */
        System.out.println("deserialize");
        IRainbowMessage resultMessage = RainbowMessageSerializer.deserialize(result);
        for (String propName : message.getPropertyNames()) {
            assertEquals(message.getProperty(propName), resultMessage.getProperty(propName));
        }
        
    }

}
