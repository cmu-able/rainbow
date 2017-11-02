package org.sa.rainbow.core;

import java.io.File;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.globals.Environment;
import org.sa.rainbow.core.globals.ExitState;

/**
 * Created by schmerl on 6/16/2016.
 */
public interface IRainbowEnvironment extends RainbowConstants, IRainbowPropertyProvider {
    Logger LOGGER = Logger.getLogger (Rainbow.class);
    /** The thread name */
    String NAME   = "Rainbow Runtime Infrastructure";

    boolean shouldTerminate ();

    void signalTerminate ();

    void signalTerminate (ExitState exitState);

    int exitValue ();

    void setExitState (ExitState state);

    boolean isMaster ();



    ThreadGroup getThreadGroup ();

    File getTargetPath ();

    void setMaster (RainbowMaster rainbowMaster);

    RainbowMaster getRainbowMaster ();

    void registerGauge (IGauge gauge);

    IGauge lookupGauge (String id);

    Environment environment ();
}
