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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Dummy Database Connector.
 *
 * <p>
 * Do nothing. For testing purposes.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class DummyDatabaseConnector implements IDatabaseConnector {

    @Override
    public void writeEvent(String channel, IRainbowMessage event, Date timestamp) {
        // Do nothing
    }

    @Override
    public void writeModelUpdateEvent(IRainbowMessage event, Date timestamp) {
        // Do nothing
    }

    @Override
    public void writeSnapshot(IModelInstance<?> snapshot, Date timestamp) {
        // Do nothing
    }

    @Override
    public IRainbowMessage getEvent(Date time) {
        return null;
    }

    @Override
    public Pair<Date, IModelInstance<?>> getLatestSnapshot(Date time) {
        return null;
    }

    @Override
    public List<IRainbowMessage> getEventRange(Date startTime, Date endTime) {
        return null;
    }

    @Override
    public List<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime) {
        return null;
    }

    @Override
    public List<IRainbowMessage> getModelEventRange(Date startTime, Date endTime) {
        return null;
    }

    @Override
    public void useSession(String name) {
        // Do nothing
    }

    @Override
    public ArrayList<String> getSessionList() {
        return null;
    }

    @Override
    public void createSession(String name) {
        // Do nothing
    }

    @Override
    public String getWriteSession() {
        return null;
    }

    @Override
    public String getReadSession() {
        return null;
    }

    @Override
    public void closeWriteSession() {
        // Do nothing
    }

    @Override
    public ArrayList<String> getConfigurationList(String type) {
        return null;
    }

    @Override
    public String getLatestConfigurationName(String type) {
        return null;
    }

    @Override
    public String getConfiguration(String type, String name) {
        return null;
    }

    @Override
    public void writeConfiguration(String config, String type, String name) {
        // Do nothing        
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
