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

import edu.cmu.rainbow_ui.storage.IDatabaseConnector;
import edu.cmu.rainbow_ui.storage.IHistoryProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Mock history provider. Used for testing.
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class MockHistoryProvider implements IHistoryProvider{
    private final IDatabaseConnector dbc;
    
    public MockHistoryProvider() {
        this.dbc = new MockDatabaseConnector();
    }

    @Override
    public IModelInstance<?> getModelState(Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IRainbowMessage> getEventRange(Date startTime, Date endTime) {
        return dbc.getEventRange(startTime, endTime);
    }

    @Override
    public List<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IRainbowMessage> getModelEventRange(Date startTime, Date endTime) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IRainbowMessage getEvent(Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSession(String session) {
        // Do nothing
    }

    @Override
    public ArrayList<String> getSessionList() {
        return null;
    }

    @Override
    public String getReadSession() {
	// TODO Auto-generated method stub
	return null;
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
    public void closeDatabaseConnection() {
        dbc.closeConnection();
    }
}
