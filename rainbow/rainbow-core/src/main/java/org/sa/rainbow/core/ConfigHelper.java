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
package org.sa.rainbow.core;



import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ConfigHelper {

    private static List<Properties> CONFIG_PROPERTIES;


    private static List<InputStream> loadResources (final String name, final ClassLoader classLoader) throws
                                                                                                      IOException {
        final List<InputStream> list = new ArrayList<> ();
        final Enumeration<URL> systemResources = (classLoader == null) ? ClassLoader.getSystemClassLoader ()
                .getResources (name) : classLoader.getResources (name);
                while (systemResources.hasMoreElements ()) {
                    list.add (systemResources.nextElement ().openStream ());
                }
                return list;
    }

    private static List<? extends Properties> getConfigProperties () {
        if (CONFIG_PROPERTIES != null) return CONFIG_PROPERTIES;
        try {
            List<InputStream> configs = loadResources ("config.properties", ConfigHelper.class.getClassLoader ());
            List<Properties> properties = new ArrayList<> ();
            for (InputStream is : configs) {
                Properties p = new Properties ();
                p.load (is);
                properties.add (p);
            }
            CONFIG_PROPERTIES = properties;
            return CONFIG_PROPERTIES;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        return Collections.<Properties> emptyList ();
    }


    public static List<String> getProperties (String key) {
        List<? extends Properties> props = getConfigProperties ();
        List<String> vals = new ArrayList<> ();
        for (Properties p : props) {
            String val = p.getProperty (key);
            if (val != null) {
                vals.add (val);
            }
        }
        return vals;
    }
}
