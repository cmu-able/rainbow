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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Establishes connection with Cassandra and defines queries for events and
 * snapshots
 * 
 * <p>
 * Creation of connection to a new cluster, tables, definition of search and
 * insert queries for snapshots and events.
 * </p>
 * 
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class DatabaseConnector implements IDatabaseConnector {

    private Session cassandraSession;
    private final AcmeModelSerializer serializer;
    private static Cluster cluster;
    private static ISystemConfiguration config;
    private String keyspaceName;
    private static final String SNAPSHOT_TABLE_NAME = "snapshot";
    private final static String MODEL_CHANGE_TABLE_NAME = "model_change";
    private final static String EVENTS_TABLE_NAME = "events";
    private final static String CONFIG_TABLE_NAME = "configurations";
    private static String currentSession;
    private final static HashSet<String> eventcolumns = new HashSet<>();
    private final static HashSet<String> modelcolumns = new HashSet<>();
    private final static String TIMESTAMP_COLUMN_NAME = "timestamp";
    private final static String CHANNEL_COLUMN_NAME = "channel";
    private final static String MODEL_COLUMN_NAME = "model";
    private final static String TYPE_COLUMN_NAME = "type";
    private final static String NAME_COLUMN_NAME = "name";
    private final static String CONFIGURATION_COLUMN_NAME = "configuration";

    /* The date object used when there is no actual date needed */
    private final Date DATE_PLACEHOLDER = new Date(0);

    /* The channel name for column headers row */
    private final static String HEADER_CHANNEL_NAME = "property";

    /**
     * Gets node information from System Configuration file
     * 
     * @param c is a System Configuration file with node information
     */
    public DatabaseConnector(ISystemConfiguration c) {
        config = c;

        establishConnection(config.getNode());
        serializer = new AcmeModelSerializer();

    }

    /**
     * Establishes connection to a Cassandra cluster
     * 
     * @param node is node information
     */
    private void establishConnection(String node) {
        cluster = Cluster.builder().addContactPoint(node).build();
        cassandraSession = cluster.connect();
        Logger.getLogger(DatabaseConnector.class.getName()).log(Level.INFO,
                "Connected to the Cassandra DB on the host: {0}", node);

    }

    /**
     * Closes connection to a Cassandra cluster
     */
    @Override
    public void closeConnection() {
        cluster.close();
        Logger.getLogger(DatabaseConnector.class.getName()).log(Level.INFO,
                "Disconnected from the Cassandra DB.");
    }

    @Override
    public void createSession(String name) {
        String cqlStatement = "CREATE KEYSPACE IF NOT EXISTS "
                + "\"" + name + "\""
                + " WITH "
                + "replication = {'class':'SimpleStrategy','replication_factor':1};";
        cassandraSession.execute(cqlStatement);
        useKeyspace(name);
        createEventsTable();
        createModelChangeTable();
        createSnapshotTable();
        currentSession = name;
        System.out.println(currentSession);
    }

    /**
     * Use an existing keyspace
     * 
     * @param name name of the keyspace
     */
    private void useKeyspace(String name) {
        String cqlStatement = "USE \"" + name + "\";";
        cassandraSession.execute(cqlStatement);
        keyspaceName = name;

    }

    @Override
    public void useSession(String name) {
        useKeyspace(name);
        pushColumnsToSerializer();
    }

    /**
     * Pushes information about columns to message serializer.
     */
    private void pushColumnsToSerializer() {
        String query = "Select * from " + EVENTS_TABLE_NAME + " Where "
                + CHANNEL_COLUMN_NAME + " = '" + HEADER_CHANNEL_NAME + "';";
        String query1 = "Select * from " + MODEL_CHANGE_TABLE_NAME + " Where "
                + CHANNEL_COLUMN_NAME + " = '" + HEADER_CHANNEL_NAME + "';";
        Row eventRow = cassandraSession.execute(query).one();
        Row modelRow = cassandraSession.execute(query1).one();
        ColumnDefinitions eventColumns = eventRow.getColumnDefinitions();
        ColumnDefinitions modelColumns = modelRow.getColumnDefinitions();
        ArrayList<Pair<String, String>> events = new ArrayList<>();
        ArrayList<Pair<String, String>> modelupdates = new ArrayList<>();
        for (int i = 0; i < eventColumns.size(); i++) {
            String key = eventColumns.getName(i);
            /**
             * Skip channel and timestamp
             */
            if ((!key.equals(CHANNEL_COLUMN_NAME))
                    && (!key.equals(TIMESTAMP_COLUMN_NAME))) {
                String value = eventRow.getString(i);
                Pair<String, String> pair = new ImmutablePair<>(key, value);
                events.add(pair);
            }
        }
        for (int i = 0; i < modelColumns.size(); i++) {
            String key = modelColumns.getName(i);
            /**
             * Skip channel and timestamp
             */
            if ((!key.equals(CHANNEL_COLUMN_NAME))
                    && (!key.equals(TIMESTAMP_COLUMN_NAME))) {
                String value = modelRow.getString(i);
                Pair<String, String> pair = new ImmutablePair<>(key, value);
                modelupdates.add(pair);
            }
        }
        RainbowMessageSerializer.updatePropertyColumnMapping(events);
        RainbowMessageSerializer.updatePropertyColumnMapping(modelupdates);
    }

    @Override
    public String getReadSession() {
        return keyspaceName;
    }

    @Override
    public String getWriteSession() {
        return currentSession;
    }

    @Override
    public void closeWriteSession() {
        currentSession = null;
    }

    /**
     * Get a list of the sessions by their name
     * 
     * @return an arraylist of strings that hold the name of the sessions
     *         (keyspaces)
     */
    @Override
    public ArrayList<String> getSessionList() {
        String selection = "SELECT * FROM system.schema_keyspaces";
        ArrayList<String> keyspaces = new ArrayList<String>();
        for (Row row : cassandraSession.execute(selection)) {
            keyspaces.add(row.getString(0));
        }
        return keyspaces;
    }

    /**
     * Creates table for all events except model update events. Channel name is
     * used as a partition key
     * 
     */
    private void createEventsTable() {
        String cqlStatement = "CREATE TABLE IF NOT EXISTS " + EVENTS_TABLE_NAME
                + " (" + CHANNEL_COLUMN_NAME + " text, "
                + TIMESTAMP_COLUMN_NAME + " timestamp, ";
        cqlStatement += "PRIMARY KEY (" + CHANNEL_COLUMN_NAME + ","
                + TIMESTAMP_COLUMN_NAME + "));";
        cassandraSession.execute(cqlStatement);

        Insert query = QueryBuilder.insertInto(EVENTS_TABLE_NAME)
                .value(TIMESTAMP_COLUMN_NAME, DATE_PLACEHOLDER)
                .value(CHANNEL_COLUMN_NAME, HEADER_CHANNEL_NAME);
        cassandraSession.execute(query);
        eventcolumns.clear();
        eventcolumns.add(CHANNEL_COLUMN_NAME);
        eventcolumns.add(TIMESTAMP_COLUMN_NAME);
    }

    /**
     * Creates table for model update events, partition key is the same for the
     * whole table and equals to the name of the table
     * 
     */
    private void createModelChangeTable() {
        String cqlStatement = "CREATE TABLE IF NOT EXISTS "
                + MODEL_CHANGE_TABLE_NAME + " (" + CHANNEL_COLUMN_NAME
                + " text, " + TIMESTAMP_COLUMN_NAME + " timestamp, ";
        cqlStatement += "PRIMARY KEY (" + CHANNEL_COLUMN_NAME + ","
                + TIMESTAMP_COLUMN_NAME + "));";
        cassandraSession.execute(cqlStatement);
        Calendar cal = Calendar.getInstance();
        Insert query = QueryBuilder.insertInto(MODEL_CHANGE_TABLE_NAME)
                .value(TIMESTAMP_COLUMN_NAME, DATE_PLACEHOLDER)
                .value(CHANNEL_COLUMN_NAME, HEADER_CHANNEL_NAME);
        cassandraSession.execute(query);
        modelcolumns.clear();
        modelcolumns.add(CHANNEL_COLUMN_NAME);
        modelcolumns.add(TIMESTAMP_COLUMN_NAME);

    }

    /**
     * Creates table for snapshots
     */
    private void createSnapshotTable() {
        String cqlStatement = "CREATE TABLE IF NOT EXISTS "
                + SNAPSHOT_TABLE_NAME + " (" + CHANNEL_COLUMN_NAME + " text, "
                + TIMESTAMP_COLUMN_NAME + " timestamp, " + MODEL_COLUMN_NAME
                + " text, ";
        cqlStatement += "PRIMARY KEY (" + CHANNEL_COLUMN_NAME + ", "
                + TIMESTAMP_COLUMN_NAME + "));";
        cassandraSession.execute(cqlStatement);
    }

    /**
     * Creates table for configurations
     */
    public void createConfigKeyspace() {
        String cqlStatement = "CREATE KEYSPACE IF NOT EXISTS "
                + CONFIG_TABLE_NAME
                + " WITH "
                + "replication = {'class':'SimpleStrategy','replication_factor':1};";
        cassandraSession.execute(cqlStatement);

        cqlStatement = "USE \"" + CONFIG_TABLE_NAME + "\";";
        cassandraSession.execute(cqlStatement);

        createConfigTable();
    }

    /**
     * Creates table for configurations
     */
    private void createConfigTable() {
        String cqlStatement = "CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE_NAME
                + " (" + TIMESTAMP_COLUMN_NAME + " timestamp, "
                + NAME_COLUMN_NAME + " text, " + TYPE_COLUMN_NAME + " text, "
                + CONFIGURATION_COLUMN_NAME + " text, ";
        cqlStatement += "PRIMARY KEY (" + TYPE_COLUMN_NAME + ", "
                + TIMESTAMP_COLUMN_NAME + "));";
        cassandraSession.execute(cqlStatement);

        cqlStatement = "CREATE INDEX IF NOT EXISTS type_index ON "
                + CONFIG_TABLE_NAME + " (" + NAME_COLUMN_NAME + ")";
        cassandraSession.execute(cqlStatement);
    }

    @Override
    public void writeConfiguration(String config, String type, String name) {
        Insert query = QueryBuilder.insertInto(CONFIG_TABLE_NAME)
                .value(CONFIGURATION_COLUMN_NAME, config)
                .value(TYPE_COLUMN_NAME, type).value(NAME_COLUMN_NAME, name)
                .value(TIMESTAMP_COLUMN_NAME, System.currentTimeMillis());
        cassandraSession.execute(query);
    }

    @Override
    public ArrayList<String> getConfigurationList(String type) {
        String query = "SELECT name FROM " + CONFIG_TABLE_NAME + " WHERE "
                + TYPE_COLUMN_NAME + " = '" + type + "'" + " ORDER BY "
                + TIMESTAMP_COLUMN_NAME + " DESC;";
        ResultSet results = cassandraSession.execute(query);
        List<Row> rows = results.all();
        ArrayList<String> configurations = new ArrayList<>();
        for (Row row : rows) {
            configurations.add(row.getString(0));
        }

        return configurations;
    }

    @Override
    public String getLatestConfigurationName(String type) {
        String query = "SELECT name FROM " + CONFIG_TABLE_NAME + " WHERE "
                + TYPE_COLUMN_NAME + " = '" + type + "'" + " ORDER BY "
                + TIMESTAMP_COLUMN_NAME + " DESC LIMIT 1;";
        ResultSet results = cassandraSession.execute(query);
        Row row = results.one();

        if (row != null) {
            return row.getString(0);
        } else {
            return null;
        }
    }

    @Override
    public String getConfiguration(String type, String name) {
        String query = "SELECT " + CONFIGURATION_COLUMN_NAME + " FROM "
                + CONFIG_TABLE_NAME + " WHERE " + TYPE_COLUMN_NAME + " = '"
                + type + "'" + " AND " + NAME_COLUMN_NAME + " = '" + name + "'"
                + ";";
        ResultSet results = cassandraSession.execute(query);
        Row row = results.one();

        if (row != null) {
            return row.getString(0);
        } else {
            return null;
        }

    }

    /**
     * Checks whether table for snapshots already exists
     * 
     * @return boolean result
     */
    /**
     * Writes snapshot into existing snapshot table
     * 
     * @param snapshot of IModelInstance<?> type which is snapshot to be written
     */
    @Override
    public void writeSnapshot(IModelInstance<?> snapshot, Date timestamp) {
        IModelInstance<IAcmeSystem> model = (IModelInstance<IAcmeSystem>) snapshot;
        String serializedModel = serializer.serialize(model);
        Insert query = QueryBuilder.insertInto(SNAPSHOT_TABLE_NAME)
                .value(CHANNEL_COLUMN_NAME, SNAPSHOT_TABLE_NAME)
                .value(MODEL_COLUMN_NAME, serializedModel)
                .value(TIMESTAMP_COLUMN_NAME, timestamp.getTime());
        cassandraSession.execute(query);
    }

    /**
     * Returns a snapshot closest to the defined time with the exact timestamp
     * 
     * @param time of a snapshot
     * @return a pair of timestamp and snapshot if exists and null if not
     */
    @Override
    public ImmutablePair<Date, IModelInstance<?>> getLatestSnapshot(Date time) {
        String query = "SELECT * FROM " + SNAPSHOT_TABLE_NAME + " WHERE "
                + TIMESTAMP_COLUMN_NAME + " <= '" + time.getTime() + "' and "
                + CHANNEL_COLUMN_NAME + " = '" + SNAPSHOT_TABLE_NAME
                + "' ORDER BY " + TIMESTAMP_COLUMN_NAME + " DESC LIMIT 1;";
        ResultSet results = cassandraSession.execute(query);
        Row row = results.one();
        ImmutablePair<Date, IModelInstance<?>> snapshotpair = null;
        if (row != null) {
            Date snapshot_key = row.getDate(TIMESTAMP_COLUMN_NAME);
            String snapshot_value = row.getString(MODEL_COLUMN_NAME);
            ImmutablePair<Date, IModelInstance<?>> snapshot = new ImmutablePair<Date, IModelInstance<?>>(
                    snapshot_key, serializer.deserialize(snapshot_value));
            snapshotpair = snapshot;
        }
        return snapshotpair;
    }

    /**
     * This query writes a new event entry to Events table
     * 
     * @param channel for event type
     * @param timestamp when the message arrived
     * @param event which is a particular IRainbowMessage
     */
    @Override
    public void writeEvent(String channel, IRainbowMessage event, Date timestamp) {
        List<Pair<String, String>> messageSerialized = RainbowMessageSerializer
                .serialize(event);
        String[] keys = new String[messageSerialized.size()];
        String[] values = new String[messageSerialized.size()];
        for (int i = 0; i < messageSerialized.size(); i++) {
            keys[i] = messageSerialized.get(i).getKey();
            values[i] = messageSerialized.get(i).getValue();
            checkColumn(keys[i], EVENTS_TABLE_NAME);
        }
        /**
         * Pad keys with quotation marks. The column name has to be enclosed in
         * quotation marks due to case-sensitivity
         */
        for (int i = 0; i < messageSerialized.size(); i++) {
            keys[i] = "\"" + keys[i] + "\"";
        }
        Insert query = QueryBuilder.insertInto(EVENTS_TABLE_NAME)
                .values(keys, values)
                .value(TIMESTAMP_COLUMN_NAME, timestamp.getTime())
                .value(CHANNEL_COLUMN_NAME, channel);
        cassandraSession.execute(query);
    }

    /**
     * This query writes a new model update event to the corresponding table
     * 
     * @param timestamp when the message arrived
     * @param event which is a particular IRainbowMessage
     */
    @Override
    public void writeModelUpdateEvent(IRainbowMessage event, Date timestamp) {

        List<Pair<String, String>> messageSerialized = RainbowMessageSerializer
                .serialize(event);
        String[] keys = new String[messageSerialized.size()];
        String[] values = new String[messageSerialized.size()];
        for (int i = 0; i < messageSerialized.size(); i++) {
            keys[i] = messageSerialized.get(i).getKey();
            values[i] = messageSerialized.get(i).getValue();
            checkColumn(keys[i], MODEL_CHANGE_TABLE_NAME);
        }
        /**
         * Pad keys with quotation marks. The column name has to be enclosed in
         * quotation marks due to case-sensitivity
         */
        for (int i = 0; i < messageSerialized.size(); i++) {
            keys[i] = "\"" + keys[i] + "\"";
        }
        Insert query = QueryBuilder.insertInto(MODEL_CHANGE_TABLE_NAME)
                .values(keys, values)
                .value(TIMESTAMP_COLUMN_NAME, timestamp.getTime())
                .value(CHANNEL_COLUMN_NAME, MODEL_CHANGE_TABLE_NAME);
        cassandraSession.execute(query);
    }

    /**
     * Checks whether column exists in ModelChange or Events table
     * 
     * @param column for column name
     * @param table for table name
     */
    public void checkColumn(String column, String table) {

        HashSet<String> columns;
        if (table.equals(MODEL_CHANGE_TABLE_NAME)) {
            columns = modelcolumns;
        } else {
            columns = eventcolumns;
        }

        if (!columns.contains(column)) {
            addColumn(column, table);
        }
    }

    /**
     * Adds new column to Events or ModelChange table
     * 
     * @param column for column name
     * @param table for table name
     */
    public void addColumn(String column, String table) {
        String query = "Alter table " + table + " Add \"" + column
                + "\" text ;";
        cassandraSession.execute(query);
        query = "Update " + table + " Set \"" + column + "\" = '"
                + RainbowMessageSerializer.getPropertyForColumn(column)
                + "' Where " + CHANNEL_COLUMN_NAME + " = '"
                + HEADER_CHANNEL_NAME + "' and " + TIMESTAMP_COLUMN_NAME
                + " = " + DATE_PLACEHOLDER.getTime() + ";";
        cassandraSession.execute(query);
        HashSet<String> columns;
        if (table.equals(MODEL_CHANGE_TABLE_NAME)) {
            columns = modelcolumns;
        } else {
            columns = eventcolumns;
        }
        columns.add(column);
    }

    /**
     * Convert the row result to the Rainbow message.
     * 
     * @param row the Row from the ResultSet
     * @return Rainbow message
     */
    private IRainbowMessage getMessageFromRow(Row row) {
        ColumnDefinitions columns = row.getColumnDefinitions();
        List<Pair<String, String>> message = new LinkedList<>();
        for (int i = 0; i < columns.size(); i++) {
            String key = columns.getName(i);
            /**
             * Timestamp and channel are artificial columns, they do not
             * correspond to any real message property, so we do not add them.
             */
            if (!key.equals(TIMESTAMP_COLUMN_NAME)
                    && !key.equals(CHANNEL_COLUMN_NAME)) {
                String value = row.getString(i);
                Pair<String, String> pair = new ImmutablePair<>(key, value);
                message.add(pair);
            }
        }
        return RainbowMessageSerializer.deserialize(message);
    }

    /**
     * Returns a list of events for a certain timestamp range
     * 
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return an array of IRainbow messages containing all events for
     *         predefined time range
     */
    @Override
    public ArrayList<IRainbowMessage> getEventRange(Date startTime, Date endTime) {
        String query = "SELECT * FROM " + EVENTS_TABLE_NAME + " WHERE "
                + TIMESTAMP_COLUMN_NAME + " >= '" + startTime.getTime()
                + "' AND " + TIMESTAMP_COLUMN_NAME + " <= '"
                + endTime.getTime() + "' ALLOW FILTERING";
        ArrayList<IRainbowMessage> events = new ArrayList<>();
        ResultSet results = cassandraSession.execute(query);
        for (Row row : results) {
            events.add(getMessageFromRow(row));
        }
        return events;
    }

    /**
     * Returns a list of events by type for a certain timestamp range
     * 
     * @param channel for event type
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return an array of IRainbow messages containing all events for
     *         predefined time range
     */
    @Override
    public ArrayList<IRainbowMessage> getEventRangeByType(String channel,
            Date startTime, Date endTime) {
        String query = "Select * from " + EVENTS_TABLE_NAME + " where "
                + TIMESTAMP_COLUMN_NAME + " >= '" + startTime.getTime()
                + "' and " + TIMESTAMP_COLUMN_NAME + " <= '"
                + endTime.getTime() + "' and " + CHANNEL_COLUMN_NAME + " = '"
                + channel + "' allow filtering;";

        ArrayList<IRainbowMessage> events = new ArrayList<>();
        ResultSet results = cassandraSession.execute(query);
        for (Row row : results) {
            events.add(getMessageFromRow(row));
        }
        return events;
    }

    /**
     * Returns a list of model update events for a certain timestamp range
     * 
     * @param startTime stands for an earliest event time stamp
     * @param endTime stands for an latest event time stamp
     * @return an array of IRainbow messages containing all events for
     *         predefined time range
     */
    @Override
    public ArrayList<IRainbowMessage> getModelEventRange(Date startTime,
            Date endTime) {
        String query = "Select * from " + MODEL_CHANGE_TABLE_NAME + " where "
                + TIMESTAMP_COLUMN_NAME + " >= '" + startTime.getTime()
                + "' and " + TIMESTAMP_COLUMN_NAME + " <= '"
                + endTime.getTime() + "' allow filtering;";

        ArrayList<IRainbowMessage> events = new ArrayList<>();
        ResultSet results = cassandraSession.execute(query);
        for (Row row : results.all()) {
            events.add(getMessageFromRow(row));
        }
        return events;
    }

    /**
     * Returns an Event from Event table
     * 
     * @param time stands for a time stamp for which event is requested
     * @return Event in IRainbowMessage format
     */
    @Override
    public IRainbowMessage getEvent(Date time) {
        String query = "Select * from " + EVENTS_TABLE_NAME + " where "
                + TIMESTAMP_COLUMN_NAME + " = '" + time.getTime()
                + "' allow filtering;";
        Row row = cassandraSession.execute(query).one();
        return getMessageFromRow(row);
    }

    /**
     * Removes the keyspace from the database
     * 
     * @param name for the name of keyspace (session)
     * 
     */
    public void dropKeyspace(String name) {
        String cqlStatement = "DROP KEYSPACE \"" + name + "\";";
        cassandraSession.execute(cqlStatement);
    }

    @Override
    public Date getStartDate() {
        String query = "Select " + TIMESTAMP_COLUMN_NAME +" from " + SNAPSHOT_TABLE_NAME 
                + " where " + CHANNEL_COLUMN_NAME + " = '" + SNAPSHOT_TABLE_NAME 
                + "' ORDER BY " + TIMESTAMP_COLUMN_NAME + " ASC LIMIT 1;";

        Date date;
        ResultSet results = cassandraSession.execute(query);
        Row row = results.one();
        date = row.getDate(0);
        return date;
    }

    @Override
    public boolean currentSessionIsWriteSession() {
        return this.getReadSession().equals(this.getWriteSession());
    }

    @Override
    public Date getMaxDate() {
        String query = "Select " + TIMESTAMP_COLUMN_NAME +" from " + EVENTS_TABLE_NAME
                + " ORDER BY " + TIMESTAMP_COLUMN_NAME + " DESC LIMIT 1;";

        Date date;
        ResultSet results = cassandraSession.execute(query);
        Row row = results.one();
        date = row.getDate(0);
        return date;
    }

    @Override
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime,
            int numEvents) {
        String query = "SELECT * FROM " + EVENTS_TABLE_NAME + " WHERE "
                + TIMESTAMP_COLUMN_NAME + " <= '" + endTime.getTime()
                + "' LIMIT " + numEvents + " ALLOW FILTERING";
        ArrayList<IRainbowMessage> events = new ArrayList<>();
        ResultSet results = cassandraSession.execute(query);
        for (Row row : results) {
            events.add(getMessageFromRow(row));
        }
        return events;
    }    
}
