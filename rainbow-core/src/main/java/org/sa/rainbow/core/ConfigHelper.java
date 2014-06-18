package org.sa.rainbow.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class ConfigHelper {

    static List<Properties> CONFIG_PROPERTIES;

    public static List<InputStream> loadResources (final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<> ();
        final Enumeration<URL> systemResources = (classLoader == null) ? ClassLoader.getSystemClassLoader ()
                .getResources (name) : classLoader.getResources (name);
                while (systemResources.hasMoreElements ()) {
                    list.add (systemResources.nextElement ().openStream ());
                }
                return list;
    }

    public static List<? extends Properties> getConfigProperties () {
        if (CONFIG_PROPERTIES != null) return CONFIG_PROPERTIES;
        try {
            List<InputStream> configs = loadResources ("config.properties", ConfigHelper.class.getClassLoader ());
            List<Properties> properties = new ArrayList<Properties> ();
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
