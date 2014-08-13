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

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.AccessibleApplicationCore;
import edu.cmu.rainbow_ui.ingestion.EventProcessingException;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import edu.cmu.rainbow_ui.storage.HistoryProvider;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;
import edu.cmu.rainbow_ui.storage.IHistoryProvider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

/**
 * Tests functionality related to getting events from Rainbow, storing the
 * events in the database, and retrieving them via the history provider
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class HistoricalPathIT {

    /**
     * Location of system settings file
     */
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_test.properties";
    private static AccessibleApplicationCore mockAppCore;
    private static ISystemConfiguration sysConfig;
    private static IRuntimeAggregator<?> runtimeAgg;
    private static IDatabaseConnector databaseCon;
    private static IHistoryProvider historyProvider;

    /**
     * Set up for all tests
     * 
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        mockAppCore = (AccessibleApplicationCore) AccessibleApplicationCore
                .getInstance();
        /* We do not want Mock Rainbow to interfere with these IT tests */
        mockAppCore.setUseMockRainbow(false);
        mockAppCore.startup(SYSTEM_CONFIG_FILE);
        sysConfig = mockAppCore.getSystemConfiguration();
        runtimeAgg = mockAppCore.getRuntimeAggregator();
        runtimeAgg.start();
    }
    
    /**
     * Each test should has its own DBC. DBC is not thread safe!
     */
    @Before
    public void setUp() {
        databaseCon = new DatabaseConnector(sysConfig);
        historyProvider = new HistoryProvider(databaseCon);
        historyProvider.setSession(mockAppCore.getWriteSession());
    }

    /**
     * Clean up after all all tests
     * 
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        String writeSession = mockAppCore.getWriteSession();
        runtimeAgg.stop();
        ((DatabaseConnector) databaseCon)
                .dropKeyspace(writeSession);
    }

    /**
     * Test storing events
     * 
     * @throws org.sa.rainbow.core.error.RainbowException
     * @throws edu.cmu.rainbow_ui.ingestion.EventProcessingException
     */
    @Test
    public void eventProcessingAndStorage() throws RainbowException,
            EventProcessingException {
        Date[] eventTimes = { new Date(1000), new Date(2000), new Date(3000) };

        IntegrationTestHelper.sendEvents(eventTimes, databaseCon);

        for (int i = 0; i < eventTimes.length; i++) {
            assertEquals((float) i + "", databaseCon.getEvent(eventTimes[i])
                    .getProperty(AcmeModelOperation.VALUE_PROP));
        }
    }

    /**
     * Test storing model update events
     */
    @Test
    public void modelUpdateProcessingAndStorage() {
        Date[] eventTimes = { new Date(11000), new Date(12000),
                new Date(13000), new Date(14000), new Date(15000) };
        IntegrationTestHelper.sendEvents(eventTimes, databaseCon);

        for (int i = 0; i < eventTimes.length; i++) {
            assertEquals((float) i + "", databaseCon.getEvent(eventTimes[i])
                    .getProperty(AcmeModelOperation.VALUE_PROP));
        }
    }

    /**
     * Test getting a snapshot of the model
     * 
     * @throws EventProcessingException
     * @throws RainbowException
     */
    @Test
    public void requestModel() throws EventProcessingException,
            RainbowException {
        Date[] modelEventTimes = { new Date(21000), new Date(22000),
                new Date(23000), new Date(24000), new Date(25000) };
        IntegrationTestHelper.processModelEvents(modelEventTimes, runtimeAgg,
                databaseCon);
        Pair<Date, IModelInstance<?>> snap1 = databaseCon
                .getLatestSnapshot(modelEventTimes[4]);
        Pair<Date, IModelInstance<?>> snap2 = databaseCon
                .getLatestSnapshot(modelEventTimes[4]);

        IModelInstance<?> model1 = historyProvider
                .getModelState(modelEventTimes[4]);
        IModelInstance<?> model2 = historyProvider
                .getModelState(modelEventTimes[4]);

        assertEquals(snap1.getLeft(), snap2.getLeft());

        assertEquals(((IModelInstance<IAcmeSystem>) snap1.getRight())
                .getModelInstance().getComponent("Server0").getProperty("load")
                .getValue(), ((IModelInstance<IAcmeSystem>) snap2.getRight())
                .getModelInstance().getComponent("Server0").getProperty("load")
                .getValue());

        // assertEquals(snap1.getValue(), model1);
        // assertEquals(snap2.getValue(), model2);
    }

    @Test
    public void requestEventsForTimeRange() {
        Date[] eventTimes = { new Date(31000), new Date(32000),
                new Date(33000), new Date(34000), new Date(35000) };
        IntegrationTestHelper.sendEvents(eventTimes, databaseCon);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(eventTimes[0]);
        Date start = cal1.getTime();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(eventTimes[0]);
        Date end = cal2.getTime();

        // Check getEvent
        IRainbowMessage event = historyProvider.getEvent(eventTimes[0]);
        assertEquals((float) 0 + "",
                event.getProperty(AcmeModelOperation.VALUE_PROP));

        event = historyProvider.getEvent(eventTimes[1]);
        assertEquals((float) 1 + "",
                event.getProperty(AcmeModelOperation.VALUE_PROP));

        // Check getEventRange
        ArrayList<IRainbowMessage> events = (ArrayList<IRainbowMessage>) historyProvider
                .getEventRange(start, end);

        assertEquals(1, events.size());

        cal2.setTime(eventTimes[1]);

        end = cal2.getTime();
        events = (ArrayList<IRainbowMessage>) historyProvider.getEventRange(
                start, end);

        assertEquals(2, events.size());

        cal2.setTime(eventTimes[2]);

        end = cal2.getTime();
        events = (ArrayList<IRainbowMessage>) historyProvider.getEventRange(
                start, end);

        assertEquals(3, events.size());

        cal1.setTime(eventTimes[1]);

        start = cal1.getTime();
        events = (ArrayList<IRainbowMessage>) historyProvider.getEventRange(
                start, end);

        assertEquals(2, events.size());
    }

    @Test
    public void requestModelUpdatesBetweenSnapshots() {

    }
}
