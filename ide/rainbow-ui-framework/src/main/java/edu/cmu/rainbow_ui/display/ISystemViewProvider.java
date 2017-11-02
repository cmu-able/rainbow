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
package edu.cmu.rainbow_ui.display;

import edu.cmu.cs.able.typelib.type.DataValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Defines the interface for the system view provider
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public interface ISystemViewProvider {

    /**
     * Sets the system to use the current view.
     */
    public void setUseCurrent();

    /**
     * Sets the system to use a historical view
     *
     * @param time the time at which the view is desired
     * @throws edu.cmu.rainbow_ui.display.SystemViewProviderException
     */
    public void setUseHistorical(Date time) throws SystemViewProviderException;
    
    /**
     * Get current historical time.
     * 
     * @return time or null if current view is not historical
     */
    public Date getHistoricalTime();

    /**
     * Returns whether the system should use the current view or a historical
     * one
     *
     * @return if the value returned is true, the system is using a current view
     * otherwise it is using a historical one
     */
    public boolean isCurrent();

    /**
     * Provides access to the model of the system under control a historical
     * point as stored in the history database
     *
     * @return returns the system view provider's internal model
     */
    public IModelInstance<?> getView();

    /**
     * Provides access to the model of the system under control a historical
     * point as stored in the history database
     *
     * @param start the the starting time from which events should be gotten
     * @param end the ending point from which events should be gotten
     */
    public void getHistoricalEventRange(Date start, Date end);

    /**
     * Provides access to the model of the system under control a historical
     * point as stored in the history database
     *
     * @param start the the starting time from which events should be gotten
     * @param end the ending point from which events should be gotten
     */
    public void getHistoricalModelEventRange(Date start, Date end);

    /**
     * Provides access to the model of the system under control a historical
     * point as stored in the history database
     *
     * @param start the the starting time from which events should be gotten
     * @param end the ending point from which events should be gotten
     * @param channel the channel on which events should be gotten
     */
    public void getHistoricalEventRangeByType(String channel, Date start,
            Date end);

    /**
     * Allows the history provider to pass the model to the system view provider
     *
     * @param view the model which is passed back to the system view provider
     */
    public void provideHistoricalView(IModelInstance<?> view);

    /**
     * Allows the history provider to pass an event back
     *
     * @param event the event which is passed back to the system view provider
     */
    public void provideHistoricalEvent(IRainbowMessage event);

    /**
     * Allows the history provider to pass an event range
     *
     * @param event the event which is passed back to the system view provider
     */
    public void provideHistoricalEventRange(List<IRainbowMessage> event);

    /**
     * Update the values of the widgets being displayed and call the UI to push
     * those updates to the screen
     */
    public void update();

    /**
     * Get the latest events added to the internal event store
     *
     * @return list of events recently added
     */
    public List<IRainbowMessage> getNewEvents();

    /**
     * Get the value of an element based on its string mapping
     * 
     * @return mapping a string that uniquely identifies an element
     */
    public DataValue getValue(String mapping) throws Exception;
    
    /**
    * Get the list of already existing sessions
     *
     * @return list of existing sessions
     */
    public ArrayList<String> getSessionList();
    
    /**
     * Set the session to be used
     *
     * @param session a string that uniquely identifies a session
     */
    public void setSession(String session);
    
    /**
     * Get the number of new events that have occurred since the last
     * {@link #getNewEvents()} call
     *
     * @return a integer number of events that have occurred
     */
    public int getNewEventsCount();
    
    /**
     * Gets the starting date from the current write session
     * 
     * @return the date of the first snapshot.
     */
    public Date getStartDate();
    
    /**
     * Determine whether the current session is the current write session
     * 
     * @return true if the current session is equal to the write session
     */
    public boolean currentSessionIsWriteSession();

    /**
     * Gets the date from the latest write session
     * 
     * @return the date of the latest event
     */
    public Date getMaxDate();
    
    /**
     * Gets the current session
     * 
     * @return the current session
     */
    public String getSession();
    
    /**
     * Gets the event list for the historical point in time
     * 
     * @return a list of the events at the historical point in time
     */
    public List<IRainbowMessage> getHistoricalEvents();
    
    /**
     * Gets a specified number of events before a historical point in time
     * 
     * @param endTime, the latest event to be returned
     * @param numEvents, the number of events to be returned
     * @return a list of events
     */
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime, int numEvents);
    
    /**
     * Stops the system view provider. Releases acquired resources and stops any update tasks.
     * System view provider should not be used after this method call.
     */
    public void stop();
}
