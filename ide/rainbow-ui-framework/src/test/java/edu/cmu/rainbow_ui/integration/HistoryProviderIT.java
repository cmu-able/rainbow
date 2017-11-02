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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import edu.cmu.rainbow_ui.storage.HistoryProvider;
import java.util.Calendar;

/**
 * Integration test for History Provider and its communication with History Database through
 * Database Connector
 *
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public class HistoryProviderIT {

    private static HistoryProvider history;
    private static DatabaseConnector databaseconn;
    private final String SESSION_NAME = "HistoryProviderTest";

    public HistoryProviderIT() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        SystemConfiguration c = new SystemConfiguration("src/test/resources/system_default.properties");
        databaseconn = new DatabaseConnector(c);
        history = new HistoryProvider(databaseconn);
    }

    @Test
    public void getModelStateTest() throws ParsingFailureException, IOException {
        databaseconn.createSession("ModelStateTest");
        databaseconn.useSession("ModelStateTest");
        try {
            String columns[] = {};
            IRainbowMessage model = new RainbowESEBMessage();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, -3);
            Date date = cal.getTime();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            Calendar cal3 = Calendar.getInstance();
            cal3.add(Calendar.SECOND, 2);
            Date date3 = cal3.getTime();
            StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString("ZNewsSys.acme");
            AcmeSystem sys = resource.getModel().getSystems().iterator().next();
            IModelInstance<IAcmeSystem> snapshot = new AcmeInternalModelInstance(sys, "Acme");
            databaseconn.writeSnapshot(snapshot, date);
            assertNotNull(history.getModelState(date3));
        } finally {
            databaseconn.dropKeyspace("ModelStateTest");
        }
    }

    @Test
    public void getEventRangeTest() {
        databaseconn.createSession("EventRangeTest");
        databaseconn.useSession("EventRangeTest");
        try {
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            databaseconn.writeEvent("test2", event, date1);
            databaseconn.writeEvent("test2", event, date2);
            ArrayList<IRainbowMessage> events = history.getEventRange(date1, date2);
            assertEquals(2, events.size());
        } finally {
            databaseconn.dropKeyspace("EventRangeTest");
        }
    }

    @Test
    public void getEventRangeByTypeTest() {
        databaseconn.createSession("EventRangeByTypeTest");
        databaseconn.useSession("EventRangeByTypeTest");
        try {
            IRainbowMessage event = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            Calendar cal3 = Calendar.getInstance();
            cal3.add(Calendar.SECOND, 2);
            Date date3 = cal3.getTime();
            databaseconn.writeEvent("A", event, date1);
            databaseconn.writeEvent("B", event, date2);
            databaseconn.writeEvent("B", event, date3);
            databaseconn.writeEvent("A", event, date2);
            ArrayList<IRainbowMessage> bytimestamp = history.getEventRangeByType("B", date1, date2);
            assertEquals(1, bytimestamp.size());
            ArrayList<IRainbowMessage> bytype = history.getEventRangeByType("A", date1, date2);
            assertEquals(2, bytype.size());
        } finally {
            databaseconn.dropKeyspace("EventRangeByTypeTest");
        }
    }

    @Test
    public void getModelEventRangeTest() {
        databaseconn.createSession("ModelEventRangeTest");
        databaseconn.useSession("ModelEventRangeTest");
        try {
            IRainbowMessage model = new RainbowESEBMessage();
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.SECOND, -1);
            Date date1 = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.SECOND, 1);
            Date date2 = cal2.getTime();
            databaseconn.writeModelUpdateEvent(model, date1);
            databaseconn.writeModelUpdateEvent(model, date2);
            ArrayList<IRainbowMessage> models = history.getModelEventRange(date1, date2);
            assertEquals(2, models.size());
        } finally {
            databaseconn.dropKeyspace("ModelEventRangeTest");
        }
    }
}
