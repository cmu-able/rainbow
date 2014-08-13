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

package edu.cmu.rainbow_ui.integration;

import java.util.Date;

import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;

import edu.cmu.rainbow_ui.ingestion.EventProcessingException;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;

/**
 * Provides common functions needed for various integration tests
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 * 
 */
public class IntegrationTestHelper {
    private final static String MODEL_CHANGE_CHANNEL = "MODEL_CHANGE";
    private final static String EVENTS_CHANNEL = "MODEL_DS";

    /**
     * Send model change events for RA processing with given timestamps.
     * 
     * @param eventTimes an array to store actual processing time
     * @throws EventProcessingException
     * @throws RainbowException
     */
    public static void processModelEvents(Date[] eventTimes,
            IRuntimeAggregator<?> runtimeAgg, IDatabaseConnector databaseCon)
            throws EventProcessingException, RainbowException {
        for (int i = 0; i < eventTimes.length; i++) {
            Date date = eventTimes[i];
            // Create model event
            RainbowESEBMessage modelEvent = new RainbowESEBMessage();
            modelEvent.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                    AcmeModelEventType.SET_PROPERTY_VALUE.toString());
            modelEvent.setProperty(AcmeModelOperation.PROPERTY_PROP,
                    "ZNewsSys.Server0.load");
            modelEvent.setProperty(AcmeModelOperation.VALUE_PROP, (float) i
                    + "");
            /* Rainbow message uses time format in milliseconds */
            modelEvent.setProperty(ESEBConstants.MSG_SENT, date.getTime() + "");
            runtimeAgg.processEvent(MODEL_CHANGE_CHANNEL, modelEvent);
        }
    }
    
    /**
     * Send model change events for RA processing with given timestamps.
     * 
     * @param eventTimes an array to store actual processing time
     * @throws EventProcessingException
     * @throws RainbowException
     */
    public static void processStreamingEvents(Date[] eventTimes,
            IRuntimeAggregator<?> runtimeAgg, IDatabaseConnector databaseCon)
            throws EventProcessingException, RainbowException {
        for (int i = 0; i < eventTimes.length; i++) {
            Date date = eventTimes[i];
            // Create model event
            RainbowESEBMessage modelEvent = new RainbowESEBMessage();
            modelEvent.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                    AcmeModelEventType.SET_PROPERTY_VALUE.toString());
            modelEvent.setProperty(AcmeModelOperation.PROPERTY_PROP,
                    "ZNewsSys.Server0.load");
            modelEvent.setProperty(AcmeModelOperation.VALUE_PROP, (float) i
                    + "");
            /* Rainbow message uses time format in milliseconds */
            modelEvent.setProperty(ESEBConstants.MSG_SENT, date.getTime() + "");
            runtimeAgg.processEvent(EVENTS_CHANNEL, modelEvent);
        }
    }

    /**
     * Send events to the DBC directly with given timestamp values.
     * 
     * @param eventTimes the array to store actual sent time
     */
    public static void sendEvents(Date[] eventTimes, IDatabaseConnector databaseCon) {
        for (int i = 0; i < eventTimes.length; i++) {
            Date date = eventTimes[i];
            // Create model event
            RainbowESEBMessage modelEvent = new RainbowESEBMessage();
            modelEvent.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                    AcmeModelEventType.SET_PROPERTY_VALUE.toString());
            modelEvent.setProperty(AcmeModelOperation.PROPERTY_PROP,
                    "ZNewsSys.Server0.load");
            modelEvent.setProperty(AcmeModelOperation.VALUE_PROP, (float) i
                    + "");
            /* Rainbow message uses time format in milliseconds */
            modelEvent.setProperty(ESEBConstants.MSG_SENT, date.getTime() + "");
            databaseCon.writeEvent(EVENTS_CHANNEL, modelEvent, date);
        }
    }
}
