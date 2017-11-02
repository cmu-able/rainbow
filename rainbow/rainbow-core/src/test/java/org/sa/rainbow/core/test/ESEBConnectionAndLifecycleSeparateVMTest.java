package org.sa.rainbow.core.test;

import auxtestlib.CommandRunner.ProcessInterface;
import auxtestlib.*;
import org.apache.log4j.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests various lifecycle methods for masters and delegates by starting delegates in separate VMs and ensuring that
 * connections happen.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class ESEBConnectionAndLifecycleSeparateVMTest extends DefaultTCase {

    @TestHelper
    ThreadCountTestHelper m_threadCountHelper;

    /**
     * The directory that the test was started in, so that it can be used subsequently to start the delegate in the same
     * place
     **/
    private static String s_currentDirectory;

    /** Sets the user path to be the right place for finding the properties file for this test **/
    private void configureTestProperties () throws IOException {
        File basePath = new File (s_currentDirectory);
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @BeforeClass
    public static void setCurrentDirectory () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    @After
    public void resetCurrentDirectory () {
        System.setProperty ("user.dir", s_currentDirectory);
    }

    /**
     * Tests that the delegate receives configuration information after starting and connecting to the delegate
     * 
     * @throws Exception
     */
    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        configureTestProperties ();
        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        try {
            master.start ();

            ProcessInterface pi = launchDelegate (s_currentDirectory);
            int wait = TestPropertiesDefinition.getInt ("delegate.connection.time");
            Thread.sleep (wait);
            String logMsg = pi.getOutputText ();
            Pattern configPattern = Pattern.compile ("RD-.*: Received configuration information");
            Matcher m = configPattern.matcher (logMsg);
            assertTrue (m.find ());
        }
        finally {
            master.terminate ();
        }
    }

    /** Launches teh delegate in a new VM **/
    private ProcessInterface launchDelegate (String current) throws IOException {
        JavaLauncher launcher = new JavaLauncher ();
        ProcessInterface pi = launcher.launch_java_async (this.getClass ().getCanonicalName (), new File (current),
                Collections.singletonList (""), 10000);
        return pi;
    }

    /**
     * Tests that the master receives a heartbeat after the delegate has connected
     * 
     * @throws Exception
     */
    @Test
    public void testHeartbeatSetup () throws Exception {

        // Heartbeat message appears in log file, so use this as a way to test
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        configureTestProperties ();

        // Start a delegate and a master
        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        master.start ();

        ProcessInterface pi = launchDelegate (s_currentDirectory);

        int extra = TestPropertiesDefinition.getInt ("heartbeat.extra.time");
        Thread.sleep (Integer.valueOf (Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD))
                + extra * 3);
        String logMsg = baos.toString ();
        assertTrue (logMsg.contains ("Received heartbeat from known delegate: "));

        master.terminate ();

    }

    private void startDelegate () throws Exception {
        BasicConfigurator.configure ();
        configureTestProperties ();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

    }

    /**
     * Called by launchDelegate to start the delegate in a new VM
     * 
     * @param args
     * @throws Exception
     */
    public static void main (String[] args) throws Exception {
        setCurrentDirectory ();
        new ESEBConnectionAndLifecycleSeparateVMTest ().startDelegate ();
        Thread.sleep (10000);
    }

}
