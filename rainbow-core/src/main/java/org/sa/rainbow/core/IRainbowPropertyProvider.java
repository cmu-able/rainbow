package org.sa.rainbow.core;

import java.util.Properties;

public interface IRainbowPropertyProvider {
    String getProperty (String key, String defaultProperty);

    String getProperty (String key);

    boolean getProperty (String key, boolean b);

    long getProperty (String key, long default_);

    short getProperty (String key, short default_);

    int getProperty (String key, int default_);

    double getProperty (String key, double default_);

    void setProperty (String key, short val);

    void setProperty (String key, long val);

    void setProperty (String key, boolean val);

    void setProperty (String key, String val);

    void setProperty (String key, double val);

    void setProperty (String key, int val);

    Properties allProperties ();
}
