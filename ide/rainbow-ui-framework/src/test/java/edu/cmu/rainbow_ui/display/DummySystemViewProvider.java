/* The MIT License
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
package edu.cmu.rainbow_ui.display;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.common.DataValueSupport;

/**
 * Dummy system view provider used for testing
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class DummySystemViewProvider implements ISystemViewProvider {

    DataValue value;
    boolean isCurrent;

    /**
     * Constructor for a dummy system view provider
     */
    public DummySystemViewProvider() {
        value = DataValueSupport.pscope.string().make("TEST");
        isCurrent = true;
    }

    @Override
    public void provideHistoricalView(IModelInstance<?> view) {
    }

    @Override
    public void provideHistoricalEvent(IRainbowMessage event) {
    }

    @Override
    public void update() {
    }

    @Override
    public boolean isCurrent() {
        return isCurrent;
    }

    @Override
    public void setUseCurrent() {
    }

    @Override
    public IModelInstance<?> getView() {
        return null;
    }

    @Override
    public void provideHistoricalEventRange(List<IRainbowMessage> event) {

    }

    @Override
    public void setUseHistorical(Date time) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getHistoricalEventRange(Date start, Date end) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getHistoricalEventRangeByType(String channel, Date start,
            Date end) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getHistoricalModelEventRange(Date start, Date end) {
        // TODO Auto-generated method stub

    }

    public DataValue getValue(String mapping) {
        return DataValueSupport.pscope.string().make("TEST");
    }

    @Override
    public List<IRainbowMessage> getNewEvents() {
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
    public int getNewEventsCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public String getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IRainbowMessage> getHistoricalEvents() {
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
    public Date getHistoricalTime() {
        return null;
    }

    @Override
    public void stop() {
        // Do nothing
    }
}