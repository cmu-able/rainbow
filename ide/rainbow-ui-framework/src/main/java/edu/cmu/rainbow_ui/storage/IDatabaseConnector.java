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
 * Describes Database Connector interface and methods which can be realized by
 * it
 *
 * <p>
 * Database Connector defines addition and search of new Events and Snapshots
 * </p>
 *
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public interface IDatabaseConnector {

    /**
     * Adds new Event in Events table
     *
     * @param channel which defines the event type
     * @param event is an IRainbowMessage object coming from one of Rainbow
     * buses
     * @param timestamp timestamp of the event
     */
    public void writeEvent(String channel, IRainbowMessage event, Date timestamp);

    /**
     * Adds new Model Update Event in the corresponding table
     *
     * @param event is an IRainbowMessage object coming from one of Rainbow
     * buses
     * @param timestamp timestamp of the event
     */
    public void writeModelUpdateEvent(IRainbowMessage event, Date timestamp);

    /**
     * Adds new Snapshot in Model table
     *
     * @param snapshot is the state of the system under control at some point of
     * time
     * @param timestamp timestamp of the snapshot
     */
    public void writeSnapshot(IModelInstance<?> snapshot, Date timestamp);

    /**
     * Returns an Event from Events table
     *
     * @param time stands for a time stamp for which event is requested
     * @return Event in IRainbowMessage format
     */
    public IRainbowMessage getEvent(Date time);

    /**
     * Returns a Snapshot from Model table
     *
     * @param time stands for a time stamp for which event is requested
     * @return Snapshot in IModelInstance format
     */
    public Pair<Date, IModelInstance<?>> getLatestSnapshot(Date time);

    /**
     * Returns a set of events from Events table
     *
     * @param startTime stands for the earliest event time stamp
     * @param endTime stands for the latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getEventRange(Date startTime,
            Date endTime);

    /**
     * Returns a set of events of certain type from Events table
     *
     * @param channel for event type
     * @param startTime stands for the earliest event time stamp
     * @param endTime stands for the latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getEventRangeByType(String channel, Date startTime,
            Date endTime);

    /**
     * Returns a set of Model Update Events from corresponding table
     *
     * @param startTime stands for the earliest event time stamp
     * @param endTime stands for the latest event time stamp
     * @return set of IRainbowMessages for time stamp range
     */
    public List<IRainbowMessage> getModelEventRange(Date startTime,
            Date endTime);

    /**
     * Switch to a different session. Connect to a corresponding database keyspace and update event
     * serialization
     *
     * @param name name of the session
     */
    public void useSession(String name);

    /**
     * Returns list of existing sessions
     *
     * @return list of strings for sessions
     */
    public ArrayList<String> getSessionList();

    /**
     * Creates a new session keyspace if it does not exist
     *
     * @param name name of the keyspace containing only alphanumeric characters
     * and starting with an alpha character
     */
    void createSession(String name);

    /**
     * Get the name of the actively writing session. This session is the same
     * for all DBC instances.
     *
     * @return the name of the session that is active to write
     */
    public String getWriteSession();

    /**
     * Get the name of the session used to read data by a certain DBC instance.
     *
     * @return the name of the session that is open to read
     */
    public String getReadSession();

    /**
     * Close currently actively writing session
     *
     */
    void closeWriteSession();

    /**
     * Get a list of the configurations by their type
     * 
     * @param type the type of the configuration
     * @return an arraylist of strings that hold the name of the configurations
     */
    public ArrayList<String> getConfigurationList(String type);

    /**
     * Get the latest configuration of the given type
     * 
     * @param type the type of the configuration
     * @return a string that holds the name of the configuration or null if no configuration exists
     */
    public String getLatestConfigurationName(String type);

    /**
     * Get the configuration of a the given type and the specific name.
     * 
     * @param type the type of the configuration
     * @param name name of the configuration
     * @return serialized configuration or null if no such configuration exists
     */
    public String getConfiguration(String type, String name);
    
    /**
     * Writes a configuration into the database
     * 
     * @param config the configuration as a string
     * @param type the type of configuration being saved
     * @param name the name to associate with the save
     */
    public void writeConfiguration(String config, String type, String name);
    
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
     * Closes connection to a Cassandra cluster
     */
    public void closeConnection();
}
