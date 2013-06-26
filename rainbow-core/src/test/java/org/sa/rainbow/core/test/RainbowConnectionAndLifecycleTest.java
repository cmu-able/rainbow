package org.sa.rainbow.core.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.Test;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.IRainbowRunnable.State;

/**
 * Base test class for testing lifecycle information. For each kind of connector (ESEB, Local, RMI), a test should
 * inherit this class and override the configureTestProperties to ensure that the right property for constructing the
 * connectors is used.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class RainbowConnectionAndLifecycleTest {

    /**
     * Test that a delegate receives configuration information after connecting.
     * 
     * @throws Exception
     */
    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        BasicConfigurator.configure ();
        configureTestProperties ();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        RainbowMaster master = new RainbowMaster ();
        master.start ();
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

        Thread.sleep (5000);
        String logMsg = baos.toString ();
        Pattern configPattern = Pattern.compile ("RD-" + delegate.getId () + ".*: Received configuration information");
        Matcher m = configPattern.matcher (logMsg);
        assertTrue (m.find ());

        master.terminate ();
        delegate.terminate ();

    }

    /**
     * Tests that a master receives a heartbeat after a delegate connects and is configured
     * 
     * @throws Exception
     */
    @Test
    public void testHeartbeatSetup () throws Exception {
        BasicConfigurator.configure ();

        // Heartbeat message appears in log file, so use this as a way to test
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        configureTestProperties ();

        // Start a delegate and a master
        RainbowMaster master = new RainbowMaster ();
        master.start ();
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

        // Wait for 7 seconds before checking if heartbeat was recorded
        Thread.sleep (7000);
        String logMsg = baos.toString ();
        assertTrue (logMsg.contains ("Received heartbeat from known delegate: " + delegate.getId ()));

        master.terminate ();
        delegate.terminate ();

    }

    protected abstract void configureTestProperties () throws IOException;

    /**
     * Tests that lifecycle operations work correctly. E.g., hearbeats are not sent if a delegate is paused, but are
     * sent after it is restarted.
     * 
     * @throws Exception
     */
    @Test
    public void testPauseAndRestart () throws Exception {
        BasicConfigurator.configure ();


        configureTestProperties ();

        RainbowMaster master = new RainbowMaster ();
        master.start ();
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

        // Wait for things to get connected
        Thread.sleep (6000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        delegate.stop ();
        Thread.sleep (1000);
        assertTrue (delegate.state () == State.STOPPED);

        String logMsg = baos.toString ();
        Logger.getRootLogger ().removeAppender (wa);
        Pattern pausePattern = Pattern.compile ("RD-" + delegate.getId () + ".*: Pausing");


        Matcher m = pausePattern.matcher (logMsg);
        assertTrue (m.find ());
        int start = m.start ();
        assertTrue (logMsg.indexOf ("Received heartbeat from known delegate: " + delegate.getId (), start) == -1);

        baos = new ByteArrayOutputStream ();
        wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        delegate.start ();
        Thread.sleep (10000);
        logMsg = baos.toString ();
        assertTrue (logMsg.indexOf ("Received heartbeat from known delegate: " + delegate.getId (), start) != -1);

        master.terminate ();
        delegate.terminate ();

    }

    @After
    public void reconfigureLogging () {
        Logger.getRootLogger ().removeAllAppenders ();
    }
}
