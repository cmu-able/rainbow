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

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.ingestion.AcmeEventDeserializer;
import edu.cmu.rainbow_ui.ingestion.RainbowDeserializationException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.apache.commons.lang3.tuple.Pair;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 *
 *
 * <p>
 * Invokes data requested by System View Provider from the database using Database Connector.
 * Returns events and model state of the system.
 * </p>
 *
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public class HistoryProvider implements IHistoryProvider {

    private final IDatabaseConnector databaseconn;
    private final AcmeEventDeserializer deserializer = new AcmeEventDeserializer();
    private static final Object copyLock = new Object();

    public HistoryProvider(IDatabaseConnector databaseCon) {
        this.databaseconn = databaseCon;
    }

    public HistoryProvider(ISystemViewProvider acmeSystemViewProvider,
            ISystemConfiguration systemConfig) {
        this.databaseconn = new DatabaseConnector(systemConfig);
    }

    /**
     * Returns a snapshot for a certain time
     *
     * @param time is a time for which snapshot is requested
     * @return IModelInstance<?> object with latest snapshot and all model updates which happened
     * after it
     */
    @Override
    public IModelInstance<?> getModelState(Date time) {
        Logger.getLogger(HistoryProvider.class.getName()).log(
                Level.INFO, "Model Requested for time: {0}", time.toString());
        Pair<Date, IModelInstance<?>> modelraw = databaseconn
                .getLatestSnapshot(time);
        Logger.getLogger(HistoryProvider.class.getName()).log(
                Level.INFO, "Got model for time: {0}", time.toString());
        IModelInstance<IAcmeSystem> model = null;

        if (modelraw != null) {
            model = (IModelInstance<IAcmeSystem>) modelraw
                    .getValue();
            ArrayList<IRainbowMessage> events = (ArrayList<IRainbowMessage>) databaseconn
                    .getModelEventRange(modelraw.getKey(), time);
            Logger.getLogger(HistoryProvider.class.getName()).log(
                    Level.INFO, "Got {0} events to apply to the model", events.size());
            for (IRainbowMessage event : events) {
                try {
                    IAcmeCommand<?> command = deserializer.deserialize(event, model.getModelInstance());
                    synchronized (copyLock) {
                        command.execute();
                    }
                }catch (IllegalStateException | AcmeException | RainbowDeserializationException ex) {
                    
                    Logger.getLogger(HistoryProvider.class.getName()).log(
                            Level.SEVERE, null, ex);

                }
            }
        }
        return model;
    }

    /**
     * Returns a set of events for a certain time range
     *
     * @param startTime which is the start time for a time range
     * @param endTime which is the end time for a time range
     * @return arraylist of events for a certain time range
     */
    @Override
    public ArrayList<IRainbowMessage> getEventRange(Date startTime, Date endTime) {
        ArrayList<IRainbowMessage> model;
        model = (ArrayList<IRainbowMessage>) databaseconn.getEventRange(
                startTime, endTime);
        return model;
    }

    /**
     * Returns a set of events for a certain time range by type
     *
     * @param channel for event type
     * @param startTime which is the start time for a time range
     * @param endTime which is the end time for a time range
     * @return arraylist of events for a certain time range
     */
    @Override
    public ArrayList<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime) {
        ArrayList<IRainbowMessage> model;
        model = (ArrayList<IRainbowMessage>) databaseconn.getEventRangeByType(
                channel, startTime, endTime);
        return model;
    }

    /**
     * Returns a set of model update events for a certain time range
     *
     * @param startTime which is the start time for a time range
     * @param endTime which is the end time for a time range
     * @return arraylist of events for a certain time range
     */
    @Override
    public ArrayList<IRainbowMessage> getModelEventRange(Date startTime,
            Date endTime) {
        ArrayList<IRainbowMessage> model;
        model = (ArrayList<IRainbowMessage>) databaseconn.getModelEventRange(
                startTime, endTime);
        return model;
    }

    /**
     * Returns an event for a certain time
     *
     * @param time for a time of an event
     * @return message which contains an event for predefined time
     */
    @Override
    public IRainbowMessage getEvent(Date time) {
        IRainbowMessage event;
        event = databaseconn.getEvent(time);
        return event;
    }

    /**
     * Set a usage of an existing session
     *
     * @param name a session name
     */
    @Override
    public void setSession(String name) {
        databaseconn.useSession(name);
    }

    /**
     * Get list of sessions which already exist
     *
     * @return list of sessions
     */
    @Override
    public ArrayList<String> getSessionList() {
        return databaseconn.getSessionList();
    }

    /**
     * Get the name of the session used to read data by a certain DBC instance.
     *
     * @return the name of the session that is open to read
     */
    public String getReadSession() {
        return databaseconn.getReadSession();
    }

    @Override
    public Date getStartDate() {
        return databaseconn.getStartDate();
    }

    @Override
    public boolean currentSessionIsWriteSession() {
        return databaseconn.currentSessionIsWriteSession();
    }

    @Override
    public Date getMaxDate() {
        return this.databaseconn.getMaxDate();
    }

    @Override
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime,
            int numEvents) {
        return databaseconn.getNumberOfEventsBefore(endTime, numEvents);
    }

    @Override
    public void closeDatabaseConnection() {
        databaseconn.closeConnection();
    }
}
