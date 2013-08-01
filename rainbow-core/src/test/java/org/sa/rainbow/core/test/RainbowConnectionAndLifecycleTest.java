package org.sa.rainbow.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.IRainbowRunnable.State;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestPropertiesDefinition;

/**
 * Base test class for testing lifecycle information. For each kind of connector (ESEB, Local, RMI), a test should
 * inherit this class and override the configureTestProperties to ensure that the right property for constructing the
 * connectors is used.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class RainbowConnectionAndLifecycleTest extends DefaultTCase {

    private static String s_currentDirectory;

    @BeforeClass
    public static void rememberUserDir () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    /**
     * Test that a delegate receives configuration information after connecting.
     * 
     * @throws Exception
     */
    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        BasicConfigurator.configure ();
        configureTestProperties ();

        RainbowMaster master = null;
        RainbowDelegate delegate = null;
        try {
            int wait = TestPropertiesDefinition.getInt ("delegate.connection.time");
            master = new RainbowMaster ();
            master.initialize ();

            master.start ();
            delegate = new RainbowDelegate ();
            delegate.start ();
            final RainbowDelegate d = delegate;

            wait_for_true (new BooleanEvaluation () {

                @Override
                public boolean evaluate () throws Exception {
                    return d.getConfigurationInformation () != null
                            && d.getConfigurationInformation ().getProperty ("test.configuration.property") != null;
                }
            }, wait);
//        String logMsg = baos.toString ();
//        Pattern configPattern = Pattern.compile ("RD-" + delegate.getId () + ".*: Received configuration information");
//        Matcher m = configPattern.matcher (logMsg);
//        assertTrue (m.find ());
            Properties props = delegate.getConfigurationInformation ();
            assertNotNull (props);
            assertEquals ("xxx", props.getProperty ("test.configuration.property"));
        }
        finally {
            if (master != null) {
                master.terminate ();
            }
            if (delegate != null) {
                delegate.terminate ();
            }
        }

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
        RainbowMaster master = null;
        RainbowDelegate delegate = null;
        try {
            master = new RainbowMaster ();
            master.initialize ();

            master.start ();
            delegate = new RainbowDelegate ();
            delegate.start ();

            int extra = TestPropertiesDefinition.getInt ("heartbeat.extra.time");
            Thread.sleep (Integer.valueOf (Rainbow.properties ().getProperty (Rainbow.PROPKEY_DELEGATE_BEACONPERIOD))
                    + extra);
            String logMsg = baos.toString ();
            assertTrue (logMsg.contains ("Received heartbeat from known delegate: " + delegate.getId ()));

        }
        finally {
            if (master != null) {
                master.terminate ();
            }
            if (delegate != null) {
                delegate.terminate ();
            }
        }

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

        RainbowMaster master = null;
        RainbowDelegate delegate = null;
        try {
            master = new RainbowMaster ();
            master.initialize ();

            master.start ();
            delegate = new RainbowDelegate ();
            delegate.start ();

            // Wait for things to get connected
            Thread.sleep (6000);

            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
            Logger.getRootLogger ().addAppender (wa);
            Logger.getRootLogger ().setLevel (Level.ALL);

            delegate.stop ();
            Thread.sleep (5000);
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

        }
        finally {
            if (master != null) {
                master.terminate ();
            }
            if (delegate != null) {
                delegate.terminate ();
            }
        }

    }


}
