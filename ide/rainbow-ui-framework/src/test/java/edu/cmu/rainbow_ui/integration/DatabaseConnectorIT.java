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

import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
import edu.cmu.rainbow_ui.storage.MockDatabaseConnector;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;

/**
 * Unit tests for Database Connector
 *
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 *
 */
public class DatabaseConnectorIT {

    private static DatabaseConnector databaseconn;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SystemConfiguration c = new SystemConfiguration(
                "src/test/resources/system_default.properties");
        databaseconn = new DatabaseConnector(c);
    }

    /**
     * Test for createSession method of Database Connector.
     */
    @Test
    public void testCreateSession() {
        databaseconn.createSession("test");
        try {
            assertTrue(databaseconn.getSessionList().contains("test"));
        } finally {
            databaseconn.dropKeyspace("test");
        }
    }

    /**
     * Test for useSession method of Database Connector.
     */
    @Test
    public void testUseSession() {
        databaseconn.createSession("usetest1");
        try {
            databaseconn.useSession("usetest1");
            String session = databaseconn.getReadSession();
            assertEquals(session, "usetest1");
        } finally {
            databaseconn.dropKeyspace("usetest1");
        }
    }

    /**
     * Test for getReadSession method of Database Connector.
     */
    @Test
    public void testGetCurrentSession() {
        databaseconn.createSession("current");
        try {
            databaseconn.useSession("current");
            assertEquals("current", databaseconn.getReadSession());
        } finally {
            databaseconn.dropKeyspace("current");
        }
    }

    /**
     * Test for getSessionList method of Database Connector.
     */
    @Test
    public void testGetSessionList() {
        ArrayList<String> sessionlist = databaseconn.getSessionList();
        assertTrue(sessionlist.size() > 0);
    }

    /**
     * Test for createEventsTable method of Database Connector.
     */
    @Test
    public void testCreateEventsTable() {
        databaseconn.createSession("eventtable");
        try {
            databaseconn.useSession("eventtable");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeEvent("channel", event, date);
        } finally {
            databaseconn.dropKeyspace("eventtable");
        }
    }

    /**
     * Test for createModelChangeTable method of Database Connector.
     */
    @Test
    public void testCreateModelChangeTable() {
        databaseconn.createSession("model");
        try {
            databaseconn.useSession("model");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeEvent("channel", event, date);
        } finally {
            databaseconn.dropKeyspace("model");
        }
    }

    /**
     * Test for createSnapshotTable method of Database Connector.
     */
    @Test
    public void testCreateSnapshotTable() {
        databaseconn.createSession("snapshot");
        try {
            databaseconn.useSession("snapshot");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeEvent("channel", event, date);
        } finally {
            databaseconn.dropKeyspace("snapshot");
        }
    }

    /**
     * Test for writeSnapshot method of Database Connector.
     *
     * @throws IOException
     * @throws ParsingFailureException
     */
    @Test
    public void testWriteSnapshot() throws ParsingFailureException, IOException {
        databaseconn.createSession("snapshot");
        try {
            databaseconn.useSession("snapshot");
            StandaloneResource resource = StandaloneResourceProvider.instance()
                    .acmeResourceForString("ZNewsSys.acme");
            AcmeSystem sys = resource.getModel().getSystems().iterator().next();
            IModelInstance<IAcmeSystem> model = new AcmeInternalModelInstance(sys,
                    "Acme");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -8);
            Date date = cal.getTime();
            databaseconn.writeSnapshot(model, date);
            assertNotNull(databaseconn.getLatestSnapshot(date));
        } finally {
            databaseconn.dropKeyspace("snapshot");
        }
    }

    /**
     * Test for getLatestSnapshot method of Database Connector.
     *
     * @throws IOException
     * @throws ParsingFailureException
     */
    @Test
    public void testGetLatestSnapshot() throws ParsingFailureException,
            IOException {
        databaseconn.createSession("getsnapshot");
        try {
            databaseconn.useSession("getsnapshot");
            StandaloneResource resource = StandaloneResourceProvider.instance()
                    .acmeResourceForString("ZNewsSys.acme");
            AcmeSystem sys = resource.getModel().getSystems().iterator().next();
            IModelInstance<IAcmeSystem> model = new AcmeInternalModelInstance(sys,
                    "Acme");
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeSnapshot(model, date);
            assertNotNull(databaseconn.getLatestSnapshot(date));
        } finally {
            databaseconn.dropKeyspace("getsnapshot");
        }
    }

    /**
     * Test for writeEvent method of Database Connector.
     */
    @Test
    public void testWriteEvent() {
        databaseconn.createSession("event");
        try {
            databaseconn.useSession("event");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeEvent("channel", event, date);
            assertNotNull(databaseconn.getEvent(date));
        } finally {
            databaseconn.dropKeyspace("event");
        }
    }

    /**
     * Test writeModelUpdateEvent method of Database Connector.
     */
    @Test
    public void testWriteModelUpdateEvent() {
        databaseconn.createSession("modelevent");
        try {
            databaseconn.useSession("modelevent");
            IRainbowMessage model = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            databaseconn.writeModelUpdateEvent(model, date1);
            assertNotNull(databaseconn.getModelEventRange(date1, date2));
        } finally {
            databaseconn.dropKeyspace("modelevent");
        }
    }

    /**
     * Test getEventRange method of Database Connector.
     */
    @Test
    public void testGetEventRange() {
        databaseconn.createSession("eventtime");
        try {
            databaseconn.useSession("eventtime");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, 15);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 20);
            Date date2 = cal2.getTime();
            databaseconn.writeEvent("test1", event, date1);
            databaseconn.writeEvent("test1", event, date2);
            ArrayList<IRainbowMessage> events = databaseconn.getEventRange(date1,
                    date2);
            assertEquals(2, events.size());
        } finally {
            //databaseconn.dropKeyspace("eventtime");
        }
    }

    /**
     * Test getEventRangeByType method of Database Connector.
     */
    @Test
    public void testGetEventRangeByType() {
        databaseconn.createSession("eventtype");
        try {
            databaseconn.useSession("eventtype");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            Calendar cal3 = Calendar.getInstance();
            cal3.add(Calendar.SECOND, 2);
            Date date3 = cal2.getTime();
            databaseconn.writeEvent("A", event, date1);
            databaseconn.writeEvent("B", event, date2);
            databaseconn.writeEvent("B", event, date3);
            databaseconn.writeEvent("A", event, date2);
            ArrayList<IRainbowMessage> bytimestamp = databaseconn
                    .getEventRangeByType("B", date1, date2);
            assertEquals(1, bytimestamp.size());
            ArrayList<IRainbowMessage> bytype = databaseconn.getEventRangeByType(
                    "A", date1, date2);
            assertEquals(2, bytype.size());
        } finally {
            databaseconn.dropKeyspace("eventtype");
        }
    }

    /**
     * Test getModelEventRange method of Database Connector.
     */
    @Test
    public void testGetModelEventRange() {
        databaseconn.createSession("modelrange");
        try {
            databaseconn.useSession("modelrange");
            IRainbowMessage model = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            databaseconn.writeModelUpdateEvent(model, date1);
            databaseconn.writeModelUpdateEvent(model, date2);
            ArrayList<IRainbowMessage> models = databaseconn.getModelEventRange(
                    date1, date2);
            assertEquals(2, models.size());
        } finally {
            databaseconn.dropKeyspace("modelrange");
        }
    }

    /**
     * Test getEvent method of Database Connector.
     *
     * @throws RainbowException
     */
    @Test
    public void testGetEvent() throws RainbowException {
        databaseconn.createSession("event");
        try {
            databaseconn.useSession("event");
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            databaseconn.writeEvent("channel", event, date);
            assertNotNull(databaseconn.getEvent(date));
        } finally {
            databaseconn.dropKeyspace("event");
        }
    }

    /**
     * Test dropKeyspace method of Database Connector.
     */
    @Test(expected = com.datastax.driver.core.exceptions.InvalidQueryException.class)
    public void testDropKeyspace() {
        databaseconn.createSession("tobedeleted");
        databaseconn.dropKeyspace("tobedeleted");
        databaseconn.useSession("tobedeleted");
    }

    @Test
    public void testCheckColumn() {
        databaseconn.createSession("column");
        try {
            databaseconn.useSession("column");
            Float expRes = (float) 99.9;
            IRainbowMessage event1 = new RainbowESEBMessage();
            IRainbowMessage event2 = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            try {
                event1.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                        AcmeModelEventType.SET_PROPERTY_VALUE.toString());
                event1.setProperty(AcmeModelOperation.PROPERTY_PROP,
                        "ZNewsSys.Server0.load");
                event1.setProperty(AcmeModelOperation.VALUE_PROP, expRes.toString());
                event1.setProperty(ESEBConstants.MSG_SENT,
                        System.currentTimeMillis() + "");
                event2.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                        AcmeModelEventType.SET_PROPERTY_VALUE.toString());
                event2.setProperty(AcmeModelOperation.PROPERTY_PROP,
                        "ZNewsSys.Server0.load");
                event2.setProperty(AcmeModelOperation.VALUE_PROP, expRes.toString());
                event2.setProperty(ESEBConstants.MSG_SENT,
                        System.currentTimeMillis() + "");
            } catch (RainbowException ex) {
                Logger.getLogger(MockDatabaseConnector.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            databaseconn.writeEvent("test", event1, date);
            databaseconn.writeModelUpdateEvent(event2, date);
            IRainbowMessage test1 = databaseconn.getEvent(date);
            List<IRainbowMessage> test2 = databaseconn.getModelEventRange(date, date);
            assertNotNull(test1);
            assertEquals(1, test2.size());
        } finally {
            databaseconn.dropKeyspace("column");
        }
    }

}
