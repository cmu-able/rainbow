package org.sa.rainbow.core;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.globals.Environment;
import org.sa.rainbow.core.globals.ExitState;

import java.io.File;
import java.util.Properties;

/**
 * Created by schmerl on 6/16/2016.
 */
public interface IRainbowEnvironment extends RainbowConstants {
    Logger LOGGER = Logger.getLogger (Rainbow.class);
    /** The thread name */
    String NAME   = "Rainbow Runtime Infrastructure";

    boolean shouldTerminate ();

    void signalTerminate ();

    void signalTerminate (ExitState exitState);

    int exitValue ();

    void setExitState (ExitState state);

    boolean isMaster ();

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

    ThreadGroup getThreadGroup ();

    File getTargetPath ();

    void setMaster (RainbowMaster rainbowMaster);

    RainbowMaster getRainbowMaster ();

    void registerGauge (IGauge gauge);

    IGauge lookupGauge (String id);

    Environment environment ();
}
