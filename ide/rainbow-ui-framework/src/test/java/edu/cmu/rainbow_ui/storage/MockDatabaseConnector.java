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

import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;

/**
 * Mock database connector.
 *
 * <p>
 * Provides plausible data to the users of Database Connector. For testing purposes.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class MockDatabaseConnector implements IDatabaseConnector {

    @Override
    public void writeEvent(String channel, IRainbowMessage event, Date timestamp) {
        // Do nothing
    }

    @Override
    public void writeSnapshot(IModelInstance<?> snapshot, Date timestamp) {
        // Do nothing
    }

    @Override
    public ImmutablePair<Date, IModelInstance<?>> getLatestSnapshot(Date time) {
        StandaloneResource resource = null;
        ImmutablePair<Date, IModelInstance<?>> snapshot = null;
        try {
            resource = StandaloneResourceProvider.instance()
                    .acmeResourceForString("ZNewsSys.acme");
            AcmeSystem sys = resource.getModel().getSystems().iterator().next();
            IModelInstance<IAcmeSystem> model = new AcmeInternalModelInstance(
                    sys, "Acme");
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            IModelInstance<?> newmodel = (IModelInstance<IAcmeSystem>) model;
            snapshot = new ImmutablePair<Date, IModelInstance<?>>(date,
                    newmodel);
        } catch (IOException | ParsingFailureException ex) {
            Logger.getLogger(MockDatabaseConnector.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return snapshot;
    }

    @Override
    public void writeModelUpdateEvent(IRainbowMessage event, Date timestamp) {
        // Do nothing
    }

    @Override
    public List<IRainbowMessage> getEventRange(Date startTime, Date endTime) {
        Float expRes = (float) 99.9;
        IRainbowMessage event1 = new RainbowESEBMessage();
        try {
            event1.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                    AcmeModelEventType.SET_PROPERTY_VALUE.toString());
            event1.setProperty(AcmeModelOperation.PROPERTY_PROP,
                    "ZNewsSys.Server0.load");
            event1.setProperty(AcmeModelOperation.VALUE_PROP, "1");
            event1.setProperty(ESEBConstants.MSG_SENT,
                    System.currentTimeMillis() + "");
        } catch (RainbowException e) {
            Logger.getLogger(MockDatabaseConnector.class.getName()).log(
                    Level.SEVERE, null, e);
        }
        Date end = new Date();
        IRainbowMessage event2 = new RainbowESEBMessage();
        List<IRainbowMessage> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        return events;
    }

    @Override
    public List<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime) {
        IRainbowMessage event1 = new RainbowESEBMessage();
        IRainbowMessage event2 = new RainbowESEBMessage();
        List<IRainbowMessage> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        return events;
    }

    @Override
    public List<IRainbowMessage> getModelEventRange(Date startTime, Date endTime) {
        Float expRes = (float) 99.9;
        IRainbowMessage event1 = new RainbowESEBMessage();
        IRainbowMessage event2 = new RainbowESEBMessage();
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
        List<IRainbowMessage> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        return events;
    }

    @Override
    public IRainbowMessage getEvent(Date time) {
        IRainbowMessage event = new RainbowESEBMessage();
        try {
            event.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                    AcmeModelEventType.SET_PROPERTY_VALUE.toString());
            event.setProperty(AcmeModelOperation.PROPERTY_PROP,
                    "ZNewsSys.Server0.load");

        } catch (RainbowException ex) {
            Logger.getLogger(MockDatabaseConnector.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return event;
    }

    @Override
    public void useSession(String name) {
        // Do nothing
    }

    @Override
    public ArrayList<String> getSessionList() {
        ArrayList<String> sessions = new ArrayList<>();
        return sessions;
    }

    @Override
    public void createSession(String name) {
        // Do nothing
    }

    @Override
    public String getWriteSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getReadSession() {
        return "mockSession";
    }

    @Override
    public void closeWriteSession() {
        // Do nothing
    }

    @Override
    public ArrayList<String> getConfigurationList(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLatestConfigurationName(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getConfiguration(String type, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeConfiguration(String config, String type, String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Date getStartDate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean currentSessionIsWriteSession() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Date getMaxDate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime,
            int numEvents) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void closeConnection() {
        // Do nothing
    }

}
