/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
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
package org.sa.rainbow.core.ports.eseb;

import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

public class RainbowESEBMessage implements IRainbowMessage {
    static final Logger LOGGER = Logger.getLogger (RainbowESEBMessage.class);
    protected static final MapDataType    MAP_STRING_TO_ANY = MapDataType.map_of (ESEBProvider.SCOPE.string (), ESEBProvider.SCOPE.any (), ESEBProvider.SCOPE);
    /** The prefix that encodes properties in maps that are sent on the wire **/
    private static final String PROP_PREFIX = "__PROP_";
    private static final int    PROP_PREFIX_LENGTH = PROP_PREFIX.length ();

    private MapDataValue            m_esebMap;

    public RainbowESEBMessage () {
        m_esebMap = MAP_STRING_TO_ANY.make ();
    }

    public RainbowESEBMessage (MapDataValue mdv) {
        try {
            m_esebMap = mdv.clone ();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace ();
        }
    }


    @Override
    public List<String> getPropertyNames () {
        LinkedList<String> pns = new LinkedList<> ();
        Map<DataValue, DataValue> all = m_esebMap.all ();
        Set<DataValue> keySet = all.keySet ();
        for (DataValue dv : keySet) {
            if (dv.type ().equals (ESEBProvider.SCOPE.string ())) {
                pns.add (((StringValue )dv).value ());
            }
        }
        return pns;
    }


    @Override
    public Object getProperty (String id) {
        DataValue dv = m_esebMap.get (ESEBProvider.SCOPE.string ().make (id));
        if (dv == null)
            return null;
        try {
            return ESEBProvider.CONVERTER.to_java (dv, null);
        }
        catch (ValueConversionException e) {
        }
        // Should never happen
        return null;
    }

    @Override
    public void setProperty (String id, Object prop) throws RainbowException {
        try {
            m_esebMap.put (ESEBProvider.CONVERTER.from_java (id, ESEBProvider.SCOPE.string ()), ESEBProvider.CONVERTER.from_java (prop, null));
        }
        catch (ValueConversionException e) {
            // Should only happen on the prop
            throw new RainbowException (MessageFormat.format (
                    "Could not convert a Java object of type {0} to an ESEB value", prop.getClass ()
                    .getCanonicalName ()), e);
        }
    }

    public void setProperty (String id, String prop) {
        try {
            setProperty (id, (Object )prop);
        }
        catch (RainbowException e) {
            // Should not happen
        }
    }

    public void setProperty (String id, short prop) {
        try {
            setProperty (id, (Object )prop);
        }
        catch (RainbowException e) {
            // Should not happen
        }
    }

    public void setProperty (String id, long prop) {
        try {
            setProperty (id, (Object )prop);
        }
        catch (RainbowException e) {
            // Should not happen
        }
    }

    public void setProperty (String id, int prop) {
        try {
            setProperty (id, (Object )prop);
        }
        catch (RainbowException e) {
        }
    }

    /**
     * Returns the internal representation. Note the protection here is the default (package) because the
     * ESEBEventService will need to get the information out
     * 
     * @return
     */
    MapDataValue getDataValue () {
        return m_esebMap;
    }

    public void removeProperty (String key) {
        m_esebMap.remove (ESEBProvider.SCOPE.string ().make (key));
    }

    public void fillProperties (Properties props) {
        for (Entry<Object, Object> entry : props.entrySet ()) {
            String key;
            if (entry.getKey () instanceof String) {
                key = PROP_PREFIX + entry.getKey ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string key is not allowed: {0}", entry.getKey ()));
                continue;
            }
            String value;
            if (entry.getValue () instanceof String) {
                value = (String )entry.getValue ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string value is not allowed: {0} -> {1}",
                        entry.getKey (), entry.getValue ().getClass ()));
                continue;
            }
            m_esebMap.put (ESEBProvider.SCOPE.string ().make (key), ESEBProvider.SCOPE.string ().make (value));
        }

    }


    public Properties pulloutProperties () {
        Properties p = new Properties ();
        Map<DataValue, DataValue> msg = m_esebMap.all ();
        Iterator<Entry<DataValue, DataValue>> iterator = msg.entrySet ().iterator ();
        while (iterator.hasNext ()) {
            Entry<DataValue, DataValue> entry = iterator.next ();
            String key = ((StringValue )entry.getKey ()).value ();
            if (key.startsWith (PROP_PREFIX)) {
                if (entry.getValue () instanceof StringValue) {
                    p.setProperty (key.substring (PROP_PREFIX_LENGTH), ((StringValue )entry.getValue ()).value ());
                    iterator.remove ();
                }
                else {
                    LOGGER.warn ("The property key " + key + " is not a String as expected"
                            + entry.getValue ().type ().toString ());
                }
            }
        }
        return p;
    }

    public boolean hasProperty (String key) {
        return m_esebMap.contains (ESEBProvider.SCOPE.string ().make (key));
    }


    @Override
    public String toString () {
        StringBuilder str = new StringBuilder ();
        Map<DataValue, DataValue> all = m_esebMap.all ();
        str.append ("Message {\n");
        for (Entry<DataValue, DataValue> e : all.entrySet ()) {
            str.append ("  ");
            str.append (e.getKey ().toString ());
            str.append (" --> ");
            str.append (e.getValue ().toString ());
            str.append ("\n");
        }
        str.append ("}\n");
        return str.toString ();
    }

}
