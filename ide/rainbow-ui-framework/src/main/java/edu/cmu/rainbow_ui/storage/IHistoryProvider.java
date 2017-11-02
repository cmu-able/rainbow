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

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.rainbow_ui.display.config.ViewConfiguration;

/**
 * Describes History Provider interface and methods which can be realized by it
 *
 * <p>
 * History Provider defines the methods for getting the information about events
 * and models
 * </p>
 *
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public interface IHistoryProvider {

    /**
     * Retrieves model from Snapshot table
     *
     * @param time stands for a certain snapshot time for which it is requested
     * @return a model instance
     */
    public IModelInstance<?> getModelState(Date time);

    /**
     * Get events except model updates between the start and end times
     *
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getEventRange(Date startTime, Date endTime);

    /**
     * Get events of certain type except model updates between the start and end
     * times
     *
     * @param channel for event type
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime);

    /**
     * Get model update events between the start and end times
     *
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getModelEventRange(Date startTime, Date endTime);

    /**
     * Returns an event from Events table for a certain time
     *
     * @param date for a time of an event
     * @return message which contains an event for predefined time
     */
    public IRainbowMessage getEvent(Date date);

    /**
     * Set the session to be used
     *
     * @param session a string that uniquely identifies a session
     */
    public void setSession(String session);

    /**
     * Get list of sessions which already exist
     *
     * @return list of existing sessions
     */
    public ArrayList<String> getSessionList();
    
    /**
     * Get the name of the session used to read data by a certain DBC instance.
     *
     * @return the name of the session that is open to read
     */
    public String getReadSession();
    
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
     * Gets a specified number of events before a historical point in time
     * 
     * @param endTime, the latest event to be returned
     * @param numEvents, the number of events to be returned
     * @return a list of events
     */
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime, int numEvents);

    /**
     * Close the connection to the database. The history provider should not be used after this 
     * method has been called.
     */
    public void closeDatabaseConnection();
}
