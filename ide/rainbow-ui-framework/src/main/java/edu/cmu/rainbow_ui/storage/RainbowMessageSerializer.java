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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

/**
 * Helper class to perform Rainbow messages serialization/deserialization
 *
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class RainbowMessageSerializer {

    /**
     * Mapping of property names to columns and back. These two maps implement bidirectional map.
     */
    private static final Map<String, String> propertyToColumn = new HashMap<>();
    private static final Map<String, String> columnToProperty = new HashMap<>();

    /**
     * Serialize message to a list of string pairs
     *
     * @param message Rainbow message
     * @return list of string pairs
     */
    public static List<Pair<String, String>> serialize(IRainbowMessage message) {
        List<Pair<String, String>> result = new LinkedList<>();
        for (String propName : message.getPropertyNames()) {
            String propValue = (String) message.getProperty(propName);
            /* Convert the property name */
            String colName = propertyToColumn.get(propName);
            if (colName == null) {
                colName = propName.replace("_", "").replace("-", "");
                propertyToColumn.put(propName, colName);
                columnToProperty.put(colName, propName);
            }
            result.add(new ImmutablePair<>(colName, propValue));
        }

        return result;
    }

    /**
     * Deserialize Rainbow message
     *
     * @param message serialized as list of string pairs Rainbow message
     * @return Rainbow message
     */
    public static IRainbowMessage deserialize(List<Pair<String, String>> message) {
        /* The only type of messages that is being used */
        IRainbowMessage result = new RainbowESEBMessage();

        for (Pair<String, String> pair : message) {
            try {
                String colName = pair.getKey();
                String propName = columnToProperty.get(colName);
                if (propName == null) {
                    Logger.getLogger(RainbowMessageSerializer.class.getName()).log(
                            Level.WARNING, "Cannot deserialize an event, unknown column name: {0}",
                            colName);
                } else {
                    result.setProperty(propName, pair.getValue());
                }
            } catch (RainbowException ex) {
                Logger.getLogger(RainbowMessageSerializer.class.getName()).log(
                        Level.WARNING, "Cannot deserialize an event", ex);
            }
        }
        return result;
    }

    /**
     * Update the mapping between property names and column names.
     *
     * @param propertyToCollumnPairs a list of pairs "property name" - "column name"
     */
    public static void updatePropertyColumnMapping(List<Pair<String, String>> propertyToCollumnPairs) {
        for (Pair<String, String> item : propertyToCollumnPairs) {
            propertyToColumn.put(item.getKey(), item.getValue());
            columnToProperty.put(item.getValue(), item.getKey());
        }
    }

    /**
     * Get a column name for the given property name.
     *
     * @param propName name of the property
     * @return column name or null if no such property was found
     */
    public static String getColumnForProperty(String propName) {
        return propertyToColumn.get(propName);
    }

    /**
     * Get a property name for the given column.
     *
     * @param colName name of the column
     * @return property name or null if no such column was found
     */
    public static String getPropertyForColumn(String colName) {
        return columnToProperty.get(colName);
    }
}
